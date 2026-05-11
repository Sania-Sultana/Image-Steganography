import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SteganographyDecoder {
    private static final byte[] MAGIC = new byte[] { 'S', 'T', 'G', '1' };
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int PBKDF2_ITERATIONS = 390000;
    private static final int KEY_SIZE_BITS = 256;

    public static String decode(String imagePath) {
        return decode(imagePath, null);
    }

    public static String decode(String imagePath, String password) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            if (image == null) {
                return "Error decoding message: Unsupported image format.";
            }

            int capacityBits = image.getWidth() * image.getHeight() * 3;
            int headerBits = (MAGIC.length + 4 + 1) * 8;
            if (capacityBits < headerBits) {
                return "No hidden message found in this image.";
            }

            byte[] header = extractBytes(image, 0, MAGIC.length + 4 + 1);
            byte[] magic = Arrays.copyOfRange(header, 0, MAGIC.length);
            if (!Arrays.equals(magic, MAGIC)) {
                return "No hidden message found in this image.";
            }

            int messageLength = bytesToInt(Arrays.copyOfRange(header, MAGIC.length, MAGIC.length + 4));
            int encryptedFlag = header[MAGIC.length + 4] & 0xFF;

            if (messageLength < 0) {
                return "Hidden message is missing or corrupted.";
            }

            int payloadStartBit = headerBits;
            int payloadBits = messageLength * 8;
            if (payloadStartBit + payloadBits > capacityBits) {
                return "Hidden message is missing or corrupted.";
            }

            byte[] payload = extractBytes(image, payloadStartBit, messageLength);

            if (encryptedFlag == 1) {
                if (password == null || password.isBlank()) {
                    return "A password is required to decrypt this hidden message.";
                }
                return decrypt(payload, password);
            }

            return new String(payload, StandardCharsets.UTF_8);
        } catch (AEADBadTagException e) {
            return "Incorrect password or corrupted encrypted payload.";
        } catch (GeneralSecurityException e) {
            return "Decryption failed: " + e.getMessage();
        } catch (Exception e) {
            return "Error decoding message: " + e.getMessage();
        }
    }

    private static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
               ((bytes[1] & 0xFF) << 16) |
               ((bytes[2] & 0xFF) << 8)  |
               (bytes[3] & 0xFF);
    }

    private static byte[] extractBytes(BufferedImage image, int startBit, int byteCount) {
        int endBit = startBit + (byteCount * 8);
        byte[] out = new byte[byteCount];

        int globalBit = 0;
        int writeBit = 0;
        outer:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int[] channels = new int[] {
                    (rgb >>> 16) & 0xFF,
                    (rgb >>> 8) & 0xFF,
                    rgb & 0xFF
                };

                for (int channel : channels) {
                    if (globalBit >= startBit && globalBit < endBit) {
                        int bit = channel & 1;
                        int byteIndex = writeBit / 8;
                        out[byteIndex] = (byte) ((out[byteIndex] << 1) | bit);
                        writeBit++;
                    }

                    globalBit++;
                    if (globalBit >= endBit) {
                        break outer;
                    }
                }
            }
        }

        return out;
    }

    private static String decrypt(byte[] payload, String password) throws Exception {
        if (payload.length < SALT_LENGTH + IV_LENGTH + 16) {
            throw new GeneralSecurityException("Encrypted payload is too short.");
        }

        byte[] salt = Arrays.copyOfRange(payload, 0, SALT_LENGTH);
        byte[] iv = Arrays.copyOfRange(payload, SALT_LENGTH, SALT_LENGTH + IV_LENGTH);
        byte[] cipherText = Arrays.copyOfRange(payload, SALT_LENGTH + IV_LENGTH, payload.length);

        SecretKeySpec key = deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        byte[] plain = cipher.doFinal(cipherText);
        return new String(plain, StandardCharsets.UTF_8);
    }

    private static SecretKeySpec deriveKey(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE_BITS);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        spec.clearPassword();
        return new SecretKeySpec(Arrays.copyOf(keyBytes, KEY_SIZE_BITS / 8), "AES");
    }
}


