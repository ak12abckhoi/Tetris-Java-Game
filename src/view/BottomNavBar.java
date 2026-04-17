package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BottomNavBar extends JPanel {
    private MainContainer parentContainer;
    private String activeScreen = "HOME";

    public BottomNavBar(MainContainer parentContainer) {
        this.parentContainer = parentContainer;
        setLayout(new GridLayout(1, 4, 0, 0));
        setBackground(NeonTheme.SURFACE);
        setOpaque(true);

        add(createNavButton("HOME", "Trang chủ"));
        add(createNavButton("RANK", "Xếp hạng"));
        add(createNavButton("STORE", "Cửa hàng"));
        add(createNavButton("PROFILE", "Hồ sơ"));
    }

    public void setActive(String screenName) {
        this.activeScreen = screenName;
        repaint();
    }

    private JPanel createNavButton(String id, String labelText) {
        JPanel btnPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                boolean isActive = id.equals(activeScreen);

                // Dynamic vertical centering
                int iconCy = h / 2 - 6;    // icon center, slightly above middle
                int textY  = h - 6;         // text near bottom with 6px padding

                if (isActive) {
                    // Glow background — 6px padding on each side, 4px top/bottom
                    g2d.setColor(new Color(0, 153, 204, 50));
                    g2d.fillRoundRect(6, 3, w - 12, h - 6, 16, 16);

                    // Neon border
                    g2d.setColor(NeonTheme.CYAN);
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawRoundRect(6, 3, w - 12, h - 6, 16, 16);

                    // Icon
                    drawNavIcon(g2d, id, w / 2, iconCy, true);

                    // Label
                    g2d.setColor(NeonTheme.CYAN);
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
                } else {
                    // Icon
                    drawNavIcon(g2d, id, w / 2, iconCy, false);

                    // Label
                    g2d.setColor(new Color(160, 160, 180));
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
                }

                FontMetrics fm = g2d.getFontMetrics();
                int textX = (w - fm.stringWidth(labelText)) / 2;
                g2d.drawString(labelText, textX, textY);
            }

            private void drawNavIcon(Graphics2D g2d, String id, int cx, int cy, boolean active) {
                g2d.setColor(active ? NeonTheme.CYAN : new Color(160, 160, 180));
                g2d.setStroke(new BasicStroke(1.5f));

                switch (id) {
                    case "HOME":
                        int[] xh = {cx - 7, cx, cx + 7, cx + 7, cx - 7};
                        int[] yh = {cy, cy - 7, cy, cy + 7, cy + 7};
                        g2d.drawPolygon(xh, yh, 5);
                        break;
                    case "RANK":
                        g2d.drawRect(cx - 8, cy, 4, 7);
                        g2d.drawRect(cx - 2, cy - 4, 4, 11);
                        g2d.drawRect(cx + 4, cy + 2, 4, 5);
                        break;
                    case "STORE":
                        g2d.drawRect(cx - 6, cy - 5, 12, 7);
                        g2d.fillOval(cx - 4, cy + 4, 3, 3);
                        g2d.fillOval(cx + 3, cy + 4, 3, 3);
                        break;
                    case "PROFILE":
                        g2d.drawOval(cx - 4, cy - 7, 8, 8);
                        g2d.drawArc(cx - 7, cy + 1, 14, 10, 0, 180);
                        break;
                }
            }
        };
        btnPanel.setOpaque(false);
        btnPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parentContainer.showScreen(id);
            }
        });
        return btnPanel;
    }
}
