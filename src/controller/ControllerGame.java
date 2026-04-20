package controller;

import model.Block;
import model.Board;
import model.GameState;
import model.ScoreManager;
import model.SoundManager;

import java.awt.Point;
import java.util.List;
import java.util.function.Consumer;

/**
 * ControllerGame — Điều phối toàn bộ luồng chơi đơn (Single Player).
 *
 * Quản lý:
 *   - Vòng đời game (bắt đầu, tạm dừng, tiếp tục, kết thúc)
 *   - Khay 3 khối hiện tại và logic thay khối mới
 *   - Điểm số và âm thanh
 */
public class ControllerGame {

    private Board        board;
    private ScoreManager scoreManager;
    private GameState    gameState;

    private Block[]   currentPieces;   // Khay 3 khối hiện tại
    private boolean[] isUsed;          // Đánh dấu khối đã được đặt trong lượt này
    private int       combo = 0;

    private Consumer<List<Point>> onLinesCleared; // Callback để phát hiệu ứng nhấp nháy

    public ControllerGame() {
        this.board        = new Board();
        this.scoreManager = new ScoreManager();
        this.gameState    = new GameState();
    }

    // ── Bắt đầu / Chơi lại ──────────────────────────────────────

    /** Khởi động ván chơi mới: đặt lại bảng, điểm, khay và bắt nhạc nền. */
    public void startGame() {
        board         = new Board();
        currentPieces = Block.generateUniqueBlocks();
        isUsed        = new boolean[]{false, false, false};
        combo         = 0;

        scoreManager.resetScore();
        gameState.startGame();
        SoundManager.startBGM("assets/bgm.wav");

        System.out.println("[Game] Bắt đầu! Trạng thái: " + gameState.getCurrent());
    }

    /** Dừng nhạc và khởi động lại ván mới. */
    public void restartGame() {
        SoundManager.stopBGM();
        startGame();
    }

    // ── Đặt khối ────────────────────────────────────────────────

    /**
     * Người chơi thả khối thứ {@code pieceIndex} vào ô (startX, startY) trên bảng.
     * Kiểm tra tính hợp lệ, cập nhật điểm số và kích hoạt xoá hàng nếu có.
     */
    public void placePiece(int pieceIndex, int startX, int startY) {
        if (!gameState.isPlaying()) return;
        if (isUsed[pieceIndex])     return;

        Block piece = currentPieces[pieceIndex];
        if (!board.canPlaceBlock(piece, startX, startY)) return;

        // Đặt khối vào lưới với ID màu theo vị trí trong khay (1, 2, 3)
        board.placeBlock(piece, startX, startY, pieceIndex + 1);
        isUsed[pieceIndex] = true;

        scoreManager.addScore(10); // +10đ mỗi khối đặt thành công
        SoundManager.playClick();

        // Xoá hàng / cột đầy và tính điểm combo
        List<Point> clearedPts = board.clearFullLines();
        if (!clearedPts.isEmpty()) {
            if (onLinesCleared != null) onLinesCleared.accept(clearedPts);
            combo++;
            int linesCleared = Math.max(1, clearedPts.size() / Board.SIZE);
            scoreManager.addClearScore(linesCleared, combo);

            if (combo >= 2) SoundManager.playCombo();
            else            SoundManager.playScore();

            System.out.printf("[Score] Xóa %d ô | Combo x%d | Điểm: %d%n",
                    clearedPts.size(), combo, scoreManager.getScore());
        } else {
            combo = 0;
        }

        // Sinh bộ khối mới khi người chơi dùng hết cả 3
        if (isUsed[0] && isUsed[1] && isUsed[2]) {
            currentPieces = Block.generateUniqueBlocks();
            isUsed        = new boolean[]{false, false, false};
            System.out.println("[Game] Sinh bộ khối mới.");
        }

        // Kiểm tra Game Over
        if (board.isGameOver(currentPieces, isUsed)) {
            triggerGameOver();
        }
    }

    // ── Tạm dừng / Tiếp tục ────────────────────────────────────

    public void pauseGame() {
        if (!gameState.isPlaying()) return;
        gameState.pauseGame();
        SoundManager.pauseBGM();
        System.out.println("[Game] Đã tạm dừng.");
    }

    public void resumeGame() {
        if (!gameState.isPaused()) return;
        gameState.resumeGame();
        SoundManager.resumeBGM();
        System.out.println("[Game] Tiếp tục chơi.");
    }

    // ── Về Menu ─────────────────────────────────────────────────

    public void goToMenu() {
        SoundManager.stopBGM();
        gameState.goToMenu();
        System.out.println("[Game] Về menu chính.");
    }

    // ── Game Over ────────────────────────────────────────────────

    private void triggerGameOver() {
        gameState.endGame();
        SoundManager.playGameOver();
        SoundManager.stopBGM();
        scoreManager.checkAndSaveScore();
        System.out.printf("[Game] GAME OVER! Điểm: %d | Kỷ lục: %d%n",
                scoreManager.getScore(), scoreManager.getHighScore());
    }

    // ── Getter ────────────────────────────────────────────────────

    public Board        getBoard()        { return board; }
    public ScoreManager getScoreManager() { return scoreManager; }
    public GameState    getGameState()    { return gameState; }
    public Block[]      getCurrentPieces(){ return currentPieces; }
    public boolean[]    getIsUsed()       { return isUsed; }
    public int          getCombo()        { return combo; }

    public void setOnLinesCleared(Consumer<List<Point>> r) { this.onLinesCleared = r; }
}