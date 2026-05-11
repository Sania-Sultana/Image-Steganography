import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();
            splash.startLoading();
        });
    }

    public static void createAndShowUi() {
        JFrame frame = new JFrame("Image Steganography");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 860);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);

        GradientPanel root = new GradientPanel(new Color(10, 18, 35), new Color(25, 35, 55));
        root.setLayout(new BorderLayout(0, 0));
        root.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel headerContent = new JPanel();
        headerContent.setOpaque(false);
        headerContent.setLayout(new BorderLayout(20, 0));

        JPanel titleSection = new JPanel();
        titleSection.setOpaque(false);
        titleSection.setLayout(new BoxLayout(titleSection, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("🔐 Image Steganography");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 38));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Secure message hiding and retrieval system");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        subtitle.setForeground(new Color(180, 190, 200));

        titleSection.add(title);
        titleSection.add(Box.createVerticalStrut(8));
        titleSection.add(subtitle);

        JPanel badgePanel = new JPanel();
        badgePanel.setOpaque(false);
        JLabel statusBadge = new JLabel("● Ready");
        statusBadge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        statusBadge.setForeground(new Color(76, 220, 97));
        badgePanel.add(statusBadge);

        headerContent.add(titleSection, BorderLayout.WEST);
        headerContent.add(badgePanel, BorderLayout.EAST);
        headerPanel.add(headerContent, BorderLayout.CENTER);
        root.add(headerPanel, BorderLayout.NORTH);

        JTextArea messageArea = new JTextArea(8, 30);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        messageArea.setBorder(new EmptyBorder(14, 14, 14, 14));
        messageArea.setBackground(Color.WHITE);
        messageArea.setForeground(new Color(25, 35, 55));
        messageArea.setCaretColor(new Color(52, 152, 219));

        JTextArea resultArea = new JTextArea(10, 30);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        resultArea.setBorder(new EmptyBorder(14, 14, 14, 14));
        resultArea.setBackground(Color.WHITE);
        resultArea.setForeground(new Color(40, 50, 70));

        JLabel selectedEncodeField = new JLabel("No image selected");
        selectedEncodeField.setForeground(new Color(62, 80, 96));
        selectedEncodeField.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel selectedDecodeField = new JLabel("No stego image selected");
        selectedDecodeField.setForeground(new Color(62, 80, 96));
        selectedDecodeField.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel statusLabel = new JLabel("Choose an image to start.");
        statusLabel.setForeground(new Color(90, 100, 110));
        statusLabel.setBorder(new EmptyBorder(8, 2, 0, 2));

        JPasswordField encodePasswordField = new JPasswordField();
        encodePasswordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        encodePasswordField.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPasswordField decodePasswordField = new JPasswordField();
        decodePasswordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        decodePasswordField.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel encodePreview = createPreviewPlaceholder("Image preview");
        JLabel decodePreview = createPreviewPlaceholder("Image preview");

        JButton browseEncodeButton = createAccentButton("Select image", new Color(52, 152, 219));
        JButton encodeButton = createAccentButton("Encode and Save", new Color(46, 204, 113));
        JButton browseDecodeButton = createAccentButton("Select stego image", new Color(155, 89, 182));
        JButton decodeButton = createAccentButton("Decode Message", new Color(230, 126, 34));

        final File[] encodeFile = {null};
        final File[] decodeFile = {null};

        browseEncodeButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select image to hide message in");
            chooser.setFileFilter(new FileNameExtensionFilter("Image files", "png", "jpg", "jpeg"));
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                encodeFile[0] = chooser.getSelectedFile();
                selectedEncodeField.setText(encodeFile[0].getAbsolutePath());
                setPreview(encodePreview, encodeFile[0]);
                setStatus(statusLabel, statusBadge, "Image selected for encoding.", new Color(52, 152, 219));
            }
        });

        encodeButton.addActionListener(e -> {
            if (encodeFile[0] == null) {
                JOptionPane.showMessageDialog(frame, "Please upload an image first.", "Missing image", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String message = messageArea.getText().trim();
            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please type a secret message.", "Missing message", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser saver = new JFileChooser();
            saver.setDialogTitle("Save encoded image as PNG");
            saver.setSelectedFile(new File("stego.png"));
            if (saver.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File outputFile = saver.getSelectedFile();
            if (!outputFile.getName().toLowerCase().endsWith(".png")) {
                outputFile = new File(outputFile.getParentFile(), outputFile.getName() + ".png");
            }

            String encodePassword = new String(encodePasswordField.getPassword()).trim();
            boolean success = SteganographyEncoder.encode(
                encodeFile[0].getAbsolutePath(),
                outputFile.getAbsolutePath(),
                message,
                encodePassword.isEmpty() ? null : encodePassword
            );
            if (success) {
                setStatus(statusLabel, statusBadge, "Message encoded successfully.", new Color(46, 204, 113));
                JOptionPane.showMessageDialog(frame, "Encoded image saved: " + outputFile.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                setStatus(statusLabel, statusBadge, "Encoding failed.", new Color(231, 76, 60));
                JOptionPane.showMessageDialog(frame, "Encoding failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        browseDecodeButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select stego image to decode");
            chooser.setFileFilter(new FileNameExtensionFilter("Image files", "png", "jpg", "jpeg"));
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                decodeFile[0] = chooser.getSelectedFile();
                selectedDecodeField.setText(decodeFile[0].getAbsolutePath());
                setPreview(decodePreview, decodeFile[0]);
                setStatus(statusLabel, statusBadge, "Stego image selected for decoding.", new Color(155, 89, 182));
            }
        });

        decodeButton.addActionListener(e -> {
            if (decodeFile[0] == null) {
                JOptionPane.showMessageDialog(frame, "Please upload a stego image first.", "Missing image", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String decodePassword = new String(decodePasswordField.getPassword()).trim();
            String message = SteganographyDecoder.decode(
                decodeFile[0].getAbsolutePath(),
                decodePassword.isEmpty() ? null : decodePassword
            );
            resultArea.setText(message);
            setStatus(statusLabel, statusBadge, "Message decoded.", new Color(230, 126, 34));
        });

        JPanel center = new JPanel(new GridLayout(1, 2, 20, 20));
        center.setOpaque(false);

        JPanel encodePanel = createCard();
        encodePanel.setLayout(new BorderLayout(14, 14));
        encodePanel.add(createCardHeader("Encode", "Hide a message in an image"), BorderLayout.NORTH);
        encodePanel.add(buildEncodeBody(browseEncodeButton, selectedEncodeField, encodePreview, messageArea, encodePasswordField, encodeButton), BorderLayout.CENTER);

        JPanel decodePanel = createCard();
        decodePanel.setLayout(new BorderLayout(14, 14));
        decodePanel.add(createCardHeader("Decode", "Extract the hidden message"), BorderLayout.NORTH);
        decodePanel.add(buildDecodeBody(browseDecodeButton, selectedDecodeField, decodePreview, decodePasswordField, decodeButton, resultArea), BorderLayout.CENTER);

        center.add(encodePanel);
        center.add(decodePanel);

        root.add(center, BorderLayout.CENTER);
        root.add(statusLabel, BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.setVisible(true);
    }

    private static JPanel buildEncodeBody(JButton browseButton, JLabel selectedField, JLabel preview, JTextArea messageArea, JPasswordField passwordField, JButton encodeButton) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(browseButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(selectedField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(preview);
        panel.add(Box.createVerticalStrut(12));

        JLabel label = new JLabel("Secret message");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setForeground(new Color(31, 41, 55));
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));
        panel.add(wrapScroll(messageArea));
        panel.add(Box.createVerticalStrut(10));

        JLabel passwordLabel = new JLabel("Password (optional)");
        passwordLabel.setFont(passwordLabel.getFont().deriveFont(Font.BOLD, 14f));
        passwordLabel.setForeground(new Color(31, 41, 55));
        panel.add(passwordLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(wrapField(passwordField));
        panel.add(Box.createVerticalStrut(12));
        panel.add(encodeButton);
        return panel;
    }

    private static JPanel buildDecodeBody(JButton browseButton, JLabel selectedField, JLabel preview, JPasswordField passwordField, JButton decodeButton, JTextArea resultArea) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(browseButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(selectedField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(preview);
        panel.add(Box.createVerticalStrut(12));

        JLabel label = new JLabel("Decoded message");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setForeground(new Color(31, 41, 55));
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));
        panel.add(wrapScroll(resultArea));
        panel.add(Box.createVerticalStrut(10));

        JLabel passwordLabel = new JLabel("Password (if encrypted)");
        passwordLabel.setFont(passwordLabel.getFont().deriveFont(Font.BOLD, 14f));
        passwordLabel.setForeground(new Color(31, 41, 55));
        panel.add(passwordLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(wrapField(passwordField));
        panel.add(Box.createVerticalStrut(12));
        panel.add(decodeButton);
        return panel;
    }

    private static JScrollPane wrapScroll(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 235), 2));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private static JPanel wrapField(JComponent component) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 235), 2));
        component.setPreferredSize(new Dimension(0, 40));
        panel.add(component, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return panel;
    }

    private static JPanel createCard() {
        JPanel card = new ShadowPanel();
        card.setOpaque(true);
        card.setBackground(new Color(240, 243, 248));
        card.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        return card;
    }

    private static JPanel createCardHeader(String title, String subtitle) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        titleLabel.setForeground(new Color(15, 25, 45));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 115, 135));

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitleLabel);
        return header;
    }

    private static JButton createAccentButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(Color.BLACK);
        button.setBackground(baseColor);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        button.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(brighter(baseColor, 0.15f));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor);
            }
        });
        return button;
    }

    private static Color brighter(Color c, float factor) {
        int r = (int) Math.min(255, c.getRed() + (255 - c.getRed()) * factor);
        int g = (int) Math.min(255, c.getGreen() + (255 - c.getGreen()) * factor);
        int b = (int) Math.min(255, c.getBlue() + (255 - c.getBlue()) * factor);
        return new Color(r, g, b);
    }

    private static JLabel createPreviewPlaceholder(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(0, 190));
        label.setMinimumSize(new Dimension(0, 190));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setForeground(new Color(120, 130, 140));
        label.setBorder(BorderFactory.createDashedBorder(new Color(194, 201, 209), 6, 4));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static void setPreview(JLabel previewLabel, File file) {
        try {
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            Image image = icon.getImage();
            int width = 360;
            int height = 190;
            Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            previewLabel.setIcon(new ImageIcon(scaled));
            previewLabel.setText("");
        } catch (Exception ex) {
            previewLabel.setIcon(null);
            previewLabel.setText("Preview unavailable");
        }
    }

    private static void setStatus(JLabel statusLabel, JLabel statusBadge, String text, Color color) {
        statusLabel.setText(text);
        statusBadge.setText(text);
        statusBadge.setBackground(color);
    }

    private static class GradientPanel extends JPanel {
        private Color startColor;
        private Color endColor;

        public GradientPanel(Color start, Color end) {
            this.startColor = start;
            this.endColor = end;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint paint = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
            g2.setPaint(paint);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 0, 0));
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }

    private static class ShadowPanel extends JPanel {
        private static final int SHADOW_SIZE = 8;

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw shadow
            for (int i = SHADOW_SIZE; i >= 1; i--) {
                float opacity = 0.05f * (SHADOW_SIZE - i + 1);
                g2.setColor(new Color(0, 0, 0, (int) (opacity * 255)));
                int inset = SHADOW_SIZE - i;
                g2.fillRoundRect(inset, inset, getWidth() - inset * 2, getHeight() - inset * 2, 12, 12);
            }

            // Draw card background
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public Insets getInsets() {
            return new Insets(SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE);
        }
    }
}
