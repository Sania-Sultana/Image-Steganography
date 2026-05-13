# Image Steganography

This folder contains the Java desktop version of the image steganography app.

## What it does

- Encode a secret message into an image
- Decode a hidden message from a stego image
- Uses file-picker dialogs, so no manual path typing is needed
- Includes image preview panes and a cleaner card-based desktop UI

## Supported workflow

- Upload/select an image for encoding
- Type a secret message
- Save the encoded image as PNG
- Upload/select a stego image for decoding
- View the hidden message on screen

## Run

From the project root:

```bash
javac "NN steganography\Main.java" "NN steganography\SteganographyEncoder.java" "NN steganography\SteganographyDecoder.java"
java -cp "NN steganography" Main
```

## Notes

- Encoded images are saved as PNG to preserve hidden bits.
- If the image is too small for the message, encoding will fail with an error.
- If an image does not contain hidden data, decoding will return a clear message.
