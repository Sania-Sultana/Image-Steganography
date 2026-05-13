import streamlit as st
from PIL import Image
import io

from steganography import encode_image, decode_image


st.set_page_config(page_title="Image Steganography", layout="centered")
st.title("Image Steganography (LSB) — Upload, Encrypt, Hide, Retrieve")

st.markdown("Upload an image (PNG/JPG/JPEG). Encoding outputs a downloadable PNG.")

uploaded = st.file_uploader("Upload image for encoding", type=["png", "jpg", "jpeg"] , key="encode_uploader")
message = st.text_area("Secret message to hide", height=120)
enc_password = st.text_input("Password (optional, for AES encryption)", type="password")

if st.button("Encode and Download"):
    if not uploaded:
        st.error("Please upload an image to encode into.")
    elif not message:
        st.error("Please enter a message to hide.")
    else:
        try:
            img = Image.open(uploaded)
            buf = encode_image(img, message, password=enc_password if enc_password else None)
            st.success("Message hidden successfully — download below.")
            st.download_button("Download stego-image (PNG)", data=buf.getvalue(), file_name="stego.png", mime="image/png")
        except Exception as e:
            st.error(f"Encoding failed: {e}")

st.write("---")
st.header("Decode (Extract hidden message)")
uploaded_stego = st.file_uploader("Upload stego-image for decoding", type=["png", "jpg", "jpeg"], key="decode_uploader")
dec_password = st.text_input("Password for decryption (if used during encode)", type="password", key="dec_pwd")

if st.button("Decode"):
    if not uploaded_stego:
        st.error("Please upload an image containing a hidden message.")
    else:
        try:
            img = Image.open(uploaded_stego)
            msg, was_encrypted = decode_image(img, password=dec_password if dec_password else None)
            if was_encrypted:
                st.success("Hidden message (decrypted):")
            else:
                st.success("Hidden message:")
            st.code(msg)
        except Exception as e:
            st.error(f"Decoding failed: {e}")
