package model;

import java.io.*;

public class ScoreManager {
    private static final String HIGH_SCORE_FILE = "highscore.txt";

    private int score = 0;
    private int highScore = 0;
    private boolean dirty = false; // đánh dấu cần ghi file

    public ScoreManager() {
        loadHighScore(); // Tự động tải điểm cao khi khởi tạo game
    }

    public void addScore(int points) {
        score += points;
        // Chỉ cập nhật biến, không ghi file ngay — giảm I/O
        if (score > highScore) {
            highScore = score;
            dirty = true;
        }
    }

    /** Điểm cộng khi đặt khối thành công (10đ mỗi ô) */
    public void addPlacementScore(int cellsPlaced) {
        addScore(cellsPlaced * 10);
    }

    /** Điểm cộng khi xóa hàng/cột (100đ mỗi hàng * combo multiplier) */
    public void addClearScore(int linesCleared, int combo) {
        addScore(linesCleared * 100 * combo);
    }

    /** Ghi file nếu highScore thay đổi. Gọi khi Game Over hoặc thoát game. */
    public void saveIfNeeded() {
        if (dirty) {
            saveHighScore();
            dirty = false;
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
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line.trim());
            }
        } catch (IOException | NumberFormatException e) {
            highScore = 0; // Nếu file lỗi hoặc chưa có, mặc định là 0
        }
    }

    // Lưu điểm cao vào file
    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            System.err.println("[ScoreManager] Không lưu được điểm cao: " + e.getMessage());
        }
    }
}