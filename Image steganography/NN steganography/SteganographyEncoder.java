import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class SteganographyEncoder {
    private static final byte[] MAGIC = new byte[] { 'S', 'T', 'G', '1' };

    public static boolean encode(String inputImagePath, String outputImagePath, String message) {
        try {
            BufferedImage image = ImageIO.read(new File(inputImagePath));
            if (image == null) {
                System.out.println("Error: Unsupported image format.");
                return false;
            }

            byte[] messageBytes = message.getBytes();
            byte[] lengthBytes = intToBytes(messageBytes.length);
            byte[] fullMessage = new byte[MAGIC.length + lengthBytes.length + messageBytes.length];

            // Prepend a magic header and message length to the actual message
            System.arraycopy(MAGIC, 0, fullMessage, 0, MAGIC.length);
            System.arraycopy(lengthBytes, 0, fullMessage, MAGIC.length, lengthBytes.length);
            System.arraycopy(messageBytes, 0, fullMessage, MAGIC.length + lengthBytes.length, messageBytes.length);

            int capacityBits = image.getWidth() * image.getHeight();
            int requiredBits = fullMessage.length * 8;
            if (requiredBits > capacityBits) {
                System.out.println("Error: Message is too large for this image.");
                return false;
            }

            int messageIndex = 0, bitIndex = 0;
            outer:
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgb = image.getRGB(x, y);
                    int blue = rgb & 0xFF;

                    if (messageIndex < fullMessage.length) {
                        int bit = (fullMessage[messageIndex] >> (7 - bitIndex)) & 1;
                        blue = (blue & 0xFE) | bit; // Set LSB

                        int newRGB = (rgb & 0xFFFF_FF00) | blue;
                        image.setRGB(x, y, newRGB);

                        bitIndex++;
                        if (bitIndex == 8) {
                            bitIndex = 0;
                            messageIndex++;
                        }
                    } else {
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
}

