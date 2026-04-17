package view;

import javax.swing.*;

public class GameWindow extends JFrame {
    public GameWindow() {
        setTitle("Neon Tetris Blocks");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        MainContainer mainContainer = new MainContainer(this);
        setContentPane(mainContainer);

        setSize(400, 650);
        setMinimumSize(new java.awt.Dimension(360, 500));
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
