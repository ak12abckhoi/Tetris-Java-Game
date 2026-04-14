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

        JLabel titleStr = new JLabel("BẢNG XẾP HẠNG", SwingConstants.CENTER);
        titleStr.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 28f));
        titleStr.setForeground(NeonTheme.CYAN);
        header.add(titleStr, BorderLayout.CENTER);
        
        add(header, BorderLayout.NORTH);

        // Center Content: Podiums + List
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Podiums Section
        JPanel podiumSection = new JPanel(null);
        podiumSection.setOpaque(false);
        podiumSection.setPreferredSize(new Dimension(400, 250));
        podiumSection.setMaximumSize(new Dimension(400, 250));

        // Draw Top 3 Podiums (2nd, 1st, 3rd)
        podiumSection.add(createPodium("HyperNova", "4,820", NeonTheme.CYAN, 2, 0, 100, 100)); // 2nd
        podiumSection.add(createPodium("NeonGod_99", "5,150", NeonTheme.YELLOW, 1, 130, 50, 120)); // 1st
        podiumSection.add(createPodium("Vortex_X", "4,500", NeonTheme.PINK, 3, 260, 110, 90)); // 3rd

        centerPanel.add(podiumSection);
        centerPanel.add(Box.createVerticalStrut(20));

        // 2. Scrollable List
        JPanel playerList = new JPanel();
        playerList.setLayout(new BoxLayout(playerList, BoxLayout.Y_AXIS));
        playerList.setOpaque(false);

        // Mock data
        playerList.add(new RankRow(4, "ShadowWalker", "3,120", false));
        playerList.add(new RankRow(5, "NGƯỜI DÙNG", "2,450", true)); // Current user
        playerList.add(new RankRow(6, "CyberCat", "2,100", false));
        playerList.add(new RankRow(7, "GlitchMaster", "1,950", false));
        playerList.add(new RankRow(8, "PixelPirate", "1,720", false));

        JScrollPane scroll = new JScrollPane(playerList);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        centerPanel.add(scroll);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createPodium(String name, String score, Color color, int rank, int x, int y, int size) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background card
                g2d.setColor(new Color(NeonTheme.SURFACE.getRed(), NeonTheme.SURFACE.getGreen(), NeonTheme.SURFACE.getBlue(), 200));
                g2d.fillRoundRect(0, 70, getWidth(), getHeight()-70, 15, 15);

                // Circular Avatar Background
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
                g2d.fillOval(getWidth()/2 - 40, 0, 80, 80);
                
                // Glowing border
                NeonTheme.drawGlow(g2d, new Ellipse2D.Float(getWidth()/2 - 40, 0, 80, 80), color, 8);

                // Rank indicator
                g2d.setColor(color);
                g2d.fillOval(getWidth()/2 + 25, 5, 25, 25);
                g2d.setColor(NeonTheme.BACKGROUND);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2d.drawString(String.valueOf(rank), getWidth()/2 + 33, 23);
            }
        };
        p.setBounds(x, y, 120, 180);
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        
        p.add(Box.createVerticalStrut(90));
        
        JLabel nameLbl = new JLabel(name, SwingConstants.CENTER);
        nameLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(12f));
        nameLbl.setForeground(Color.WHITE);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(nameLbl);

        JLabel scoreLbl = new JLabel(score, SwingConstants.CENTER);
        scoreLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 18f));
        scoreLbl.setForeground(color);
        scoreLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(scoreLbl);

        return p;
    }

    private class RankRow extends JPanel {
        public RankRow(int rank, String name, String score, boolean isCurrentUser) {
            setLayout(new BorderLayout());
            if (isCurrentUser) {
                setOpaque(true);
                setBackground(new Color(243, 255, 202, 30)); // Faint YELLOW
            } else {
                setOpaque(false);
            }

            setPreferredSize(new Dimension(380, 80));
            setMaximumSize(new Dimension(380, 80));
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            JLabel rankLbl = new JLabel(String.format("%02d", rank));
            rankLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(22f));
            rankLbl.setForeground(new Color(164, 167, 222)); // PURPLE
            rankLbl.setPreferredSize(new Dimension(50, 60));
            add(rankLbl, BorderLayout.WEST);

            JPanel center = new JPanel(new GridLayout(2, 1));
            center.setOpaque(false);
            JLabel nameLbl = new JLabel(name);
            nameLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(isCurrentUser ? Font.BOLD : Font.PLAIN, 18f));
            nameLbl.setForeground(isCurrentUser ? NeonTheme.YELLOW : Color.WHITE);
            center.add(nameLbl);

            if (isCurrentUser) {
                JLabel subText = new JLabel("THÀNH TÍCH CÁ NHÂN");
                subText.setFont(new Font("SansSerif", Font.PLAIN, 10));
                subText.setForeground(NeonTheme.YELLOW);
                center.add(subText);
            }
            add(center, BorderLayout.CENTER);

            JLabel scoreLbl = new JLabel(score, SwingConstants.RIGHT);
            scoreLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 20f));
            scoreLbl.setForeground(isCurrentUser ? NeonTheme.YELLOW : NeonTheme.CYAN);
            add(scoreLbl, BorderLayout.EAST);
        }
    }
}
