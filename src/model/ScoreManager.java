package model;

import java.io.*;

public class ScoreManager {
    private int score = 0;
    private int highScore = 0;

    public ScoreManager() {
        loadHighScore(); // Tự động tải điểm cao khi khởi tạo game 
    }

    public void addScore(int points) {
        score += points;
        // Nếu điểm hiện tại vượt kỷ lục, cập nhật và lưu lại ngay
        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }
    }

    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return highScore;
    }

    public void resetScore() {
        score = 0;
    }

    // Đọc điểm cao từ file 
    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line);
            }
        } catch (IOException | NumberFormatException e) {
            highScore = 0; // Nếu file lỗi hoặc chưa có, mặc định là 0
        }
    }

    // Lưu điểm cao vào file 
    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("highscore.txt"))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}