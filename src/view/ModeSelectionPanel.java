package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModeSelectionPanel extends JPanel {
    private MainContainer parent;

    public ModeSelectionPanel(MainContainer parent) {
        this.parent = parent;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(NeonTheme.BACKGROUND);

        // Standardized Header
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
        
        // Placeholder to balance the back button and center the title
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);
        placeholder.setPreferredSize(new Dimension(50, 40));
        header.add(placeholder, BorderLayout.EAST);

        add(header);
        add(Box.createVerticalStrut(5));

        // Custom subtle glowing underline
        JPanel underline = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0,
                        new Color(NeonTheme.YELLOW.getRed(), NeonTheme.YELLOW.getGreen(), NeonTheme.YELLOW.getBlue(),
                                0),
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

        // Mode Card 1: AI (Placeholder)
        ModeCard card1 = new ModeCard("CHƠI VỚI MÁY",
                "Thử thách kỹ năng của bạn với AI thông minh trong những ván đấu kịch tính", NeonTheme.CYAN, null);
        add(card1);

        add(Box.createVerticalStrut(30));

        // Mode Card 2: Solo (Leads to Game) — only "BẮT ĐẦU" button triggers navigation
        ModeCard card2 = new ModeCard("CHƠI MỘT MÌNH",
                "Phá vỡ kỷ lục điểm số của chính mình trong chế độ chơi cổ điển không giới hạn", NeonTheme.PINK,
                () -> parent.showScreen("GAME"));
        add(card2);
    }

    private class ModeCard extends JPanel {
        public ModeCard(String title, String desc, Color themeColor, Runnable onStartAction) {
            setLayout(null);
            setPreferredSize(new Dimension(380, 200));
            setMaximumSize(new Dimension(380, 200));
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            Color bgDark = new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 30);

            // Overriding paint component for background & border
            JPanel bgPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2d.setColor(bgDark);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                    // Glowing bottom border
                    g2d.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 150));
                    g2d.fillRoundRect(0, getHeight() - 8, getWidth(), 8, 30, 30);
                }
            };
            bgPanel.setBounds(0, 0, 380, 200);
            bgPanel.setOpaque(false);
            bgPanel.setLayout(new BorderLayout());

            // Inner content
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);
            content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Icon + Title
            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
            header.setOpaque(false);

            JPanel iconBox = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(themeColor);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    NeonTheme.drawGlow(g2d,
                            new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15), themeColor,
                            10);
                }
            };
            iconBox.setPreferredSize(new Dimension(50, 50));
            iconBox.setOpaque(false);

            JLabel titleLbl = new JLabel("  " + title);
            titleLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 22f));
            titleLbl.setForeground(themeColor);

            header.add(iconBox);
            header.add(titleLbl);

            // Description
            JTextArea descLbl = new JTextArea(desc);
            descLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.PLAIN, 14f));
            descLbl.setForeground(new Color(164, 167, 222)); // Text color from HTML
            descLbl.setWrapStyleWord(true);
            descLbl.setLineWrap(true);
            descLbl.setOpaque(false);
            descLbl.setEditable(false);
            descLbl.setFocusable(false);

            content.add(header);
            content.add(Box.createVerticalStrut(10));
            content.add(descLbl);

            bgPanel.add(content, BorderLayout.CENTER);

            // "BẮT ĐẦU" button — click handler triggers navigation
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
                    public void mouseClicked(MouseEvent e) {
                        onStartAction.run();
                    }
                });
            }

            add(startBtn);
            add(bgPanel);
        }
    }
}
