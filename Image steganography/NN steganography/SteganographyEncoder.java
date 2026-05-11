import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SteganographyEncoder {
    private static final byte[] MAGIC = new byte[] { 'S', 'T', 'G', '1' };
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int PBKDF2_ITERATIONS = 390000;
    private static final int KEY_SIZE_BITS = 256;

    public static boolean encode(String inputImagePath, String outputImagePath, String message) {
        return encode(inputImagePath, outputImagePath, message, null);
    }

    public static boolean encode(String inputImagePath, String outputImagePath, String message, String password) {
        try {
            BufferedImage image = ImageIO.read(new File(inputImagePath));
            if (image == null) {
                System.out.println("Error: Unsupported image format.");
                return false;
            }

            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            boolean useEncryption = password != null && !password.isBlank();
            byte[] payload = useEncryption ? encrypt(messageBytes, password) : messageBytes;
            byte[] lengthBytes = intToBytes(payload.length);
            byte[] fullMessage = new byte[MAGIC.length + lengthBytes.length + 1 + payload.length];

            // Prepend a magic header and message length to the actual message
            System.arraycopy(MAGIC, 0, fullMessage, 0, MAGIC.length);
            System.arraycopy(lengthBytes, 0, fullMessage, MAGIC.length, lengthBytes.length);
            fullMessage[MAGIC.length + lengthBytes.length] = (byte) (useEncryption ? 1 : 0);
            System.arraycopy(payload, 0, fullMessage, MAGIC.length + lengthBytes.length + 1, payload.length);

            int capacityBits = image.getWidth() * image.getHeight() * 3;
            int requiredBits = fullMessage.length * 8;
            if (requiredBits > capacityBits) {
                System.out.println("Error: Message is too large for this image.");
                return false;
            }

            int bitCursor = 0;
            outer:
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgb = image.getRGB(x, y);
                    int alpha = (rgb >>> 24) & 0xFF;
                    int red = (rgb >>> 16) & 0xFF;
                    int green = (rgb >>> 8) & 0xFF;
                    int blue = rgb & 0xFF;

                    if (bitCursor < requiredBits) {
                        red = (red & 0xFE) | getBit(fullMessage, bitCursor++);
                    }
                    if (bitCursor < requiredBits) {
                        green = (green & 0xFE) | getBit(fullMessage, bitCursor++);
                    }
                    if (bitCursor < requiredBits) {
                        blue = (blue & 0xFE) | getBit(fullMessage, bitCursor++);
                    }

                    int newRGB = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    image.setRGB(x, y, newRGB);

                    if (bitCursor >= requiredBits) {
                        break outer;
                    }
                }
            }

            ImageIO.write(image, "png", new File(outputImagePath));
            return true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    private static byte[] intToBytes(int val) {
        return new byte[] {
            (byte)(val >> 24),
            (byte)(val >> 16),
            (byte)(val >> 8),
            (byte)(val)
        };
    }

    private static int getBit(byte[] data, int bitIndex) {
        int byteIndex = bitIndex / 8;
        int bitInByte = 7 - (bitIndex % 8);
        return ((data[byteIndex] & 0xFF) >> bitInByte) & 1;
    }

    private static byte[] encrypt(byte[] messageBytes, String password) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(salt);
        random.nextBytes(iv);

        SecretKeySpec key = deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        byte[] cipherText = cipher.doFinal(messageBytes);

        byte[] payload = new byte[salt.length + iv.length + cipherText.length];
        System.arraycopy(salt, 0, payload, 0, salt.length);
        System.arraycopy(iv, 0, payload, salt.length, iv.length);
        System.arraycopy(cipherText, 0, payload, salt.length + iv.length, cipherText.length);
        return payload;
    }

    private static SecretKeySpec deriveKey(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE_BITS);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        spec.clearPassword();
        return new SecretKeySpec(Arrays.copyOf(keyBytes, KEY_SIZE_BITS / 8), "AES");
    }
}

