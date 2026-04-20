package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * RankPanel — Màn hình Bảng xếp hạng.
 *
 * Hiển thị bục vinh quang Top 3 (dạng podium 3 bậc)
 * và danh sách cuộn các vị trí tiếp theo (hạng 4–8).
 *
 * Lưu ý: Dữ liệu hiện tại là dữ liệu mẫu cố định.
 * Có thể thay bằng dữ liệu thực từ server khi có backend.
 */
public class RankPanel extends JPanel {

    private final MainContainer parent;

    public RankPanel(MainContainer parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(NeonTheme.BACKGROUND);
        initUI();
    }

    private void initUI() {
        // ── Header ───────────────────────────────────────────────
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

        // Khoảng trống để căn giữa tiêu đề
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(50, 40));
        header.add(spacer, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Nội dung chính ────────────────────────────────────────
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Bục vinh quang Top 3 (thứ tự: Hạng 2 – Hạng 1 – Hạng 3)
        JPanel podiumSection = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        podiumSection.setOpaque(false);
        podiumSection.setPreferredSize(new Dimension(340, 230));
        podiumSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        podiumSection.setAlignmentX(Component.CENTER_ALIGNMENT);

        podiumSection.add(wrapPodiumWithOffset(createPodium("HyperNova",  "4,820", NeonTheme.CYAN,   2, 100), 50));
        podiumSection.add(wrapPodiumWithOffset(createPodium("NeonGod_99", "5,150", NeonTheme.YELLOW, 1, 100), 10));
        podiumSection.add(wrapPodiumWithOffset(createPodium("Vortex_X",   "4,500", NeonTheme.PINK,   3, 100), 65));

        centerPanel.add(podiumSection);
        centerPanel.add(Box.createVerticalStrut(12));

        // Danh sách hạng 4–8
        JPanel playerList = new JPanel();
        playerList.setLayout(new BoxLayout(playerList, BoxLayout.Y_AXIS));
        playerList.setOpaque(false);

        playerList.add(new RankRow(4, "ShadowWalker", "3,120", false));
        playerList.add(new RankRow(5, "PandaGamer",   "2,840", false));
        playerList.add(new RankRow(6, "CyberCat",     "2,100", false));
        playerList.add(new RankRow(7, "GlitchMaster", "1,950", false));
        playerList.add(new RankRow(8, "PixelPirate",  "1,720", false));

        JScrollPane scroll = new JScrollPane(playerList);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        applyNeonScrollBar(scroll);

        centerPanel.add(scroll);
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Tạo một cột bục vinh quang: vòng avatar + huy hiệu hạng + tên + điểm.
     */
    private JPanel createPodium(String name, String score, Color color, int rank, int width) {
        int podiumHeight = 180;
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();

                // Nền card kính mờ
                int cardTop = 45;
                g2d.setColor(new Color(NeonTheme.SURFACE.getRed(), NeonTheme.SURFACE.getGreen(),
                        NeonTheme.SURFACE.getBlue(), 220));
                g2d.fillRoundRect(0, cardTop, w, h - cardTop, 15, 15);
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.drawRoundRect(0, cardTop, w - 1, h - cardTop - 1, 15, 15);

                // Vòng tròn avatar
                int avatarSize = 60;
                int avatarX = (w - avatarSize) / 2;
                int avatarY = 0;
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
                g2d.fillOval(avatarX, avatarY, avatarSize, avatarSize);
                NeonTheme.drawGlow(g2d, new Ellipse2D.Float(avatarX, avatarY, avatarSize, avatarSize), color, 6);

                // Huy hiệu thứ hạng (góc trên phải của avatar)
                int badgeSize = 20;
                g2d.setColor(color);
                g2d.fillOval(avatarX + avatarSize - 10, avatarY + 2, badgeSize, badgeSize);
                g2d.setColor(NeonTheme.BACKGROUND);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                FontMetrics fmB = g2d.getFontMetrics();
                String rankStr = String.valueOf(rank);
                g2d.drawString(rankStr,
                        avatarX + avatarSize - 10 + (badgeSize - fmB.stringWidth(rankStr)) / 2,
                        avatarY + 2 + fmB.getAscent() + 1);

                // Tên người chơi
                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 10f));
                g2d.setColor(Color.WHITE);
                FontMetrics fmN = g2d.getFontMetrics();
                g2d.drawString(name, (w - fmN.stringWidth(name)) / 2, cardTop + 25);

                // Điểm số
                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
                g2d.setColor(color);
                FontMetrics fmS = g2d.getFontMetrics();
                g2d.drawString(score, (w - fmS.stringWidth(score)) / 2, cardTop + 47);
            }
        };
        p.setPreferredSize(new Dimension(width, podiumHeight));
        p.setMinimumSize(new Dimension(width, podiumHeight));
        p.setMaximumSize(new Dimension(width, podiumHeight));
        p.setOpaque(false);
        return p;
    }

    /**
     * Bọc bục trong một Box dọc với khoảng trống trên để tạo hiệu ứng bậc thang
     * (hạng 1 cao nhất, hạng 2 và hạng 3 thấp hơn lần lượt).
     */
    private JPanel wrapPodiumWithOffset(JPanel podium, int topOffset) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.add(Box.createVerticalStrut(topOffset));
        wrapper.add(podium);
        wrapper.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        return wrapper;
    }

    /**
     * Áp dụng thanh cuộn phong cách Neon (thumb gradient cyan→purple, track mờ).
     */
    private void applyNeonScrollBar(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = NeonTheme.CYAN;
                this.trackColor = new Color(10, 12, 50, 100);
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int x = r.x + 2, y = r.y, w = r.width - 4, h = r.height;
                g2d.setPaint(new GradientPaint(x, y, NeonTheme.CYAN, x, y + h, NeonTheme.PURPLE));
                g2d.fillRoundRect(x, y, w, h, 10, 10);
                g2d.setColor(new Color(NeonTheme.CYAN.getRed(), NeonTheme.CYAN.getGreen(), NeonTheme.CYAN.getBlue(), 100));
                g2d.drawRoundRect(x, y, w, h, 10, 10);
            }
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(255, 255, 255, 10));
                g2d.fillRoundRect(r.x + 3, r.y, r.width - 6, r.height, 10, 10);
            }
        });
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
    }

    // ── Lớp nội tuyến: hàng xếp hạng ────────────────────────────

    /**
     * RankRow — Một dòng trong danh sách xếp hạng (từ hạng 4 trở đi).
     * Nếu là người dùng hiện tại, dòng được tô nền vàng nhạt.
     */
    private class RankRow extends JPanel {
        public RankRow(int rank, String name, String score, boolean isCurrentUser) {
            setLayout(new BorderLayout());
            setOpaque(isCurrentUser);
            if (isCurrentUser) setBackground(new Color(243, 255, 202, 30));

            setPreferredSize(new Dimension(340, 60));
            setMaximumSize(new Dimension(500, 60));
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

            // Số thứ tự
            JLabel rankLbl = new JLabel(String.format("%02d", rank));
            rankLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(18f));
            rankLbl.setForeground(new Color(164, 167, 222));
            rankLbl.setPreferredSize(new Dimension(40, 44));
            add(rankLbl, BorderLayout.WEST);

            // Tên (và nhãn phụ nếu là người dùng hiện tại)
            JPanel center = new JPanel(new GridBagLayout());
            center.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0;

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

            // Điểm
            JLabel scoreLbl = new JLabel(score, SwingConstants.RIGHT);
            scoreLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 16f));
            scoreLbl.setForeground(isCurrentUser ? NeonTheme.YELLOW : NeonTheme.CYAN);
            add(scoreLbl, BorderLayout.EAST);
        }
    }
}
