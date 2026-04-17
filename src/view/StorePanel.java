package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * StorePanel — Giao diện cửa hàng neon responsive.
 * Mọi kích thước tính tương đối, tự động co giãn theo width.
 */
public class StorePanel extends JPanel {
    private MainContainer parent;
    private String selectedCategory = "THEMES";
    private int coinBalance = 2500;

    public StorePanel(MainContainer parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(NeonTheme.BACKGROUND);
        initUI();
    }

    private void initUI() {
        // ═══ HEADER ═══
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));

        // Use a wrapper for back button to match the coin panel width (center the title perfectly)
        JPanel backWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backWrapper.setOpaque(false);
        backWrapper.setPreferredSize(new Dimension(95, 26));

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        backBtn.setForeground(Color.WHITE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> parent.showScreen("HOME"));
        backWrapper.add(backBtn);
        header.add(backWrapper, BorderLayout.WEST);

        JLabel titleLbl = new JLabel("CỬA HÀNG", SwingConstants.CENTER);
        titleLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 22f));
        titleLbl.setForeground(NeonTheme.CYAN);
        header.add(titleLbl, BorderLayout.CENTER);

        // Coin display
        JPanel coinPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                int w = getWidth(), h = getHeight();
                
                // Background
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(0, 0, w, h, 16, 16);
                g2d.setColor(NeonTheme.YELLOW);
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

                // Coin Icon
                int iconSize = 16;
                int iconX = 6;
                int iconY = (h - iconSize) / 2;
                g2d.setColor(NeonTheme.YELLOW);
                g2d.fillOval(iconX, iconY, iconSize, iconSize);
                
                g2d.setColor(NeonTheme.BACKGROUND);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
                FontMetrics fmIcon = g2d.getFontMetrics();
                String dol = "$";
                g2d.drawString(dol, iconX + (iconSize - fmIcon.stringWidth(dol)) / 2, iconY + fmIcon.getAscent() + 1);

                // Balance
                g2d.setColor(NeonTheme.YELLOW);
                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 12f));
                FontMetrics fmBal = g2d.getFontMetrics();
                String bal = String.format("%,d", coinBalance);
                // Center slightly towards the right side of the icon
                g2d.drawString(bal, iconX + iconSize + 6, (h + fmBal.getAscent()) / 2 - 2);
            }
        };
        coinPanel.setOpaque(false);
        coinPanel.setPreferredSize(new Dimension(95, 26));
        header.add(coinPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ═══ CENTER ═══
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));

        // Category tabs
        JPanel categoryPanel = createCategoryTabs();
        centerWrapper.add(categoryPanel, BorderLayout.NORTH);

        // Items
        JPanel itemsGrid = createItemsGrid();
        JScrollPane scroll = new JScrollPane(itemsGrid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        centerWrapper.add(scroll, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createCategoryTabs() {
        JPanel tabs = new JPanel(new GridLayout(1, 3, 6, 0));
        tabs.setOpaque(false);
        tabs.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 0));
        tabs.setPreferredSize(new Dimension(0, 34));

        tabs.add(createCategoryTab("THEMES", "Giao diện", NeonTheme.CYAN));
        tabs.add(createCategoryTab("BLOCKS", "Khối", NeonTheme.PINK));
        tabs.add(createCategoryTab("EFFECTS", "Hiệu ứng", NeonTheme.LIME));
        return tabs;
    }

    private JPanel createCategoryTab(String id, String label, Color color) {
        JPanel tab = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                boolean active = id.equals(selectedCategory);

                if (active) {
                    g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
                    g2d.fillRoundRect(0, 0, w, h, 12, 12);
                    g2d.setColor(color);
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
                } else {
                    g2d.setColor(new Color(255, 255, 255, 10));
                    g2d.fillRoundRect(0, 0, w, h, 12, 12);
                }

                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(active ? Font.BOLD : Font.PLAIN, 11f));
                g2d.setColor(active ? color : new Color(160, 160, 180));
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(label, (w - fm.stringWidth(label)) / 2, (h + fm.getAscent()) / 2 - 3);
            }
        };
        tab.setOpaque(false);
        tab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tab.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedCategory = id;
                StorePanel.this.repaint();
            }
        });
        return tab;
    }

    private JPanel createItemsGrid() {
        JPanel grid = new JPanel();
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));
        grid.setOpaque(false);

        // Row 1
        JPanel row1 = new JPanel(new GridLayout(1, 2, 8, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        row1.setPreferredSize(new Dimension(0, 170));
        row1.add(createStoreItem("Neon Sunset", "Gradient cam hồng rực rỡ", 500, NeonTheme.ORANGE, false, true));
        row1.add(createStoreItem("Cyber Galaxy", "Tím neon huyền bí", 750, NeonTheme.PURPLE, false, false));
        grid.add(row1);
        grid.add(Box.createVerticalStrut(8));

        JPanel row2 = new JPanel(new GridLayout(1, 2, 8, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        row2.setPreferredSize(new Dimension(0, 170));
        row2.add(createStoreItem("Aurora Borealis", "Ánh sáng cực quang", 1200, NeonTheme.LIME, false, false));
        row2.add(createStoreItem("Classic Neon", "Neon xanh cổ điển", 0, NeonTheme.CYAN, true, false));
        grid.add(row2);
        grid.add(Box.createVerticalStrut(8));

        JPanel row3 = new JPanel(new GridLayout(1, 2, 8, 0));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        row3.setPreferredSize(new Dimension(0, 170));
        row3.add(createStoreItem("Diamond Block", "Khối lấp lánh kim cương", 900, NeonTheme.YELLOW, false, false));
        row3.add(createStoreItem("Fire Trail", "Vệt lửa khi xóa hàng", 650, NeonTheme.PINK, false, false));
        grid.add(row3);
        grid.add(Box.createVerticalStrut(8));

        return grid;
    }

    private JPanel createStoreItem(String name, String desc, int price, Color color,
            boolean owned, boolean equipped) {
        JPanel card = new JPanel() {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Card bg
                g2d.setColor(new Color(255, 255, 255, hovered ? 16 : 8));
                g2d.fillRoundRect(0, 0, w, h, 16, 16);

                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), hovered ? 170 : 50));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

                if (hovered) {
                    NeonTheme.drawGlow(g2d, new RoundRectangle2D.Float(0, 0, w, h, 16, 16), color, 3);
                }

                // Preview area — relative
                int previewH = h / 3;
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 25));
                g2d.fillRoundRect(8, 8, w - 16, previewH, 10, 10);

                // Tetris block preview
                int bs = Math.max(7, w / 14);
                int px = w / 2 - bs;
                int py = 8 + previewH / 2 - bs;
                g2d.setColor(color);
                g2d.fillRoundRect(px, py, bs, bs, 2, 2);
                g2d.fillRoundRect(px, py + bs + 1, bs, bs, 2, 2);
                g2d.fillRoundRect(px + bs + 1, py + bs + 1, bs, bs, 2, 2);
                g2d.fillRoundRect(px + (bs + 1) * 2, py + bs + 1, bs, bs, 2, 2);

                // Name
                int nameY = 8 + previewH + 16;
                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 11f));
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                String displayName = name;
                if (fm.stringWidth(displayName) > w - 16) {
                    displayName = displayName.substring(0, Math.min(displayName.length(), 12)) + "..";
                }
                g2d.drawString(displayName, 8, nameY);

                // Desc
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2d.setColor(new Color(160, 160, 180));
                fm = g2d.getFontMetrics();
                String displayDesc = desc;
                if (fm.stringWidth(displayDesc) > w - 16) {
                    int cut = 0;
                    while (cut < displayDesc.length() && fm.stringWidth(displayDesc.substring(0, cut)) < w - 30)
                        cut++;
                    displayDesc = displayDesc.substring(0, Math.max(1, cut - 1)) + "..";
                }
                g2d.drawString(displayDesc, 8, nameY + 14);

                // Button — at bottom
                int btnY = h - 30;
                int btnW = w - 16;
                int btnH = 24;

                if (equipped) {
                    g2d.setColor(NeonTheme.LIME);
                    g2d.fillRoundRect(8, btnY, btnW, btnH, 10, 10);
                    g2d.setColor(NeonTheme.BACKGROUND);
                    g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 10f));
                    fm = g2d.getFontMetrics();
                    g2d.drawString("✓ ĐANG DÙNG", 8 + (btnW - fm.stringWidth("✓ ĐANG DÙNG")) / 2, btnY + 16);
                } else if (owned) {
                    g2d.setColor(new Color(255, 255, 255, 20));
                    g2d.fillRoundRect(8, btnY, btnW, btnH, 10, 10);
                    g2d.setColor(NeonTheme.CYAN);
                    g2d.drawRoundRect(8, btnY, btnW, btnH, 10, 10);
                    g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 10f));
                    fm = g2d.getFontMetrics();
                    g2d.drawString("SỬ DỤNG", 8 + (btnW - fm.stringWidth("SỬ DỤNG")) / 2, btnY + 16);
                } else {
                    g2d.setColor(NeonTheme.YELLOW);
                    g2d.fillRoundRect(8, btnY, btnW, btnH, 10, 10);
                    g2d.setColor(NeonTheme.BACKGROUND);
                    g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 10f));
                    fm = g2d.getFontMetrics();
                    String priceStr = "$ " + String.format("%,d", price);
                    g2d.drawString(priceStr, 8 + (btnW - fm.stringWidth(priceStr)) / 2, btnY + 16);
                }
            }
        };
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return card;
    }
}
