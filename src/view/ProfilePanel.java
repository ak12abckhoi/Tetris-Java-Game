package view;

import model.ScoreManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;

/**
 * ProfilePanel — Màn hình Hồ sơ cá nhân.
 *
 * Hiển thị:
 *   - Card avatar (biểu tượng người dùng + tên)
 *   - Dãy thống kê: Kỷ lục, Số ván đã chơi, Combo tối đa
 *   - Lịch sử top 3 điểm số gần nhất (đọc từ ScoreManager)
 *
 * Tất cả kích thước tính tương đối theo chiều rộng để hỗ trợ resize.
 */
public class ProfilePanel extends JPanel {

    private final MainContainer parent;
    private final ScoreManager  scoreManager;

    // Dữ liệu tĩnh (có thể mở rộng kết nối backend sau)
    private final String username   = "NGƯỜI DÙNG";
    private final int    totalGames = 24;
    private final int    bestCombo  = 5;

    public ProfilePanel(MainContainer parent) {
        this.parent       = parent;
        this.scoreManager = new ScoreManager();
        setLayout(new BorderLayout());
        setBackground(NeonTheme.BACKGROUND);
        initUI();
    }

    private void initUI() {
        // Panel nội dung chính với chiều rộng tự động theo cha
        JPanel mainContent = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                Container p = getParent();
                int w = (p != null) ? p.getWidth() : 400;
                return new Dimension(w, d.height);
            }
        };
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        // ── 1. Tiêu đề ─────────────────────────────────────────────
        JLabel titleLbl = new JLabel("HỒ SƠ CÁ NHÂN", SwingConstants.CENTER);
        titleLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 22f));
        titleLbl.setForeground(NeonTheme.CYAN);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        mainContent.add(titleLbl);
        mainContent.add(Box.createVerticalStrut(12));

        // ── 2. Card Avatar ─────────────────────────────────────────
        mainContent.add(createAvatarCard());
        mainContent.add(Box.createVerticalStrut(12));

        // ── 3. Dãy thống kê ────────────────────────────────────────
        mainContent.add(createStatsRow());
        mainContent.add(Box.createVerticalStrut(12));

        // ── 4. Lịch sử điểm số ─────────────────────────────────────
        mainContent.add(createRecentScoresSection());
        mainContent.add(Box.createVerticalStrut(12));

        // Bọc trong JScrollPane để cuộn khi nội dung dài
        JScrollPane scroll = new JScrollPane(mainContent);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        applyNeonScrollBar(scroll);

        add(scroll, BorderLayout.CENTER);
    }

    // ── Card Avatar ────────────────────────────────────────────────

    /** Tạo thẻ hiển thị avatar và tên người dùng, nền glassmorphism. */
    private JPanel createAvatarCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Nền card kính mờ
                g2d.setColor(new Color(NeonTheme.SURFACE.getRed(), NeonTheme.SURFACE.getGreen(),
                        NeonTheme.SURFACE.getBlue(), 200));
                g2d.fillRoundRect(0, 0, w, h, 20, 20);

                // Lớp phủ gradient Cyan → Purple
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(NeonTheme.CYAN.getRed(), NeonTheme.CYAN.getGreen(), NeonTheme.CYAN.getBlue(), 12),
                    w, h, new Color(NeonTheme.PURPLE.getRed(), NeonTheme.PURPLE.getGreen(), NeonTheme.PURPLE.getBlue(), 12));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, w, h, 20, 20);

                // Viền trắng mờ
                g2d.setColor(new Color(255, 255, 255, 25));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);

                // Vòng tròn avatar căn giữa
                int avatarSize = Math.min(60, w / 5);
                int avatarX    = w / 2 - avatarSize / 2;
                int avatarY    = 12;

                NeonTheme.drawGlow(g2d, new Ellipse2D.Float(avatarX, avatarY, avatarSize, avatarSize), NeonTheme.CYAN, 6);
                g2d.setColor(new Color(NeonTheme.CYAN.getRed(), NeonTheme.CYAN.getGreen(), NeonTheme.CYAN.getBlue(), 30));
                g2d.fillOval(avatarX, avatarY, avatarSize, avatarSize);
                g2d.setColor(NeonTheme.CYAN);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawOval(avatarX, avatarY, avatarSize, avatarSize);

                // Biểu tượng người dùng bên trong vòng
                g2d.setColor(NeonTheme.CYAN);
                int cx = w / 2;
                int cy = avatarY + avatarSize / 2;
                int r  = avatarSize / 7;
                g2d.fillOval(cx - r, cy - r * 2 - 2, r * 2, r * 2);
                g2d.fillArc(cx - r - 4, cy, r * 2 + 8, r + 8, 0, 180);

                // Tên người dùng bên dưới avatar
                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 18f));
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(username, (w - fm.stringWidth(username)) / 2, avatarY + avatarSize + 18);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(0, 120));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        return card;
    }

    // ── Dãy thống kê ──────────────────────────────────────────────

    /** Tạo hàng ngang 3 thẻ thống kê: Kỷ lục / Số ván / Combo max. */
    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 8, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 75));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        int highScore = scoreManager.getHighScore();
        row.add(createStatCard("Kỷ lục",    String.format("%,d", highScore), NeonTheme.YELLOW));
        row.add(createStatCard("Số ván",    String.valueOf(totalGames),      NeonTheme.CYAN));
        row.add(createStatCard("Combo max", String.valueOf(bestCombo),       NeonTheme.PINK));
        return row;
    }

    /** Tạo một thẻ thống kê nhỏ với nhãn và giá trị. */
    private JPanel createStatCard(String label, String value, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Nền card
                g2d.setColor(new Color(NeonTheme.SURFACE.getRed(), NeonTheme.SURFACE.getGreen(),
                        NeonTheme.SURFACE.getBlue(), 180));
                g2d.fillRoundRect(0, 0, w, h, 14, 14);

                // Thanh màu trên cùng
                g2d.setColor(color);
                g2d.fillRoundRect(w / 4, 0, w / 2, 3, 3, 3);

                // Giá trị lớn
                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 18f));
                g2d.setColor(color);
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(value, (w - fm.stringWidth(value)) / 2, h / 2);

                // Nhãn nhỏ phía dưới
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2d.setColor(new Color(160, 160, 180));
                fm = g2d.getFontMetrics();
                g2d.drawString(label, (w - fm.stringWidth(label)) / 2, h - 10);
            }
        };
        card.setOpaque(false);
        return card;
    }

    // ── Lịch sử điểm số ───────────────────────────────────────────

    /** Tạo khu vực hiển thị top 3 điểm số gần nhất từ ScoreManager. */
    private JPanel createRecentScoresSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JLabel sectionTitle = new JLabel("  LỊCH SỬ ĐIỂM SỐ");
        sectionTitle.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
        sectionTitle.setForeground(NeonTheme.PURPLE);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(6));

        List<Integer> topScores = scoreManager.getTopScores();
        if (topScores.isEmpty()) {
            // Thẻ trống khi chưa có dữ liệu
            JPanel emptyCard = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(255, 255, 255, 8));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(13f));
                    g2d.setColor(new Color(160, 160, 180));
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = "Chưa có dữ liệu. Hãy chơi!";
                    g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 5);
                }
            };
            emptyCard.setOpaque(false);
            emptyCard.setPreferredSize(new Dimension(0, 40));
            emptyCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            section.add(emptyCard);
        } else {
            // Hiển thị tối đa 3 ván đầu tiên
            int showCount = Math.min(3, topScores.size());
            for (int i = 0; i < showCount; i++) {
                section.add(createScoreRow(i + 1, topScores.get(i), i == 0));
                section.add(Box.createVerticalStrut(4));
            }
        }
        return section;
    }

    /**
     * Tạo một hàng điểm số.
     * Hàng đứng đầu (#1) có viền vàng và biểu tượng vương miện.
     */
    private JPanel createScoreRow(int rank, int score, boolean isTop) {
        JPanel row = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Nền hàng
                g2d.setColor(new Color(NeonTheme.SURFACE.getRed(), NeonTheme.SURFACE.getGreen(),
                        NeonTheme.SURFACE.getBlue(), isTop ? 220 : 150));
                g2d.fillRoundRect(0, 0, w, h, 10, 10);

                // Viền vàng cho hạng nhất
                if (isTop) {
                    g2d.setColor(NeonTheme.YELLOW);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
                }

                // Số thứ hạng
                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
                g2d.setColor(isTop ? NeonTheme.YELLOW : NeonTheme.PURPLE);
                g2d.drawString(String.format("#%d", rank), 12, h / 2 + 5);

                // Biểu tượng vương miện cho hạng 1
                if (isTop) {
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    g2d.drawString("👑", 42, h / 2 + 5);
                }

                // Điểm số bên phải
                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 15f));
                g2d.setColor(isTop ? NeonTheme.YELLOW : Color.WHITE);
                String scoreStr = String.format("%,d điểm", score);
                FontMetrics fm  = g2d.getFontMetrics();
                g2d.drawString(scoreStr, w - fm.stringWidth(scoreStr) - 12, h / 2 + 5);
            }
        };
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 38));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return row;
    }

    // ── Thanh cuộn Neon ───────────────────────────────────────────

    /** Áp dụng thanh cuộn phong cách Neon (thumb gradient cyan→purple, track mờ). */
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
}
