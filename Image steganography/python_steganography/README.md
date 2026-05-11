# Image Steganography (Streamlit)

This project implements a simple file-based image steganography app using LSB embedding and optional AES encryption (via `cryptography.Fernet`).

Quick start

1. Create a virtual environment and install dependencies:

```bash
python -m venv .venv
.venv\Scripts\activate
pip install -r python_steganography/requirements.txt
```

2. Run the Streamlit app:

```bash
streamlit run python_steganography/app.py
```

Usage

- Use the top section to upload an image and type a secret message. Optionally provide a password to encrypt the message before hiding.
- Click "Encode and Download" to receive a PNG stego-image with the hidden message.
- Use the Decode section to upload a stego-image and retrieve the hidden message. Provide the same password if the message was encrypted.

Notes

- The app saves the encoded image as PNG to avoid lossy compression removing hidden bits.
- If the image is too small to hold the message, encoding will fail with an error.
