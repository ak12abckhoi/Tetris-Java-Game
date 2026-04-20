package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * ModeSelectionPanel — Màn hình chọn chế độ chơi.
 *
 * Hiển thị hai thẻ chế độ:
 *   - "Chơi với máy": đối kháng AI, vào BattlePanel
 *   - "Chơi đơn"    : chế độ cổ điển, vào GamePanel
 */
public class ModeSelectionPanel extends JPanel {

    private final MainContainer parent;

    public ModeSelectionPanel(MainContainer parent) {
        this.parent = parent;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(NeonTheme.BACKGROUND);

        // ── Header ───────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(12, 15, 0, 15));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        backBtn.setForeground(Color.WHITE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> parent.showScreen("HOME"));
        header.add(backBtn, BorderLayout.WEST);

        JLabel title = new JLabel("CHỌN CHẾ ĐỘ CHƠI", SwingConstants.CENTER);
        title.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 22f));
        title.setForeground(NeonTheme.CYAN);
        header.add(title, BorderLayout.CENTER);

        // Khoảng trống bên phải để cân bằng tiêu đề
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(50, 40));
        header.add(spacer, BorderLayout.EAST);

        add(header);
        add(Box.createVerticalStrut(5));

        // ── Đường kẻ gradient bên dưới tiêu đề ──────────────────
        JPanel underline = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                    0, 0,
                    new Color(NeonTheme.YELLOW.getRed(), NeonTheme.YELLOW.getGreen(), NeonTheme.YELLOW.getBlue(), 0),
                    getWidth() / 2, 0, NeonTheme.YELLOW,
                    true);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), 3);
            }
        };
        underline.setPreferredSize(new Dimension(180, 3));
        underline.setMaximumSize(new Dimension(180, 3));
        underline.setOpaque(false);
        add(underline);
        add(Box.createVerticalStrut(25));

        // ── Thẻ chế độ 1: Chơi với AI ────────────────────────────
        add(new ModeCard(
            "CHƠI VỚI MÁY",
            "Thử thách kỹ năng của bạn với AI thông minh trong những ván đấu kịch tính",
            NeonTheme.CYAN, "BOT",
            () -> parent.showBattle()
        ));
        add(Box.createVerticalStrut(30));

        // ── Thẻ chế độ 2: Chơi đơn ───────────────────────────────
        add(new ModeCard(
            "CHƠI ĐƠN",
            "Phá vỡ kỷ lục điểm số của chính mình trong chế độ chơi cổ điển không giới hạn",
            NeonTheme.PINK, "GUEST",
            () -> parent.showScreen("GAME")
        ));
    }

    // ── Lớp nội tuyến ModeCard ───────────────────────────────────

    /**
     * ModeCard — Thẻ hiển thị thông tin một chế độ chơi.
     *
     * Gồm: biểu tượng, tiêu đề, mô tả và nút "BẮT ĐẦU".
     */
    private class ModeCard extends JPanel {

        public ModeCard(String title, String desc, Color themeColor, String iconType, Runnable onStartAction) {
            setLayout(null);
            setPreferredSize(new Dimension(380, 200));
            setMaximumSize(new Dimension(380, 200));
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            Color bgDark = new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 30);

            // Nền card và viền dưới phát sáng
            JPanel bgPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(bgDark);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                    // Đường viền dưới phát sáng
                    g2d.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 150));
                    g2d.fillRoundRect(0, getHeight() - 8, getWidth(), 8, 30, 30);
                }
            };
            bgPanel.setBounds(0, 0, 380, 200);
            bgPanel.setOpaque(false);
            bgPanel.setLayout(new BorderLayout());

            // ── Nội dung bên trong ───────────────────────────────
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);
            content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Biểu tượng + tiêu đề
            JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            headerRow.setOpaque(false);

            JPanel iconBox = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(themeColor);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    NeonTheme.drawGlow(g2d,
                        new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15),
                        themeColor, 10);

                    // Vẽ biểu tượng lên trên nền màu
                    g2d.setColor(NeonTheme.BACKGROUND);
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;

                    if ("BOT".equals(iconType)) {
                        // Đầu robot: thân + mắt + miệng + ăng-ten
                        int hw = 20, hh = 16;
                        int hx = cx - hw / 2, hy = cy - hh / 2 + 2;
                        g2d.fillRoundRect(hx, hy, hw, hh, 4, 4);
                        g2d.setColor(themeColor);
                        g2d.fillOval(hx + 4,       hy + 4,  4, 4);
                        g2d.fillOval(hx + hw - 8,  hy + 4,  4, 4);
                        g2d.fillRect(hx + 5,       hy + 11, hw - 10, 2);
                        g2d.setColor(NeonTheme.BACKGROUND);
                        g2d.fillRect(cx - 1, hy - 4, 2, 4);
                        g2d.fillOval(cx - 3, hy - 7, 6, 6);
                    } else if ("GUEST".equals(iconType)) {
                        // Hình người: đầu + cung vai
                        int r = 7;
                        g2d.fillOval(cx - r, cy - r - 4, r * 2, r * 2);
                        g2d.fillArc(cx - 12, cy + 1, 24, 16, 0, 180);
                    }
                }
            };
            iconBox.setPreferredSize(new Dimension(50, 50));
            iconBox.setOpaque(false);

            JLabel titleLbl = new JLabel("  " + title);
            titleLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 22f));
            titleLbl.setForeground(themeColor);

            headerRow.add(iconBox);
            headerRow.add(titleLbl);

            // Mô tả chế độ chơi
            JTextArea descLbl = new JTextArea(desc);
            descLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.PLAIN, 14f));
            descLbl.setForeground(new Color(164, 167, 222));
            descLbl.setWrapStyleWord(true);
            descLbl.setLineWrap(true);
            descLbl.setOpaque(false);
            descLbl.setEditable(false);
            descLbl.setFocusable(false);

            content.add(headerRow);
            content.add(Box.createVerticalStrut(10));
            content.add(descLbl);
            bgPanel.add(content, BorderLayout.CENTER);

            // ── Nút "BẮT ĐẦU" ────────────────────────────────────
            JPanel startBtn = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(themeColor);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2d.setColor(NeonTheme.BACKGROUND);
                    g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString("BẮT ĐẦU", (getWidth() - fm.stringWidth("BẮT ĐẦU")) / 2, 23);
                }
            };
            startBtn.setBounds(230, 140, 120, 35);
            startBtn.setOpaque(false);
            if (onStartAction != null) {
                startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                startBtn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) { onStartAction.run(); }
                });
            }

            add(startBtn);
            add(bgPanel);
        }
    }
}
