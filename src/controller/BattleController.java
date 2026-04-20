package controller;

import model.Block;
import model.Board;
import model.ScoreManager;
import model.SoundManager;

import java.awt.Point;
import java.util.List;
import java.util.function.Consumer;

/**
 * BattleController — Điều phối trận đấu luân phiên (Turn-based Shared Board).
 *
 * Hai bên (Người chơi và AI) cùng chơi trên một bảng 10×10 duy nhất.
 * Luân phiên mỗi bên chọn một khối từ khay chung gồm 3 mảnh.
 *
 * Kết thúc ván: khi không còn khối nào trong khay đặt vừa vào bảng.
 * Người thắng: ai có điểm cao hơn; bằng điểm → Hòa.
 */
public class BattleController {

    public enum Result { NONE, PLAYER_WIN, AI_WIN, DRAW }

    // ── Mô hình ──────────────────────────────────────────────────

    private Board board;
    private ScoreManager playerScoreManager;
    private ScoreManager aiScoreManager;

    private Block[]   sharedPieces;     // Khay chung 3 khối
    private boolean[] sharedUsed;       // Đánh dấu khối nào đã được sử dụng

    private int playerCombo = 0;
    private int aiCombo     = 0;

    private boolean isPlayerTurn;
    private Result  result = Result.NONE;

    // ── Callback UI ───────────────────────────────────────────────

    private AIController          aiController;
    private Runnable              onTick;          // Kích hoạt vẽ lại UI
    private Runnable              onBattleEnd;     // Thông báo kết thúc trận
    private Runnable              onTurnChange;    // Thông báo đổi lượt
    private Consumer<List<Point>> onLinesCleared;  // Gửi toạ độ hiệu ứng nhấp nháy

    // ── Khởi tạo ─────────────────────────────────────────────────

    public BattleController() {
        board               = new Board();
        playerScoreManager  = new ScoreManager();
        aiScoreManager      = new ScoreManager();
        aiController        = new AIController(this);
    }

    /**
     * Quá tải tương thích ngược — tham số dummyDuration không được dùng.
     */
    public void startBattle(int dummyDuration) {
        startBattleLocal();
    }

    /** Khởi động trận đấu mới: đặt lại bảng, điểm, khay và trao lượt cho Người chơi. */
    public void startBattleLocal() {
        board = new Board();
        playerScoreManager.resetScore();
        aiScoreManager.resetScore();
        playerCombo = 0;
        aiCombo     = 0;

        sharedPieces = Block.generateUniqueBlocks();
        sharedUsed   = new boolean[]{false, false, false};

        result       = Result.NONE;
        isPlayerTurn = true; // Người chơi luôn đi trước

        SoundManager.startBGM("assets/bgm.wav");

        if (onTurnChange != null) onTurnChange.run();
    }

    /** Dừng trận đấu và giải phóng tài nguyên (dùng khi người chơi thoát giữa chừng). */
    public void stopBattle() {
        if (aiController != null) aiController.stop();
        SoundManager.stopBGM();
        result = Result.NONE;
    }

    // ── Đặt khối ────────────────────────────────────────────────

    /**
     * Người chơi kéo thả một khối vào (gridX, gridY).
     * Trả về true nếu hợp lệ và đã đặt thành công.
     */
    public boolean placePiecePlayer(int pieceIndex, int gridX, int gridY) {
        if (!isPlayerTurn || result != Result.NONE || sharedUsed[pieceIndex]) return false;

        Block p = sharedPieces[pieceIndex];
        if (!board.canPlaceBlock(p, gridX, gridY)) return false;

        board.placeBlock(p, gridX, gridY, pieceIndex + 1);
        sharedUsed[pieceIndex] = true;
        SoundManager.playClick();

        // Kiểm tra và xoá hàng/cột đầy
        List<Point> clearedPts = board.clearFullLines();
        if (!clearedPts.isEmpty()) {
            if (onLinesCleared != null) onLinesCleared.accept(clearedPts);
            playerCombo++;
            int lines = Math.max(1, clearedPts.size() / Board.SIZE);
            playerScoreManager.addClearScore(lines, playerCombo);
            if (playerCombo >= 2) SoundManager.playCombo();
            else                  SoundManager.playScore();
        } else {
            playerCombo = 0;
        }

        playerScoreManager.addScore(10); // +10đ cho mỗi lần đặt khối thành công

        // Sinh lứa gạch mới nếu khay đã dùng hết cả 3
        if (sharedUsed[0] && sharedUsed[1] && sharedUsed[2]) {
            sharedPieces = Block.generateUniqueBlocks();
            sharedUsed   = new boolean[]{false, false, false};
        }

        checkGameOverAndSwitchTurn();
        return true;
    }

    /**
     * AI thực hiện nước đi tại (pieceIndex, gridX, gridY).
     * Gọi từ AIController trên Swing Event Thread.
     */
    public void executeAIMove(int pieceIndex, int gridX, int gridY) {
        // Bảo vệ luồng: không cho AI đi khi không phải lượt của nó
        if (result != Result.NONE || isPlayerTurn || sharedUsed[pieceIndex]) return;

        Block p = sharedPieces[pieceIndex];
        board.placeBlock(p, gridX, gridY, pieceIndex + 1);
        sharedUsed[pieceIndex] = true;
        SoundManager.playClick();

        List<Point> clearedPts = board.clearFullLines();
        if (!clearedPts.isEmpty()) {
            if (onLinesCleared != null) onLinesCleared.accept(clearedPts);
            aiCombo++;
            int lines = Math.max(1, clearedPts.size() / Board.SIZE);
            aiScoreManager.addClearScore(lines, aiCombo);
            if (aiCombo >= 2) SoundManager.playCombo();
            else              SoundManager.playScore();
        } else {
            aiCombo = 0;
        }

        aiScoreManager.addScore(10); // +10đ cho mỗi lần AI đặt khối thành công

        // Sinh lứa gạch mới nếu AI dùng hết khay chung
        if (sharedUsed[0] && sharedUsed[1] && sharedUsed[2]) {
            sharedPieces = Block.generateUniqueBlocks();
            sharedUsed   = new boolean[]{false, false, false};
        }

        checkGameOverAndSwitchTurn();
    }

    // ── Logic đổi lượt và kết thúc ───────────────────────────────

    private void checkGameOverAndSwitchTurn() {
        if (result != Result.NONE) return;

        isPlayerTurn = !isPlayerTurn;
        if (onTurnChange != null) javax.swing.SwingUtilities.invokeLater(onTurnChange);

        // Kiểm tra xem khay hiện tại còn có thể đặt vào bảng không
        if (board.isGameOver(sharedPieces, sharedUsed)) {
            // Ai nhiều điểm hơn thì thắng
            int pScore = getPlayerScore();
            int aScore = getAiScore();
            if      (pScore > aScore) result = Result.PLAYER_WIN;
            else if (aScore > pScore) result = Result.AI_WIN;
            else                      result = Result.DRAW;
            endBattle();
        } else if (!isPlayerTurn) {
            // Chưa kết thúc và đến lượt AI → uỷ quyền tính toán
            aiController.calculateAndMakeMoveAsync();
        }
    }

    /**
     * Ép kết thúc trận — dùng khi người chơi thoát giữa chừng.
     * AI được tính là thắng mặc định trong trường hợp này.
     */
    public void notifyPlayerGameOver() {
        if (result != Result.NONE) return;
        result = Result.AI_WIN;
        endBattle();
    }

    private void endBattle() {
        if (aiController != null) aiController.stop();
        playerScoreManager.checkAndSaveScore();
        SoundManager.stopBGM();
        SoundManager.playGameOver();
        if (onBattleEnd != null) javax.swing.SwingUtilities.invokeLater(onBattleEnd);
    }

    // ── Getter ────────────────────────────────────────────────────

    public Board        getBoard()               { return board; }
    public ScoreManager getPlayerScoreManager()  { return playerScoreManager; }
    public AIController getAIController()        { return aiController; }

    public int      getPlayerScore() { return playerScoreManager.getScore(); }
    public int      getAiScore()     { return aiScoreManager.getScore(); }
    public Block[]  getSharedPieces(){ return sharedPieces; }
    public boolean[]getSharedUsed()  { return sharedUsed; }
    public boolean  isPlayerTurn()   { return isPlayerTurn; }
    public Result   getResult()      { return result; }

    // ── Setter callback ───────────────────────────────────────────

    public void setOnTurnChange(Runnable r)              { this.onTurnChange    = r; }
    public void setOnTick(Runnable r)                    { this.onTick          = r; }
    public void setOnBattleEnd(Runnable r)               { this.onBattleEnd     = r; }
    public void setOnLinesCleared(Consumer<List<Point>> r){ this.onLinesCleared = r; }

    // ── Tương thích ngược ─────────────────────────────────────────

    /** Không còn dùng timer → trả về 0 để tránh lỗi biên dịch với code cũ. */
    public int    getSecondsLeft()   { return 0; }

    /** Mô tả lượt hiện tại bằng màu sắc — dùng để debug nhanh. */
    public String getFormattedTime() { return isPlayerTurn ? "BẠN MÀU XANH" : "AI MÀU HỒNG"; }
}
