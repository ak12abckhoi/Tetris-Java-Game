package view;

import model.GameSettings;
import model.ScoreManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * ProfilePanel — Giao diện hồ sơ cá nhân responsive.
 * Mọi kích thước tính tương đối theo chiều rộng panel cha.
 */
public class ProfilePanel extends JPanel {
    private MainContainer parent;
    private GameSettings settings;
    private ScoreManager scoreManager;

    private String username = "NGƯỜI DÙNG";
    private String title = "Tân thủ Tetris";
    private int totalGames = 24;
    private int bestCombo = 5;

    public ProfilePanel(MainContainer parent) {
        this.parent = parent;
        this.settings = GameSettings.getInstance();
        this.scoreManager = new ScoreManager();
        setLayout(new BorderLayout());
        setBackground(NeonTheme.BACKGROUND);
        initUI();
    }

    private void initUI() {
        JPanel mainContent = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                // Let parent width drive sizing
                Container p = getParent();
                int w = (p != null) ? p.getWidth() : 400;
                return new Dimension(w, 1100); // Enough vertical space for all content
            }
        };
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        // 1. HEADER
        JLabel titleLbl = new JLabel("HỒ SƠ CÁ NHÂN", SwingConstants.CENTER);
        titleLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 22f));
        titleLbl.setForeground(NeonTheme.CYAN);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        mainContent.add(titleLbl);
        mainContent.add(Box.createVerticalStrut(12));

        // 2. AVATAR CARD
        mainContent.add(createAvatarCard());
        mainContent.add(Box.createVerticalStrut(12));

        // 3. STATS ROW
        mainContent.add(createStatsRow());
        mainContent.add(Box.createVerticalStrut(12));

        // 4. ACHIEVEMENTS
        mainContent.add(createAchievementSection());
        mainContent.add(Box.createVerticalStrut(12));

        // 5. RECENT SCORES
        mainContent.add(createRecentScoresSection());
        mainContent.add(Box.createVerticalStrut(12));

        // 6. QUICK SETTINGS
        mainContent.add(createQuickSettings());
        mainContent.add(Box.createVerticalStrut(12));

        // 7. EDIT PROFILE BUTTON
        JPanel editBtnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        editBtnWrapper.setOpaque(false);
        editBtnWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        editBtnWrapper.add(createActionButton("✎  CHỈNH SỬA HỒ SƠ", NeonTheme.CYAN));
        mainContent.add(editBtnWrapper);

        JScrollPane scroll = new JScrollPane(mainContent);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    // ─── AVATAR CARD ──────────────────────────────────────────
    private JPanel createAvatarCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();

                // Card bg
                g2d.setColor(new Color(NeonTheme.SURFACE.getRed(), NeonTheme.SURFACE.getGreen(),
                        NeonTheme.SURFACE.getBlue(), 200));
                g2d.fillRoundRect(0, 0, w, h, 20, 20);

                // Gradient overlay
                GradientPaint gp = new GradientPaint(0, 0,
                        new Color(NeonTheme.CYAN.getRed(), NeonTheme.CYAN.getGreen(), NeonTheme.CYAN.getBlue(), 12),
                        w, h,
                        new Color(NeonTheme.PURPLE.getRed(), NeonTheme.PURPLE.getGreen(), NeonTheme.PURPLE.getBlue(),
                                12));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, w, h, 20, 20);

                g2d.setColor(new Color(255, 255, 255, 25));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);

                // Avatar — always centered
                int avatarSize = Math.min(60, w / 5);
                int avatarX = w / 2 - avatarSize / 2;
                int avatarY = 12;

                NeonTheme.drawGlow(g2d,
                        new Ellipse2D.Float(avatarX, avatarY, avatarSize, avatarSize),
                        NeonTheme.CYAN, 6);

                g2d.setColor(new Color(NeonTheme.CYAN.getRed(), NeonTheme.CYAN.getGreen(),
                        NeonTheme.CYAN.getBlue(), 30));
                g2d.fillOval(avatarX, avatarY, avatarSize, avatarSize);
                g2d.setColor(NeonTheme.CYAN);
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawOval(avatarX, avatarY, avatarSize, avatarSize);

                // Person icon
                g2d.setColor(NeonTheme.CYAN);
                int cx = w / 2;
                int cy = avatarY + avatarSize / 2;
                int headR = avatarSize / 7;
                g2d.fillOval(cx - headR, cy - headR * 2 - 2, headR * 2, headR * 2);
                g2d.fillArc(cx - headR - 4, cy, headR * 2 + 8, headR + 8, 0, 180);

                // Username
                int textStartY = avatarY + avatarSize + 18;
                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 18f));
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(username, (w - fm.stringWidth(username)) / 2, textStartY);

                // Title
                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.PLAIN, 12f));
                g2d.setColor(NeonTheme.YELLOW);
                fm = g2d.getFontMetrics();
                String titleStr = "⭐ " + title;
                g2d.drawString(titleStr, (w - fm.stringWidth(titleStr)) / 2, textStartY + 18);

                // Level bar — relative width
                int barPad = w / 10;
                int barY = textStartY + 28;
                int barW = w - barPad * 2;
                int barH = 6;

                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(barPad, barY, barW, barH, 3, 3);
                g2d.setColor(NeonTheme.LIME);
                g2d.fillRoundRect(barPad, barY, (int) (barW * 0.65), barH, 3, 3);

                g2d.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2d.setColor(new Color(160, 160, 180));
                g2d.drawString("Cấp 3", barPad, barY + 16);
                g2d.drawString("65%", barPad + barW - 22, barY + 16);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(0, 170));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        return card;
    }

    // ─── STATS ROW ────────────────────────────────────────────
    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 8, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 75));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        int highScore = scoreManager.getHighScore();
        row.add(createStatCard("Kỷ lục", String.format("%,d", highScore), NeonTheme.YELLOW));
        row.add(createStatCard("Số ván", String.valueOf(totalGames), NeonTheme.CYAN));
        row.add(createStatCard("Combo max", String.valueOf(bestCombo), NeonTheme.PINK));
        return row;
    }

    private JPanel createStatCard(String label, String value, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                g2d.setColor(new Color(NeonTheme.SURFACE.getRed(), NeonTheme.SURFACE.getGreen(),
                        NeonTheme.SURFACE.getBlue(), 180));
                g2d.fillRoundRect(0, 0, w, h, 14, 14);

                g2d.setColor(color);
                g2d.fillRoundRect(w / 4, 0, w / 2, 3, 3, 3);

                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 18f));
                g2d.setColor(color);
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(value, (w - fm.stringWidth(value)) / 2, h / 2);

                g2d.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2d.setColor(new Color(160, 160, 180));
                fm = g2d.getFontMetrics();
                g2d.drawString(label, (w - fm.stringWidth(label)) / 2, h - 10);
            }
        };
        card.setOpaque(false);
        return card;
    }

    // ─── ACHIEVEMENTS ─────────────────────────────────────────
    private JPanel createAchievementSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        JLabel sectionTitle = new JLabel("  THÀNH TÍCH");
        sectionTitle.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
        sectionTitle.setForeground(NeonTheme.PURPLE);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(6));

        // Use GridLayout for 4 badges that fill available width
        JPanel badgesRow = new JPanel(new GridLayout(1, 4, 6, 0));
        badgesRow.setOpaque(false);
        badgesRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        badgesRow.setPreferredSize(new Dimension(0, 100));

        badgesRow.add(createAchievementBadge("Người mới", "Ván đầu tiên", NeonTheme.LIME, true));
        badgesRow.add(createAchievementBadge("500 điểm", "Đạt 500 điểm", NeonTheme.CYAN, true));
        badgesRow.add(createAchievementBadge("1000 điểm", "Đạt 1000 điểm", NeonTheme.YELLOW, false));
        badgesRow.add(createAchievementBadge("Combo x3", "Đạt combo x3", NeonTheme.PINK, false));

        section.add(badgesRow);
        return section;
    }

    private JPanel createAchievementBadge(String name, String desc, Color color, boolean unlocked) {
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                g2d.setColor(unlocked ? new Color(color.getRed(), color.getGreen(), color.getBlue(), 25)
                        : new Color(255, 255, 255, 8));
                g2d.fillRoundRect(0, 0, w, h, 12, 12);

                g2d.setColor(unlocked ? color : new Color(80, 80, 100));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

                // Icon circle — relative to width
                int iconSize = Math.min(28, w / 3);
                int iconX = (w - iconSize) / 2;
                int iconY = 8;

                g2d.setColor(unlocked ? color : new Color(60, 60, 80));
                g2d.fillOval(iconX, iconY, iconSize, iconSize);

                g2d.setColor(unlocked ? NeonTheme.BACKGROUND : new Color(100, 100, 120));
                g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2d.getFontMetrics();
                String icon = unlocked ? "✓" : "🔒";
                g2d.drawString(icon, (w - fm.stringWidth(icon)) / 2, iconY + iconSize / 2 + 5);

                g2d.setFont(new Font("SansSerif", Font.BOLD, 8));
                g2d.setColor(unlocked ? Color.WHITE : new Color(100, 100, 120));
                fm = g2d.getFontMetrics();
                String displayName = name.length() > 10 ? name.substring(0, 9) + ".." : name;
                g2d.drawString(displayName, (w - fm.stringWidth(displayName)) / 2, h - 18);

                g2d.setFont(new Font("SansSerif", Font.PLAIN, 7));
                g2d.setColor(unlocked ? new Color(160, 160, 180) : new Color(70, 70, 90));
                fm = g2d.getFontMetrics();
                String shortDesc = desc.length() > 14 ? desc.substring(0, 12) + ".." : desc;
                g2d.drawString(shortDesc, (w - fm.stringWidth(shortDesc)) / 2, h - 7);
            }
        };
        badge.setOpaque(false);
        return badge;
    }

    // ─── RECENT SCORES ────────────────────────────────────────
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
            int showCount = Math.min(3, topScores.size());
            for (int i = 0; i < showCount; i++) {
                section.add(createScoreRow(i + 1, topScores.get(i), i == 0));
                section.add(Box.createVerticalStrut(4));
            }
        }
        return section;
    }

    private JPanel createScoreRow(int rank, int score, boolean isTop) {
        JPanel row = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                g2d.setColor(new Color(NeonTheme.SURFACE.getRed(), NeonTheme.SURFACE.getGreen(),
                        NeonTheme.SURFACE.getBlue(), isTop ? 220 : 150));
                g2d.fillRoundRect(0, 0, w, h, 10, 10);

                if (isTop) {
                    g2d.setColor(NeonTheme.YELLOW);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
                }

                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
                g2d.setColor(isTop ? NeonTheme.YELLOW : NeonTheme.PURPLE);
                g2d.drawString(String.format("#%d", rank), 12, h / 2 + 5);

                if (isTop) {
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    g2d.drawString("👑", 42, h / 2 + 5);
                }

                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 15f));
                g2d.setColor(isTop ? NeonTheme.YELLOW : Color.WHITE);
                String scoreStr = String.format("%,d điểm", score);
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(scoreStr, w - fm.stringWidth(scoreStr) - 12, h / 2 + 5);
            }
        };
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 38));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return row;
    }

    // ─── QUICK SETTINGS ───────────────────────────────────────
    private JPanel createQuickSettings() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel sectionTitle = new JLabel("  CÀI ĐẶT NHANH");
        sectionTitle.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
        sectionTitle.setForeground(NeonTheme.PURPLE);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(6));

        section.add(createSettingRow("🔊  Âm thanh", settings.isSoundEnabled()));
        section.add(Box.createVerticalStrut(4));
        section.add(createVolumeRow());
        return section;
    }

    private JPanel createSettingRow(String label, boolean enabled) {
        JPanel row = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                g2d.setColor(new Color(NeonTheme.SURFACE.getRed(), NeonTheme.SURFACE.getGreen(),
                        NeonTheme.SURFACE.getBlue(), 150));
                g2d.fillRoundRect(0, 0, w, h, 10, 10);

                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(13f));
                g2d.setColor(Color.WHITE);
                g2d.drawString(label, 12, h / 2 + 5);

                int toggleW = 38, toggleH = 20;
                int toggleX = w - toggleW - 12;
                int toggleY = (h - toggleH) / 2;

                boolean sndEnabled = settings.isSoundEnabled();
                if (sndEnabled) {
                    g2d.setColor(NeonTheme.LIME);
                    g2d.fillRoundRect(toggleX, toggleY, toggleW, toggleH, toggleH, toggleH);
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(toggleX + toggleW - toggleH + 2, toggleY + 2, toggleH - 4, toggleH - 4);
                } else {
                    g2d.setColor(new Color(80, 80, 100));
                    g2d.fillRoundRect(toggleX, toggleY, toggleW, toggleH, toggleH, toggleH);
                    g2d.setColor(new Color(150, 150, 170));
                    g2d.fillOval(toggleX + 2, toggleY + 2, toggleH - 4, toggleH - 4);
                }
            }
        };
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 38));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                settings.setSoundEnabled(!settings.isSoundEnabled());
                ProfilePanel.this.repaint();
            }
        });
        return row;
    }

    private JPanel createVolumeRow() {
        JPanel row = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                g2d.setColor(new Color(NeonTheme.SURFACE.getRed(), NeonTheme.SURFACE.getGreen(),
                        NeonTheme.SURFACE.getBlue(), 150));
                g2d.fillRoundRect(0, 0, w, h, 10, 10);

                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(13f));
                g2d.setColor(Color.WHITE);
                g2d.drawString("🎵  Âm lượng", 12, h / 2 + 5);

                // Volume bar — relative to width
                int barW = w / 4;
                int barH = 5;
                int barX = w - barW - 50;
                int barY = (h - barH) / 2;
                float vol = settings.getVolume() / 100f;

                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(barX, barY, barW, barH, 3, 3);
                g2d.setColor(NeonTheme.CYAN);
                g2d.fillRoundRect(barX, barY, (int) (barW * vol), barH, 3, 3);

                g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2d.setColor(NeonTheme.CYAN);
                g2d.drawString(settings.getVolume() + "%", w - 42, h / 2 + 5);
            }
        };
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 38));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return row;
    }

    // ─── ACTION BUTTON ────────────────────────────────────────
    private JPanel createActionButton(String text, Color color) {
        JPanel btn = new JPanel() {
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
                int w = getWidth(), h = getHeight();

                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(),
                        hovered ? 40 : 20));
                g2d.fillRoundRect(0, 0, w, h, 18, 18);

                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(),
                        hovered ? 220 : 150));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);

                if (hovered) {
                    NeonTheme.drawGlow(g2d,
                            new RoundRectangle2D.Float(0, 0, w, h, 18, 18), color, 4);
                }

                g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 13f));
                g2d.setColor(color);
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(text, (w - fm.stringWidth(text)) / 2, (h + fm.getAscent()) / 2 - 3);
            }
        };
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(240, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
