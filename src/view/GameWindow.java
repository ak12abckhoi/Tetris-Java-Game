package view;

import javax.swing.*;

/**
 * GameWindow — Cửa sổ chính của ứng dụng Neon Tetris Blocks.
 *
 * Tạo JFrame và nhúng MainContainer vào làm nội dung chính.
 * Kích thước mặc định 400×650, có thể resize nhưng tối thiểu 360×500.
 */
public class GameWindow extends JFrame {

    public GameWindow() {
        setTitle("Neon Tetris Blocks");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        setContentPane(new MainContainer(this));

        setSize(400, 650);
        setMinimumSize(new java.awt.Dimension(360, 500));
        setLocationRelativeTo(null); // Căn giữa màn hình
        setVisible(true);
    }
}
