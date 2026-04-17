package view;
 
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class HighScorePanel extends JPanel {
    private MainContainer parent;

    public HighScorePanel(MainContainer parent) {
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

        JLabel titleStr = new JLabel("ĐIỂM CAO CÁ NHÂN", SwingConstants.CENTER);
        titleStr.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 26f));
        titleStr.setForeground(NeonTheme.PURPLE);
        header.add(titleStr, BorderLayout.CENTER);
        
        add(header, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Scrollable List
        JPanel playerList = new JPanel();
        playerList.setLayout(new BoxLayout(playerList, BoxLayout.Y_AXIS));
        playerList.setOpaque(false);

        model.ScoreManager scoreManager = new model.ScoreManager();
        List<Integer> topScores = scoreManager.getTopScores();

        if (topScores.isEmpty()) {
            JLabel emptyLabel = new JLabel("Chưa có dữ liệu. Hãy chơi một ván!", SwingConstants.CENTER);
            emptyLabel.setFont(NeonTheme.MAIN_FONT.deriveFont(16f));
            emptyLabel.setForeground(Color.WHITE);
            playerList.add(Box.createVerticalStrut(50));
            playerList.add(emptyLabel);
        } else {
            for (int i = 0; i < topScores.size(); i++) {
                String scoreStr = String.format("%,d", topScores.get(i));
                boolean isTop3 = i < 3;
                playerList.add(new HighScoreRow(i + 1, "LƯỢT CHƠI", scoreStr, isTop3));
            }
        }

        JScrollPane scroll = new JScrollPane(playerList);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        centerPanel.add(scroll);

        add(centerPanel, BorderLayout.CENTER);
    }

    private class HighScoreRow extends JPanel {
        public HighScoreRow(int rank, String name, String score, boolean isTop3) {
            setLayout(new BorderLayout());
            setOpaque(false);

            setPreferredSize(new Dimension(380, 80));
            setMaximumSize(new Dimension(380, 80));
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            JLabel rankLbl = new JLabel(String.format("%02d", rank));
            rankLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(22f));
            rankLbl.setForeground(isTop3 ? NeonTheme.YELLOW : NeonTheme.PINK); 
            rankLbl.setPreferredSize(new Dimension(50, 60));
            add(rankLbl, BorderLayout.WEST);

            JPanel center = new JPanel(new GridLayout(2, 1));
            center.setOpaque(false);
            JLabel nameLbl = new JLabel(name + " " + rank);
            nameLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 18f));
            nameLbl.setForeground(Color.WHITE);
            center.add(nameLbl);

            JLabel subText = new JLabel("Hoàn thành nhanh");
            subText.setFont(new Font("SansSerif", Font.PLAIN, 10));
            subText.setForeground(NeonTheme.CYAN);
            center.add(subText);
            
            add(center, BorderLayout.CENTER);

            JLabel scoreLbl = new JLabel(score, SwingConstants.RIGHT);
            scoreLbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 22f));
            scoreLbl.setForeground(isTop3 ? NeonTheme.YELLOW : Color.WHITE);
            add(scoreLbl, BorderLayout.EAST);
        }
    }
}
