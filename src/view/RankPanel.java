package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class RankPanel extends JPanel {
    private MainContainer parent;

    public RankPanel(MainContainer parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(NeonTheme.BACKGROUND);
        initUI();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(12, 15, 0, 15));

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        backBtn.setForeground(Color.WHITE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> parent.showScreen("HOME"));
        header.add(backBtn, BorderLayout.WEST);

        JLabel titleStr = new JLabel("BẢNG XẾP HẠNG", SwingConstants.CENTER);
        titleStr.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 22f));
        titleStr.setForeground(NeonTheme.CYAN);
        header.add(titleStr, BorderLayout.CENTER);

        // Placeholder to balance the back button and center the title
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);
        placeholder.setPreferredSize(new Dimension(50, 40));
        header.add(placeholder, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Center Content: Podiums + List
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // 1. Podiums Section — Restored to absolute layout (null) to keep them 'liền nhau'
        JPanel podiumSection = new JPanel(null);
        podiumSection.setOpaque(false);
        podiumSection.setPreferredSize(new Dimension(340, 230)); // Increased height
        podiumSection.setMaximumSize(new Dimension(400, 230));

        // Restore original overlapping/contiguous positions
        podiumSection.add(createPodium("HyperNova", "4,820", NeonTheme.CYAN, 2, 10, 75, 100));   // 2nd
        podiumSection.add(createPodium("NeonGod_99", "5,150", NeonTheme.YELLOW, 1, 120, 30, 100)); // 1st
        podiumSection.add(createPodium("Vortex_X", "4,500", NeonTheme.PINK, 3, 230, 85, 100));    // 3rd

        centerPanel.add(podiumSection);
        centerPanel.add(Box.createVerticalStrut(12));

        // 2. Scrollable List
        JPanel playerList = new JPanel();
        playerList.setLayout(new BoxLayout(playerList, BoxLayout.Y_AXIS));
        playerList.setOpaque(false);

        // Mock data
        playerList.add(new RankRow(4, "ShadowWalker", "3,120", false));
        playerList.add(new RankRow(5, "PandaGamer", "2,840", false));
        playerList.add(new RankRow(6, "CyberCat", "2,100", false));
        playerList.add(new RankRow(7, "GlitchMaster", "1,950", false));
        playerList.add(new RankRow(8, "PixelPirate", "1,720", false));

        JScrollPane scroll = new JScrollPane(playerList);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Custom sleek neon scrollbar
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = NeonTheme.CYAN;
                this.trackColor = new Color(10, 12, 50, 100);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                return btn;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Slimmer thumb
                int x = thumbBounds.x + 2;
                int y = thumbBounds.y;
                int width = thumbBounds.width - 4;
                int height = thumbBounds.height;

                GradientPaint gp = new GradientPaint(x, y, NeonTheme.CYAN, x, y + height, NeonTheme.PURPLE);
                g2d.setPaint(gp);
                g2d.fillRoundRect(x, y, width, height, 10, 10);

                // Subtle glow on thumb
                g2d.setColor(
                        new Color(NeonTheme.CYAN.getRed(), NeonTheme.CYAN.getGreen(), NeonTheme.CYAN.getBlue(), 100));
                g2d.drawRoundRect(x, y, width, height, 10, 10);
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(255, 255, 255, 10)); // Very subtle track
                g2d.fillRoundRect(trackBounds.x + 3, trackBounds.y, trackBounds.width - 6, trackBounds.height, 10, 10);
            }
        });
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        centerPanel.add(scroll);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createPodium(String name, String score, Color color, int rank, int x, int y, int width) {
        int podiumHeight = 180; // Standardized height for the card
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Background card with glassmorphism
                int cardTop = 45;
                g2d.setColor(new Color(NeonTheme.SURFACE.getRed(), NeonTheme.SURFACE.getGreen(),
                        NeonTheme.SURFACE.getBlue(), 220));
                g2d.fillRoundRect(0, cardTop, w, h - cardTop, 15, 15);
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.drawRoundRect(0, cardTop, w - 1, h - cardTop - 1, 15, 15);

                // Avatar circle
                int avatarSize = 60;
                int avatarX = (w - avatarSize) / 2;
                int avatarY = 0;
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
                g2d.fillOval(avatarX, avatarY, avatarSize, avatarSize);

                // Glowing border
                NeonTheme.drawGlow(g2d, new Ellipse2D.Float(avatarX, avatarY, avatarSize, avatarSize), color, 6);

                // Rank badge
                int badgeSize = 20;
                g2d.setColor(color);
                g2d.fillOval(avatarX + avatarSize - 10, avatarY + 2, badgeSize, badgeSize);
                g2d.setColor(NeonTheme.BACKGROUND);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                FontMetrics fmBadge = g2d.getFontMetrics();
                String rankStr = String.valueOf(rank);
                g2d.drawString(rankStr,
                        avatarX + avatarSize - 10 + (badgeSize - fmBadge.stringWidth(rankStr)) / 2,
                        avatarY + 2 + fmBadge.getAscent() + 1);

                // Draw Name
                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 10f));
                g2d.setColor(Color.WHITE);
                FontMetrics fmName = g2d.getFontMetrics();
                int nameY = cardTop + 25;
                g2d.drawString(name, (w - fmName.stringWidth(name)) / 2, nameY);

                // Draw Score
                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
                g2d.setColor(color);
                FontMetrics fmScore = g2d.getFontMetrics();
                int scoreY = nameY + 22;
                g2d.drawString(score, (w - fmScore.stringWidth(score)) / 2, scoreY);
            }
        };

        p.setBounds(x, y, width, podiumHeight);
        p.setOpaque(false);
        return p;
    }

    private class RankRow extends JPanel {
        public RankRow(int rank, String name, String score, boolean isCurrentUser) {
            setLayout(new BorderLayout());
            if (isCurrentUser) {
                setOpaque(true);
                setBackground(new Color(243, 255, 202, 30));
            } else {
                setOpaque(false);
            }

            setPreferredSize(new Dimension(340, 60));
            setMaximumSize(new Dimension(500, 60));
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

            JLabel rankLbl = new JLabel(String.format("%02d", rank));
            rankLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(18f));
            rankLbl.setForeground(new Color(164, 167, 222));
            rankLbl.setPreferredSize(new Dimension(40, 44));
            add(rankLbl, BorderLayout.WEST);

            JPanel center = new JPanel(new GridBagLayout());
            center.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0;

            JLabel nameLbl = new JLabel(name);
            nameLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(isCurrentUser ? Font.BOLD : Font.PLAIN, 15f));
            nameLbl.setForeground(isCurrentUser ? NeonTheme.YELLOW : Color.WHITE);
            center.add(nameLbl, gbc);

            if (isCurrentUser) {
                gbc.gridy = 1;
                JLabel subText = new JLabel("THÀNH TÍCH CÁ NHÂN");
                subText.setFont(new Font("SansSerif", Font.PLAIN, 9));
                subText.setForeground(NeonTheme.YELLOW);
                center.add(subText, gbc);
            }
            add(center, BorderLayout.CENTER);

            JLabel scoreLbl = new JLabel(score, SwingConstants.RIGHT);
            scoreLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 16f));
            scoreLbl.setForeground(isCurrentUser ? NeonTheme.YELLOW : NeonTheme.CYAN);
            add(scoreLbl, BorderLayout.EAST);
        }
    }
}
