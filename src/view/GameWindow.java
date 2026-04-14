package view;

import javax.swing.*;

public class GameWindow extends JFrame {
    public GameWindow() {
        setTitle("Neon Tetris Blocks");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 800);
        setLocationRelativeTo(null);
        setResizable(false);

        // Uses MainContainer instead of direct CardLayout to include NavBar
        MainContainer mainContainer = new MainContainer(this);
        add(mainContainer);

        setVisible(true);
    }
}
