package model;

import java.io.*;
import java.util.*;

/**
 * ScoreManager — Quản lý điểm số trong một ván chơi và bảng xếp hạng top 10.
 *
 * Điểm được tích luỹ từ hai nguồn:
 *   - Đặt khối thành công : +10 điểm / khối
 *   - Xoá hàng / cột         : +100 × số hàng × combo
 *
 * Bảng xếp hạng được ghi/đọc từ file "highscore.txt".
 */
public class ScoreManager {

    private static final String HIGH_SCORE_FILE = "highscore.txt";

    private int score = 0;
    private List<Integer> topScores = new ArrayList<>();
    private boolean dirty = false;       // Đánh dấu cần ghi lại file khi có thay đổi
    private boolean isNewRecord = false; // true nếu điểm vừa đạt vượt kỷ lục cũ

    public ScoreManager() {
        loadHighScores();
    }

    // ── Cộng điểm ────────────────────────────────────────────────

    /** Cộng thẳng một lượng điểm tuỳ ý. */
    public void addScore(int points) {
        score += points;
    }

    /** Cộng điểm đặt khối: 10 điểm × số ô được đặt. */
    public void addPlacementScore(int cellsPlaced) {
        addScore(cellsPlaced * 10);
    }

    /**
     * Cộng điểm xoá dòng: 100 điểm × số hàng/cột xoá được × hệ số combo.
     *
     * @param linesCleared  Số hàng (hoặc cột) đã xoá trong lượt này
     * @param combo         Hệ số combo liên tiếp (bắt đầu từ 1)
     */
    public void addClearScore(int linesCleared, int combo) {
        addScore(linesCleared * 100 * combo);
    }

    // ── Kết thúc ván ─────────────────────────────────────────────

    /**
     * So sánh điểm với kỷ lục cũ, cập nhật bảng top 10 và lưu file nếu cần.
     * Gọi khi kết thúc ván chơi.
     */
    public void checkAndSaveScore() {
        if (score <= 0) return;
        isNewRecord = (score > getHighScore());
        topScores.add(score);
        topScores.sort(Collections.reverseOrder());
        if (topScores.size() > 10) topScores = topScores.subList(0, 10);
        dirty = true;
        saveIfNeeded();
    }

    /** Ghi file nếu bảng xếp hạng có thay đổi. */
    public void saveIfNeeded() {
        if (dirty) {
            saveHighScores();
            dirty = false;
        }
    }

    // ── Getter ────────────────────────────────────────────────────

    public int getScore()          { return score; }
    public boolean isNewRecord()   { return isNewRecord; }
    public int getHighScore()      { return topScores.isEmpty() ? 0 : topScores.get(0); }
    public List<Integer> getTopScores() { return new ArrayList<>(topScores); }

    /** Đặt lại điểm về 0, dùng khi bắt đầu ván mới. */
    public void resetScore() {
        score       = 0;
        isNewRecord = false;
    }

    // ── Lưu / Tải file ───────────────────────────────────────────

    private void loadHighScores() {
        topScores.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                topScores.add(Integer.parseInt(line.trim()));
            }
            topScores.sort(Collections.reverseOrder());
        } catch (IOException | NumberFormatException ignored) {
            // File chưa tồn tại hoặc bị lỗi → bảng xếp hạng rỗng
        }
    }

    private void saveHighScores() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            for (int s : topScores) {
                writer.write(String.valueOf(s));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[ScoreManager] Không thể lưu điểm cao: " + e.getMessage());
        }
    }
}