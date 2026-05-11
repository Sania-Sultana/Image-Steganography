import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class SplashScreen extends JWindow {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private int progress = 0;

    public SplashScreen() {
        setSize(600, 500);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        GradientPanel contentPane = new GradientPanel();
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel logoLabel = new JLabel("🔐");
        logoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 80));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Image Steganography");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Hide Secret Messages Inside Images");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(210, 220, 230));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(logoLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createVerticalStrut(60));

        progressBar = new JProgressBar(0, 100);
        progressBar.setMaximumSize(new Dimension(300, 8));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setForeground(new Color(46, 204, 113));
        progressBar.setBackground(new Color(60, 75, 100));
        progressBar.setStringPainted(false);
        progressBar.setBorderPainted(false);

        statusLabel = new JLabel("Loading...");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        statusLabel.setForeground(new Color(170, 180, 190));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(progressBar);
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(statusLabel);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 30, 40));

        JLabel versionLabel = new JLabel("Version 1.0 | Professional Edition");
        versionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        versionLabel.setForeground(new Color(100, 120, 140));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(versionLabel);

        contentPane.add(centerPanel, BorderLayout.CENTER);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);
    }

    public void startLoading() {
        setVisible(true);
        Timer timer = new Timer(30, e -> {
            progress += 2;
            if (progress <= 100) {
                progressBar.setValue(progress);
                updateStatusText(progress);
            }
        });
        timer.start();

        Timer dismissTimer = new Timer(6000, e -> {
            dispose();
            SwingUtilities.invokeLater(Main::createAndShowUi);
        });
        dismissTimer.setRepeats(false);
        dismissTimer.start();
    }

    private void updateStatusText(int percent) {
        if (percent < 30) {
            statusLabel.setText("Initializing UI...");
        } else if (percent < 60) {
            statusLabel.setText("Loading components...");
        } else if (percent < 90) {
            statusLabel.setText("Preparing interface...");
        } else {
            statusLabel.setText("Ready!");
        }
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint paint = new GradientPaint(0, 0, new Color(15, 23, 42), 0, getHeight(), new Color(30, 41, 59));
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
}
