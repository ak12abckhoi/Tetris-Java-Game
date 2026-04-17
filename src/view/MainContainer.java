package view;

import javax.swing.*;
import java.awt.*;

public class MainContainer extends JPanel {

    private static final int NAV_HEIGHT = 60;

    private CardLayout cardLayout;
    private JPanel cardsPanel;
    private BottomNavBar navBar;

    public MainContainer(GameWindow gameWindow) {
        // Use null layout — doLayout() handles all positioning manually
        // to guarantee the navbar is ALWAYS visible at the bottom.
        setLayout(null);
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
        cardsPanel.add(new SettingsPanel(this), "SETTINGS");
        cardsPanel.add(new HighScorePanel(this), "HIGHSCORE");
        cardsPanel.add(new StorePanel(this), "STORE");
        cardsPanel.add(new ProfilePanel(this), "PROFILE");

        // NavBar
        navBar = new BottomNavBar(this);

        add(cardsPanel);
        add(navBar);

        showScreen("HOME");
    }

    @Override
    public void doLayout() {
        // Force-position children: navBar always gets exactly NAV_HEIGHT px
        // at the very bottom; cardsPanel fills everything above it.
        int w = getWidth();
        int h = getHeight();
        cardsPanel.setBounds(0, 0, w, h - NAV_HEIGHT);
        navBar.setBounds(0, h - NAV_HEIGHT, w, NAV_HEIGHT);
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
