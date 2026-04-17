package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * HomePanel represents the main menu screen of the game.
 * It features a custom-drawn neon logo and a specialized 3D button.
 */
public class HomePanel extends JPanel {
    private MainContainer parent;

    public HomePanel(MainContainer parent) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        setBackground(NeonTheme.BACKGROUND);
        initUI();
    }

    private void initUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Space for Logo — reduced for 400x650 window
        gbc.gridy = 0;
        gbc.weighty = 0.25;
        add(Box.createVerticalStrut(120), gbc);

        // Buttons — reduced padding & height
        gbc.weighty = 0;
        gbc.insets = new Insets(6, 30, 6, 30);

        gbc.gridy = 1;
        add(createMainMenuButton("CHƠI NGAY", NeonTheme.LIME, e -> parent.showScreen("MODE")), gbc);

        gbc.gridy = 2;
        add(createMainMenuButton("BẢNG XẾP HẠNG", NeonTheme.SURFACE.brighter(), e -> parent.showScreen("RANK")), gbc);

        gbc.gridy = 3;
        add(createMainMenuButton("ĐIỂM CAO NHẤT", NeonTheme.PURPLE, e -> parent.showScreen("HIGHSCORE")), gbc);

        gbc.gridy = 4;
        add(createMainMenuButton("CÀI ĐẶT", new Color(160, 160, 160), e -> parent.showScreen("SETTINGS")), gbc);

        // Footer padding
        gbc.gridy = 5;
        gbc.weighty = 0.3;
        add(Box.createVerticalStrut(10), gbc);
    }

    private JButton createMainMenuButton(String text, Color color, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                
                // Draw simple glassmorphism bg
                g2d.setColor(new Color(255, 255, 255, hovered ? 30 : 15));
                g2d.fillRoundRect(0, 0, w, h, 18, 18);
                
                // Neon border
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), hovered ? 255 : 150));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, w-2, h-2, 18, 18);

                // Glow if hovered
                if (hovered) {
                    NeonTheme.drawGlow(g2d, new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, 18, 18), color, 5);
                }

                // Text
                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 16f));
                FontMetrics fm = g2d.getFontMetrics();
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h + fm.getAscent()) / 2 - 3);
            }
        };
        btn.setPreferredSize(new Dimension(280, 48));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (listener != null) btn.addActionListener(listener);
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawLogo(g2d);
    }

    private void drawLogo(Graphics2D g2d) {
        String title = "TETRIS";
        Color[] colors = {
            NeonTheme.PINK,
            NeonTheme.CYAN,
            NeonTheme.YELLOW,
            NeonTheme.LIME,
            NeonTheme.PURPLE,
            NeonTheme.ORANGE
        };

        // Use a slightly smaller logo font that fits the reduced window
        Font font = NeonTheme.LOGO_FONT.deriveFont(NeonTheme.LOGO_FONT.getSize2D() * 0.75f);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(title)) / 2;
        int y = 120; // Reduced from 180 to fit smaller window

        for (int i = 0; i < title.length(); i++) {
            String letter = String.valueOf(title.charAt(i));
            Color c = colors[i % colors.length];

            // Draw Glow
            g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
            g2d.drawString(letter, x + 2, y + 2);
            g2d.drawString(letter, x - 2, y - 2);

            // Draw Main Letter
            g2d.setColor(c);
            g2d.drawString(letter, x, y);
            
            x += fm.charWidth(title.charAt(i));
        }

        // Add a subtle underline glow
        g2d.setPaint(new GradientPaint(
            getWidth()/4, y + 8, new Color(NeonTheme.CYAN.getRed(), NeonTheme.CYAN.getGreen(), NeonTheme.CYAN.getBlue(), 0),
            getWidth()/2, y + 8, NeonTheme.CYAN,
            true
        ));
        g2d.fillRect(getWidth()/4, y + 12, getWidth()/2, 2);
    }

}
