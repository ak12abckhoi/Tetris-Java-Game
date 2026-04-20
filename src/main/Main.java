package main;

import view.GameWindow;
import javax.swing.SwingUtilities;

/**
 * Main — Điểm khởi động của ứng dụng Neon Tetris Blocks.
 *
 * Tạo GameWindow trên Swing Event Dispatch Thread (EDT) để đảm bảo
 * an toàn luồng cho toàn bộ giao diện người dùng.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Main starting...");
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Initializing GameWindow...");
                new GameWindow();
                System.out.println("GameWindow initialized.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
