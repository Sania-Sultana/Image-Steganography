from PIL import Image
from steganography import encode_image, decode_image


def main():
    img = Image.new("RGB", (100, 100), "white")
    msg = "hello world"

    # Test without password
    buf = encode_image(img, msg)
    img2 = Image.open(buf)
    decoded, was_enc = decode_image(img2)
    print("decoded:", decoded, "encrypted:", was_enc)
    assert decoded == msg and was_enc is False

    # Test with password
    buf2 = encode_image(img, msg, password="secret123")
    img3 = Image.open(buf2)
    decoded2, was_enc2 = decode_image(img3, password="secret123")
    print("decoded2:", decoded2, "encrypted:", was_enc2)
    assert decoded2 == msg and was_enc2 is True

    print("All tests passed")


if __name__ == "__main__":
    main()
