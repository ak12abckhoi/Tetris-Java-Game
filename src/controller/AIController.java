package controller;

import model.Block;
import model.Board;

import java.util.ArrayList;
import java.util.List;

/**
 * AIController — Thuật toán tìm kiếm nước đi cho AI trong chế độ đối kháng.
 *
 * Sử dụng Minimax kết hợp cắt tỉa Alpha-Beta với độ sâu MAX_DEPTH = 2:
 *   Tầng 1: AI chọn khối và vị trí đặt (tối đa hoá điểm)
 *   Tầng 2: Người chơi phản đòn (tối thiểu hoá — giả định người chơi cũng đi tối ưu)
 *   Tầng 3: Đánh giá Heuristic trạng thái bảng
 *
 * Heuristic: ưu tiên tối thiểu hoá "hố cụt" (ô bị chặn ≥ 3 phía)
 * và duy trì không gian mở để tiếp tục đấu.
 */
public class AIController {

    private final BattleController battleController;
    private boolean isStopped = false;

    // Độ sâu tìm kiếm: AI đi 1 lượt → Người chơi đi 1 lượt → đánh giá
    private final int MAX_DEPTH = 2;

    public AIController(BattleController battleController) {
        this.battleController = battleController;
    }

    /** Yêu cầu dừng luồng AI (dùng khi thoát hoặc bắt đầu ván mới). */
    public void stop() {
        isStopped = true;
    }

    /**
     * Tính toán nước đi tốt nhất trên luồng nền, sau đó thực thi trên Swing EDT.
     * Có độ trễ nhỏ (~800ms) để mô phỏng AI "đang suy nghĩ".
     */
    public void calculateAndMakeMoveAsync() {
        isStopped = false;
        Thread aiThread = new Thread(() -> {
            try {
                Thread.sleep(800); // Độ trễ giả lập suy nghĩ
            } catch (InterruptedException ignored) {}

            if (isStopped) return;

            // Tìm nước đi tốt nhất bằng Minimax với cắt tỉa Alpha-Beta
            Object[] bestMove = findBestMove(
                    battleController.getBoard(),
                    battleController.getSharedPieces(),
                    battleController.getSharedUsed(),
                    MAX_DEPTH
            );

            // bestMove = [pieceIndex, gridX, gridY]
            if (bestMove != null && bestMove.length == 3 && !isStopped) {
                int pIdx = (int) bestMove[0];
                int x    = (int) bestMove[1];
                int y    = (int) bestMove[2];
                javax.swing.SwingUtilities.invokeLater(() ->
                        battleController.executeAIMove(pIdx, x, y));
            }
            // Nếu bestMove == null → không còn nước đi hợp lệ → Game Over sẽ do Controller xử lý
        });
        aiThread.setDaemon(true);
        aiThread.start();
    }

    // ── Minimax ──────────────────────────────────────────────────

    /**
     * Tìm nước đi tốt nhất ở tầng gốc (lượt AI).
     * Trả về mảng [pieceIndex, x, y] hoặc null nếu không có nước đi.
     */
    private Object[] findBestMove(Board board, Block[] sharedP, boolean[] sharedU, int depth) {
        List<Move> validMoves = getAllValidMoves(board, sharedP, sharedU);
        if (validMoves.isEmpty()) return null;

        Move   bestMove  = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double alpha     = Double.NEGATIVE_INFINITY;
        double beta      = Double.POSITIVE_INFINITY;

        for (Move m : validMoves) {
            Board nextBoard = new Board(board);
            nextBoard.placeBlock(sharedP[m.pieceIndex], m.x, m.y);
            int clearLines = nextBoard.clearFullLines().size();

            boolean[] nextSharedU = sharedU.clone();
            nextSharedU[m.pieceIndex] = true;

            // Điểm = giá trị Minimax + thưởng ngay cho hàng/cột xoá được
            double score = alphaBeta(nextBoard, depth - 1, alpha, beta, false, sharedP, nextSharedU)
                         + (clearLines * 500);

            if (score > bestScore) {
                bestScore = score;
                bestMove  = m;
                alpha     = Math.max(alpha, bestScore);
            }
        }

        if (bestMove != null) return new Object[]{bestMove.pieceIndex, bestMove.x, bestMove.y};

        // Phương án dự phòng: chọn nước đầu tiên trong danh sách
        Move fallback = validMoves.get(0);
        return new Object[]{fallback.pieceIndex, fallback.x, fallback.y};
    }

    /**
     * Đệ quy Minimax với cắt tỉa Alpha-Beta.
     *
     * @param isMaximizingPlayer true = lượt AI (tối đa hoá), false = lượt Người chơi (tối thiểu hoá)
     */
    private double alphaBeta(Board board, int depth, double alpha, double beta,
                             boolean isMaximizingPlayer, Block[] sharedP, boolean[] sharedU) {
        // Điều kiện dừng: đã đạt độ sâu cực đại hoặc khay đã hết gạch
        // (không thể dự đoán lứa gạch sinh ngẫu nhiên tiếp theo → coi là nút lá)
        if (depth == 0 || (sharedU[0] && sharedU[1] && sharedU[2])) {
            return evaluateBoard(board);
        }

        if (isMaximizingPlayer) {
            // Lượt AI — tối đa hoá điểm số
            List<Move> moves = getAllValidMoves(board, sharedP, sharedU);
            if (moves.isEmpty()) return Double.NEGATIVE_INFINITY; // AI bị kẹt → tình huống tệ nhất

            double maxEval = Double.NEGATIVE_INFINITY;
            for (Move m : moves) {
                Board nextB = new Board(board);
                nextB.placeBlock(sharedP[m.pieceIndex], m.x, m.y);
                int cl = nextB.clearFullLines().size();

                boolean[] nextU = sharedU.clone();
                nextU[m.pieceIndex] = true;

                double eval = alphaBeta(nextB, depth - 1, alpha, beta, false, sharedP, nextU)
                            + (cl * 500);
                maxEval = Math.max(maxEval, eval);
                alpha   = Math.max(alpha, eval);
                if (beta <= alpha) break; // Cắt tỉa Beta
            }
            return maxEval;

        } else {
            // Lượt Người chơi — tối thiểu hoá (giả định người chơi cũng đi tối ưu)
            List<Move> moves = getAllValidMoves(board, sharedP, sharedU);
            if (moves.isEmpty()) return Double.POSITIVE_INFINITY; // Người chơi bị kẹt → AI thắng

            double minEval = Double.POSITIVE_INFINITY;
            for (Move m : moves) {
                Board nextB = new Board(board);
                nextB.placeBlock(sharedP[m.pieceIndex], m.x, m.y);
                int cl = nextB.clearFullLines().size();

                boolean[] nextU = sharedU.clone();
                nextU[m.pieceIndex] = true;

                // Trừ điểm khi Người chơi ăn được hàng (đó là thiệt hại đối với AI)
                double eval = alphaBeta(nextB, depth - 1, alpha, beta, true, sharedP, nextU)
                            - (cl * 500);
                minEval = Math.min(minEval, eval);
                beta    = Math.min(beta, eval);
                if (beta <= alpha) break; // Cắt tỉa Alpha
            }
            return minEval;
        }
    }

    // ── Heuristic ────────────────────────────────────────────────

    /**
     * Đánh giá chất lượng trạng thái bảng hiện tại bằng Heuristic.
     *
     * Tiêu chí:
     *   + Càng nhiều ô trống → bảng còn rộng → tốt
     *   − Càng nhiều "hố cụt" (ô bị chặn ≥ 3 mặt) → khó đặt → xấu
     */
    private double evaluateBoard(Board board) {
        int[][] grid       = board.getGrid();
        int gaps           = 0;
        int emptySpaces    = 0;

        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                if (grid[y][x] == 0) {
                    emptySpaces++;

                    // Đếm số mặt bị chặn (viền bảng hoặc gạch kề)
                    int blocked = 0;
                    if (y == 0             || grid[y-1][x] != 0) blocked++;
                    if (y == Board.SIZE-1  || grid[y+1][x] != 0) blocked++;
                    if (x == 0             || grid[y][x-1] != 0) blocked++;
                    if (x == Board.SIZE-1  || grid[y][x+1] != 0) blocked++;

                    // Ô bị chặn ≥ 3 mặt → hố cụt khó lấp
                    if (blocked >= 3) gaps++;
                }
            }
        }

        return (emptySpaces * 2) - (gaps * 50);
    }

    // ── Liệt kê nước đi ──────────────────────────────────────────

    /**
     * Liệt kê tất cả nước đi hợp lệ từ các khối chưa dùng trong khay.
     * Nếu khay rỗng (cả 3 đều đã dùng) → trả về danh sách rỗng;
     * thuật toán Minimax sẽ coi nút đó là nút lá.
     */
    private List<Move> getAllValidMoves(Board board, Block[] pieces, boolean[] used) {
        List<Move> valid = new ArrayList<>();
        for (int i = 0; i < pieces.length; i++) {
            if (used[i]) continue;
            for (int y = 0; y < Board.SIZE; y++) {
                for (int x = 0; x < Board.SIZE; x++) {
                    if (board.canPlaceBlock(pieces[i], x, y)) {
                        valid.add(new Move(i, x, y));
                    }
                }
            }
        }
        return valid;
    }

    // ── Lớp nội bộ ───────────────────────────────────────────────

    /** Đại diện cho một nước đi: chỉ số khối và toạ độ đặt trên bảng. */
    private static class Move {
        final int pieceIndex, x, y;
        Move(int pieceIndex, int x, int y) {
            this.pieceIndex = pieceIndex;
            this.x = x;
            this.y = y;
        }
    }
}
