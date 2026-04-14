package view;

import javax.swing.*;
import java.awt.*;

public class MainContainer extends JPanel {
    private CardLayout cardLayout;
    private JPanel cardsPanel;
    private BottomNavBar navBar;

    public MainContainer(GameWindow gameWindow) {
        setLayout(new BorderLayout());
        setBackground(NeonTheme.BACKGROUND);

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(NeonTheme.BACKGROUND);

        // Add Screens
        HomePanel homePanel = new HomePanel(this);
        ModeSelectionPanel modeSelectionPanel = new ModeSelectionPanel(this);
        RankPanel rankPanel = new RankPanel(this);
        GamePanel gamePanel = new GamePanel(this);

        cardsPanel.add(homePanel, "HOME");
        cardsPanel.add(modeSelectionPanel, "MODE");
        cardsPanel.add(rankPanel, "RANK");
        cardsPanel.add(gamePanel, "GAME");
        cardsPanel.add(createPlaceholderPanel("CỬA HÀNG"), "STORE");
        cardsPanel.add(createPlaceholderPanel("HỒ SƠ"), "PROFILE");

        // NavBar
        navBar = new BottomNavBar(this);

        add(cardsPanel, BorderLayout.CENTER);
        add(navBar, BorderLayout.SOUTH);

        showScreen("HOME");
    }

    public void showScreen(String screenName) {
        cardLayout.show(cardsPanel, screenName);
        navBar.setActive(screenName);
    }

    private JPanel createPlaceholderPanel(String name) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(NeonTheme.BACKGROUND);
        JLabel lbl = new JLabel(name + " (COMING SOON)", SwingConstants.CENTER);
        lbl.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 24f));
        lbl.setForeground(NeonTheme.CYAN);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }
}
