package main;

import view.GameWindow;
import javax.swing.SwingUtilities;

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
