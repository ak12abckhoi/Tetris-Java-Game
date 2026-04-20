package view;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * HighScorePanel — Màn hình Điểm cao cá nhân.
 *
 * Đọc điểm số từ file "highscore.txt" (qua ScoreManager) và hiển thị
 * danh sách top 10 ván chơi tốt nhất của người chơi hiện tại.
 * Top 3 được tô màu vàng để phân biệt.
 */
public class HighScorePanel extends JPanel {

    private final MainContainer parent;

    public HighScorePanel(MainContainer parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(NeonTheme.BACKGROUND);
        initUI();
    }

    private void initUI() {
        // ── Header ───────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 24));
        backBtn.setForeground(Color.WHITE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> parent.showScreen("HOME"));
        header.add(backBtn, BorderLayout.WEST);

        JLabel titleStr = new JLabel("ĐIỂM CAO CÁ NHÂN", SwingConstants.CENTER);
        titleStr.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 26f));
        titleStr.setForeground(NeonTheme.PURPLE);
        header.add(titleStr, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // ── Danh sách điểm ────────────────────────────────────────
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel playerList = new JPanel();
        playerList.setLayout(new BoxLayout(playerList, BoxLayout.Y_AXIS));
        playerList.setOpaque(false);

        // Đọc điểm từ ScoreManager (file highscore.txt)
        model.ScoreManager scoreManager = new model.ScoreManager();
        List<Integer> topScores = scoreManager.getTopScores();

        if (topScores.isEmpty()) {
            // Chưa có dữ liệu
            JLabel emptyLabel = new JLabel("Chưa có dữ liệu. Hãy chơi một ván!", SwingConstants.CENTER);
            emptyLabel.setFont(NeonTheme.MAIN_FONT.deriveFont(16f));
            emptyLabel.setForeground(Color.WHITE);
            playerList.add(Box.createVerticalStrut(50));
            playerList.add(emptyLabel);
        } else {
            for (int i = 0; i < topScores.size(); i++) {
                String scoreStr = String.format("%,d", topScores.get(i));
                boolean isTop3  = i < 3;
                playerList.add(new HighScoreRow(i + 1, "LƯỢT CHƠI", scoreStr, isTop3));
            }
        }

        // Thanh cuộn
        JScrollPane scroll = new JScrollPane(playerList);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        applyNeonScrollBar(scroll);

        centerPanel.add(scroll);
        add(centerPanel, BorderLayout.CENTER);
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

    // ── Lớp nội tuyến: dòng điểm ─────────────────────────────────

    /**
     * HighScoreRow — Một hàng trong bảng điểm cá nhân.
     * Top 3 tô màu vàng, còn lại tô màu hồng.
     */
    private class HighScoreRow extends JPanel {
        public HighScoreRow(int rank, String label, String score, boolean isTop3) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(0, 80));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 30));

            // Số thứ tự
            JLabel rankLbl = new JLabel(String.format("%02d", rank));
            rankLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(22f));
            rankLbl.setForeground(isTop3 ? NeonTheme.YELLOW : NeonTheme.PINK);
            rankLbl.setPreferredSize(new Dimension(50, 60));
            add(rankLbl, BorderLayout.WEST);

            // Nhãn + mô tả phụ
            JPanel center = new JPanel(new GridLayout(2, 1));
            center.setOpaque(false);
            JLabel nameLbl = new JLabel(label + " " + rank);
            nameLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 18f));
            nameLbl.setForeground(Color.WHITE);
            center.add(nameLbl);
            JLabel subText = new JLabel("Hoàn thành nhanh");
            subText.setFont(new Font("SansSerif", Font.PLAIN, 10));
            subText.setForeground(NeonTheme.CYAN);
            center.add(subText);
            add(center, BorderLayout.CENTER);

            // Điểm số
            JLabel scoreLbl = new JLabel(score, SwingConstants.RIGHT);
            scoreLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 22f));
            scoreLbl.setForeground(isTop3 ? NeonTheme.YELLOW : Color.WHITE);
            add(scoreLbl, BorderLayout.EAST);
        }
    }
}
