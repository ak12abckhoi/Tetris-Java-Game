package model;

import java.io.*;
import java.util.*;

public class ScoreManager {
    private static final String HIGH_SCORE_FILE = "highscore.txt";

    private int score = 0;
    private List<Integer> topScores = new ArrayList<>();
    private boolean dirty = false; // đánh dấu cần ghi file

    public ScoreManager() {
        loadHighScores(); // Tự động tải điểm cao khi khởi tạo game
    }

    public void addScore(int points) {
        score += points;
    }

    /** Điểm cộng khi đặt khối thành công (10đ mỗi ô) */
    public void addPlacementScore(int cellsPlaced) {
        addScore(cellsPlaced * 10);
    }

    /** Điểm cộng khi xóa hàng/cột (100đ mỗi hàng * combo multiplier) */
    public void addClearScore(int linesCleared, int combo) {
        addScore(linesCleared * 100 * combo);
    }

    public void checkAndSaveScore() {
        if (score > 0) {
            topScores.add(score);
            topScores.sort(Collections.reverseOrder());
            if (topScores.size() > 10) {
                topScores = topScores.subList(0, 10);
            }
            dirty = true;
            saveIfNeeded();
        }
    }

    /** Ghi file nếu có cập nhật xếp hạng. Gọi khi Game Over hoặc thoát game. */
    public void saveIfNeeded() {
        if (dirty) {
            saveHighScores();
            dirty = false;
        }
    }

    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return topScores.isEmpty() ? 0 : topScores.get(0);
    }

    public List<Integer> getTopScores() {
        return new ArrayList<>(topScores);
    }

    public void resetScore() {
        score = 0;
    }

    // Đọc điểm cao từ file
    private void loadHighScores() {
        topScores.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                topScores.add(Integer.parseInt(line.trim()));
            }
            topScores.sort(Collections.reverseOrder());
        } catch (IOException | NumberFormatException e) {
            // Nếu file lỗi hoặc chưa có, danh sách sẽ rỗng
        }
    }

    // Lưu top điểm cao vào file
    private void saveHighScores() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            for (int s : topScores) {
                writer.write(String.valueOf(s));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[ScoreManager] Không lưu được điểm cao: " + e.getMessage());
        }
    }
}