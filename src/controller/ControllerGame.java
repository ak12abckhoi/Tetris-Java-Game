package controller;

import model.Block;
import model.Board;
import model.GameState;
import model.ScoreManager;
import model.SoundManager;

/**
 * ControllerGame — phần của Hà.
 * Quản lý toàn bộ luồng game, điểm số, âm thanh.
 * Chưa ghép View — thành viên phụ trách View sẽ bổ sung sau.
 */
public class ControllerGame {

    private Board        board;
    private ScoreManager scoreManager;
    private GameState    gameState;

    private Block[]   currentPieces;
    private boolean[] isUsed;
    private int       combo = 0;

    public ControllerGame() {
        this.board        = new Board();
        this.scoreManager = new ScoreManager();
        this.gameState    = new GameState();
    }

    // ── 1. BẮT ĐẦU / CHƠI LẠI ───────────────────────────────

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

    public void restartGame() {
        SoundManager.stopBGM();
        startGame();
    }

    // ── 2. ĐẶT KHỐI ──────────────────────────────────────────

    /**
     * Gọi khi người dùng thả khối vào vị trí (startX, startY) trên bảng.
     * @param pieceIndex  khối thứ mấy trong khay (0, 1, 2)
     * @param startX      cột bắt đầu
     * @param startY      hàng bắt đầu
     */
    public void placePiece(int pieceIndex, int startX, int startY) {
        if (!gameState.isPlaying()) return;
        if (isUsed[pieceIndex])     return;

        Block piece = currentPieces[pieceIndex];

        // Kiểm tra hợp lệ
        if (!board.canPlaceBlock(piece, startX, startY)) return;

        // Đặt khối
        board.placeBlock(piece, startX, startY);
        isUsed[pieceIndex] = true;

        // Điểm đặt khối: 10đ mỗi khối
        scoreManager.addScore(10);
        SoundManager.playClick();

        // Xóa hàng/cột đầy
        int clearedCells = board.clearFullLines();
        if (clearedCells > 0) {
            combo++;
            int linesCleared = Math.max(1, clearedCells / Board.SIZE);
            scoreManager.addClearScore(linesCleared, combo);

            if (combo >= 2) SoundManager.playCombo();
            else            SoundManager.playScore();

            System.out.printf("[Score] Xóa %d ô | Combo x%d | Điểm: %d%n",
                    clearedCells, combo, scoreManager.getScore());
        } else {
            combo = 0;
        }

        // Sinh bộ khối mới nếu dùng hết 3
        if (isUsed[0] && isUsed[1] && isUsed[2]) {
            currentPieces = Block.generateUniqueBlocks();
            isUsed        = new boolean[]{false, false, false};
            System.out.println("[Game] Sinh bộ khối mới.");
        }

        // Kiểm tra game over
        if (board.isGameOver(currentPieces, isUsed)) {
            triggerGameOver();
        }
    }

    // ── 3. PAUSE / RESUME ─────────────────────────────────────

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

    // ── 4. VỀ MENU ────────────────────────────────────────────

    public void goToMenu() {
        SoundManager.stopBGM();
        gameState.goToMenu();
        System.out.println("[Game] Về menu chính.");
    }

    // ── 5. GAME OVER ──────────────────────────────────────────

    private void triggerGameOver() {
        gameState.endGame();
        SoundManager.playGameOver();
        SoundManager.stopBGM();
        System.out.printf("[Game] GAME OVER! Điểm: %d | Kỷ lục: %d%n",
                scoreManager.getScore(), scoreManager.getHighScore());
    }

    // ── GETTERS (cho View dùng sau) ───────────────────────────

    public Board        getBoard()        { return board; }
    public ScoreManager getScoreManager() { return scoreManager; }
    public GameState    getGameState()    { return gameState; }
    public Block[]      getCurrentPieces(){ return currentPieces; }
    public boolean[]    getIsUsed()       { return isUsed; }
    public int          getCombo()        { return combo; }

}