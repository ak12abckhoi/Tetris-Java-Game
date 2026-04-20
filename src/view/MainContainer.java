package view;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * MainContainer — Khung chứa trung tâm của toàn bộ ứng dụng.
 *
 * Dùng CardLayout để quản lý nhiều màn hình.
 * BottomNavBar tự động ẩn khi vào màn chơi (GAME, BATTLE) để không chiếm diện tích
 * và cardsPanel mở rộng toàn chiều cao cửa sổ.
 */
public class MainContainer extends JPanel {

    private static final int NAV_HEIGHT = 60;

    // Tập các màn hình KHÔNG hiển thị NavBar
    private static final Set<String> FULLSCREEN_SCREENS = Set.of("GAME", "BATTLE");

    private final CardLayout   cardLayout;
    private final JPanel       cardsPanel;
    private final BottomNavBar navBar;
    private final BattlePanel  battlePanel;

    private boolean navVisible = true; // Trạng thái hiện tại của NavBar

    public MainContainer(GameWindow gameWindow) {
        setLayout(null);
        setBackground(NeonTheme.BACKGROUND);

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(NeonTheme.BACKGROUND);

        // ── Đăng ký các màn hình ─────────────────────────────────
        HomePanel          homePanel          = new HomePanel(this);
        ModeSelectionPanel modeSelectionPanel = new ModeSelectionPanel(this);
        RankPanel          rankPanel          = new RankPanel(this);
        GamePanel          gamePanel          = new GamePanel(this);
        battlePanel = new BattlePanel(this);

        cardsPanel.add(homePanel,          "HOME");
        cardsPanel.add(modeSelectionPanel, "MODE");
        cardsPanel.add(rankPanel,          "RANK");
        cardsPanel.add(gamePanel,          "GAME");
        cardsPanel.add(battlePanel,        "BATTLE");
        cardsPanel.add(new SettingsPanel(this),  "SETTINGS");
        cardsPanel.add(new HighScorePanel(this), "HIGHSCORE");
        cardsPanel.add(new ProfilePanel(this),   "PROFILE");

        // ── Thanh điều hướng ─────────────────────────────────────
        navBar = new BottomNavBar(this);

        add(cardsPanel);
        add(navBar);

        showScreen("HOME");
    }

    @Override
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();
        if (navVisible) {
            // NavBar hiện: chiếm 60px phía dưới
            cardsPanel.setBounds(0, 0, w, h - NAV_HEIGHT);
            navBar.setBounds(0, h - NAV_HEIGHT, w, NAV_HEIGHT);
        } else {
            // NavBar ẩn: cardsPanel chiếm toàn bộ chiều cao
            cardsPanel.setBounds(0, 0, w, h);
            navBar.setBounds(0, h, w, NAV_HEIGHT); // Ngoài vùng nhìn thấy
        }
    }

    /**
     * Chuyển sang màn hình theo tên.
     * Tự động ẩn/hiện NavBar tuỳ thuộc màn đích là màn chơi hay không.
     */
    public void showScreen(String screenName) {
        cardLayout.show(cardsPanel, screenName);
        navBar.setActive(screenName);

        boolean shouldHideNav = FULLSCREEN_SCREENS.contains(screenName);
        if (navVisible == shouldHideNav) { // Cần đổi trạng thái
            navVisible = !shouldHideNav;
            navBar.setVisible(navVisible);
            doLayout();
            repaint();
        }
    }

    /**
     * Khởi động trận đấu AI và chuyển sang màn BATTLE.
     * NavBar sẽ tự động ẩn qua showScreen().
     */
    public void showBattle() {
        battlePanel.startBattle();
        showScreen("BATTLE");
    }
}
