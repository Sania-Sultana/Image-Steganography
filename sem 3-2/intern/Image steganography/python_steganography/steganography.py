import io
import struct
import os
import base64
from typing import Optional, Tuple

import numpy as np
from PIL import Image

from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from cryptography.hazmat.backends import default_backend
from cryptography.fernet import Fernet


def _derive_key(password: str, salt: bytes) -> bytes:
    kdf = PBKDF2HMAC(
        algorithm=hashes.SHA256(), length=32, salt=salt, iterations=390000, backend=default_backend()
    )
    return base64.urlsafe_b64encode(kdf.derive(password.encode()))


def _to_bitarray(data: bytes):
    for byte in data:
        for i in range(7, -1, -1):
            yield (byte >> i) & 1


def _bits_to_bytes(bits) -> bytes:
    b = bytearray()
    acc = 0
    count = 0
    for bit in bits:
        acc = (acc << 1) | bit
        count += 1
        if count == 8:
            b.append(acc)
            acc = 0
            count = 0
    return bytes(b)


def encrypt_message(message: bytes, password: str) -> Tuple[bytes, bytes]:
    salt = os.urandom(16)
    key = _derive_key(password, salt)
    f = Fernet(key)
    token = f.encrypt(message)
    return salt, token


def decrypt_message(salt: bytes, token: bytes, password: str) -> bytes:
    key = _derive_key(password, salt)
    f = Fernet(key)
    return f.decrypt(token)


def _embed_bytes_into_image(img: Image.Image, data: bytes) -> Image.Image:
    img = img.convert("RGB")
    arr = np.array(img)
    flat = arr.flatten()

    total_bits = flat.size
    needed_bits = len(data) * 8
    if needed_bits > total_bits:
        raise ValueError("Image does not have enough capacity for this message.")

    bitgen = _to_bitarray(data)
    for i, bit in enumerate(bitgen):
        flat[i] = (int(flat[i]) & 254) | int(bit)

    new_arr = flat.reshape(arr.shape)
    return Image.fromarray(new_arr.astype(np.uint8))


def _extract_bytes_from_image(img: Image.Image, num_bytes: int) -> bytes:
    img = img.convert("RGB")
    arr = np.array(img)
    flat = arr.flatten()
    bits = (flat[i] & 1 for i in range(num_bytes * 8))
    return _bits_to_bytes(bits)


def encode_image(img: Image.Image, message: str, password: Optional[str] = None) -> io.BytesIO:
    msg_bytes = message.encode("utf-8")
    encrypted_flag = 0
    payload = b""
    if password:
        salt, token = encrypt_message(msg_bytes, password)
        encrypted_flag = 1
        payload = salt + token
    else:
        payload = msg_bytes

    header = struct.pack(
        ">I", len(payload)
    ) + bytes([encrypted_flag])

    full = header + payload

    out_img = _embed_bytes_into_image(img, full)
    buf = io.BytesIO()
    out_img.save(buf, format="PNG")
    buf.seek(0)
    return buf


def decode_image(img: Image.Image, password: Optional[str] = None) -> Tuple[str, bool]:
    # Read header first: 4 bytes length + 1 byte flag
    header_bytes = _extract_bytes_from_image(img, 5)
    if len(header_bytes) < 5:
        raise ValueError("No hidden message found or image corrupted.")
    payload_len = struct.unpack(
        ">I", header_bytes[:4]
    )[0]
    flag = header_bytes[4]

    payload_bytes = _extract_bytes_from_image(img, 5 + payload_len)[5:]
    if len(payload_bytes) < payload_len:
        raise ValueError("Hidden payload truncated or image corrupted.")

    if flag == 1:
        if not password:
            raise ValueError("A password is required to decrypt the hidden message.")
        salt = payload_bytes[:16]
        token = payload_bytes[16:]
        plain = decrypt_message(salt, token, password)
        return plain.decode("utf-8"), True
    else:
        return payload_bytes.decode("utf-8"), False
