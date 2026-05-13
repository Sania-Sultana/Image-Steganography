import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class SteganographyDecoder {
    private static final byte[] MAGIC = new byte[] { 'S', 'T', 'G', '1' };

    public static String decode(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            if (image == null) {
                return "❌ Error decoding message: Unsupported image format.";
            }

            int messageLength = 0;
            byte[] lengthBytes = new byte[4];

            int x = 0, y = 0;

            // Validate magic header first
            for (int i = 0; i < MAGIC.length * 8; i++) {
                int rgb = image.getRGB(x, y);
                int blue = rgb & 0xFF;
                int bit = blue & 1;
                byte expected = MAGIC[i / 8];
                int expectedBit = (expected >> (7 - (i % 8))) & 1;
                if (bit != expectedBit) {
                    return "❌ No hidden message found in this image.";
                }

                x++;
                if (x >= image.getWidth()) {
                    x = 0;
                    y++;
                }
            }

            // Read next 32 bits (4 bytes) for message length
            for (int i = 0; i < 32; i++) {
                int rgb = image.getRGB(x, y);
                int blue = rgb & 0xFF;
                int bit = blue & 1;
                lengthBytes[i / 8] = (byte) ((lengthBytes[i / 8] << 1) | bit);

                x++;
                if (x >= image.getWidth()) {
                    x = 0;
                    y++;
                }
            }

            messageLength = bytesToInt(lengthBytes);
            int remainingPixels = (image.getWidth() * image.getHeight()) - (MAGIC.length * 8) - 32;
            if (messageLength < 0 || messageLength * 8 > remainingPixels) {
                return "❌ Hidden message is missing or corrupted.";
            }

            byte[] messageBytes = new byte[messageLength];

            // Read the message bytes
            for (int i = 0; i < messageLength * 8; i++) {
                int rgb = image.getRGB(x, y);
                int blue = rgb & 0xFF;
                int bit = blue & 1;
                messageBytes[i / 8] = (byte) ((messageBytes[i / 8] << 1) | bit);

                x++;
                if (x >= image.getWidth()) {
                    x = 0;
                    y++;
                }
            }

            return new String(messageBytes);
        } catch (Exception e) {
            return "❌ Error decoding message: " + e.getMessage();
        }
    }

    private static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
               ((bytes[1] & 0xFF) << 16) |
               ((bytes[2] & 0xFF) << 8)  |
               (bytes[3] & 0xFF);
    }
}
    

