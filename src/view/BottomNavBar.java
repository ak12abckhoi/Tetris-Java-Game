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
        setLayout(new GridLayout(1, 4));
        setBackground(new Color(13, 17, 76, 204)); // #0d114c with 80% opacity
        setPreferredSize(new Dimension(450, 70));
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

                boolean isActive = id.equals(activeScreen);

                if (isActive) {
                    // 1. The semi-transparent glow background box (Image 2 style)
                    g2d.setColor(new Color(0, 153, 204, 60)); // Cyan-ish glow
                    g2d.fillRoundRect(10, 8, getWidth() - 20, 50, 20, 20);
                    
                    // 2. Neon Border for active item
                    g2d.setColor(NeonTheme.CYAN);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(10, 8, getWidth() - 20, 50, 20, 20);
                    
                    // 3. Icon drawing
                    drawNavIcon(g2d, id, getWidth()/2, 28, true);
                    
                    // 4. Label
                    g2d.setColor(NeonTheme.CYAN);
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
                } else {
                    // Inactive Icon
                    drawNavIcon(g2d, id, getWidth()/2, 28, false);
                    
                    // Inactive Label
                    g2d.setColor(new Color(160, 160, 180));
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
                }
 
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(labelText)) / 2;
                g2d.drawString(labelText, textX, 70);
            }

            private void drawNavIcon(Graphics2D g2d, String id, int cx, int cy, boolean active) {
                g2d.setColor(active ? NeonTheme.CYAN : new Color(160, 160, 180));
                g2d.setStroke(new BasicStroke(2));
                
                switch(id) {
                    case "HOME":
                        // House shape
                        int[] xh = {cx-8, cx, cx+8, cx+8, cx-8};
                        int[] yh = {cy, cy-8, cy, cy+8, cy+8};
                        g2d.drawPolygon(xh, yh, 5);
                        break;
                    case "RANK":
                        // Bar chart shape
                        g2d.drawRect(cx-9, cy, 5, 8);
                        g2d.drawRect(cx-2, cy-4, 5, 12);
                        g2d.drawRect(cx+5, cy+2, 5, 6);
                        break;
                    case "STORE":
                        // Cart shape
                        g2d.drawPolyline(new int[]{cx-10, cx-6, cx+8}, new int[]{cy-6, cy-6, cy-6}, 3);
                        g2d.drawRect(cx-6, cy-6, 14, 8);
                        g2d.fillOval(cx-4, cy+4, 4, 4);
                        g2d.fillOval(cx+4, cy+4, 4, 4);
                        break;
                    case "PROFILE":
                        // Person shape
                        g2d.drawOval(cx-4, cy-8, 8, 8);
                        g2d.drawArc(cx-8, cy, 16, 12, 0, 180);
                        break;
                }
            }
        };
        btnPanel.setOpaque(false);
        btnPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!id.equals("PROFILE")) { // Assuming Profile might not be implemented yet
                    parentContainer.showScreen(id);
                }
            }
        });
        return btnPanel;
    }
}
