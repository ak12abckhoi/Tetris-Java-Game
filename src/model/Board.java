package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Board — Quản lý lưới bảng chơi 10×10.
 *
 * Quy ước giá trị ô:
 *   0     = ô trống
 *   1–3   = ID màu sắc của khối gạch đã được đặt
 *            (1 = Cyan, 2 = Yellow, 3 = Pink — theo thứ tự khay)
 */
public class Board {

    public static final int SIZE = 10;
    private int[][] grid;

    /** Khởi tạo bảng trống 10×10. */
    public Board() {
        grid = new int[SIZE][SIZE];
    }

    /**
     * Sao chép sâu (deep copy) toàn bộ bảng.
     * Dùng khi AI mô phỏng nước đi mà không làm ảnh hưởng tới bảng thật.
     */
    public Board(Board other) {
        this.grid = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(other.grid[i], 0, this.grid[i], 0, SIZE);
        }
    }

    /** Trả về tham chiếu trực tiếp tới lưới — dùng cho lớp vẽ (View). */
    public int[][] getGrid() {
        return grid;
    }

    /**
     * Kiểm tra xem có thể đặt khối tại vị trí (startX, startY) hay không.
     * Trả về false nếu: tràn biên bảng, hoặc đè lên ô đã có gạch.
     */
    public boolean canPlaceBlock(Block block, int startX, int startY) {
        int[][] shape = block.getShape();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int bx = startX + j;
                    int by = startY + i;
                    // Kiểm tra tràn biên
                    if (bx < 0 || bx >= SIZE || by < 0 || by >= SIZE) return false;
                    // Kiểm tra đè lên ô đã có gạch
                    if (grid[by][bx] != 0) return false;
                }
            }
        }
        return true;
    }

    /**
     * Đặt khối lên bảng với ID màu chỉ định (colorId: 1–3).
     * Giá trị colorId được lưu trong grid để lớp vẽ dùng đúng màu sắc.
     */
    public void placeBlock(Block block, int startX, int startY, int colorId) {
        int[][] shape = block.getShape();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    grid[startY + i][startX + j] = colorId;
                }
            }
        }
    }

    /**
     * Đặt khối lên bảng với colorId mặc định = 1.
     * Dùng trong Minimax vì chỉ cần biết "có gạch", không cần phân biệt màu.
     */
    public void placeBlock(Block block, int startX, int startY) {
        placeBlock(block, startX, startY, 1);
    }

    /**
     * Quét và xoá các hàng / cột đã lấp đầy.
     * Trả về danh sách toạ độ các ô bị xoá — dùng để hiển thị hiệu ứng nhấp nháy.
     *
     * Lưu ý: Ô nằm tại giao điểm hàng-cột đầy sẽ chỉ xuất hiện một lần trong danh sách.
     */
    public List<Point> clearFullLines() {
        List<Point> clearedPoints = new ArrayList<>();
        boolean[] rowsToClear = new boolean[SIZE];
        boolean[] colsToClear = new boolean[SIZE];

        // Xác định hàng đầy
        for (int i = 0; i < SIZE; i++) {
            boolean full = true;
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == 0) { full = false; break; }
            }
            if (full) rowsToClear[i] = true;
        }

        // Xác định cột đầy
        for (int j = 0; j < SIZE; j++) {
            boolean full = true;
            for (int i = 0; i < SIZE; i++) {
                if (grid[i][j] == 0) { full = false; break; }
            }
            if (full) colsToClear[j] = true;
        }

        // Xoá hàng đầy và ghi nhận toạ độ
        for (int i = 0; i < SIZE; i++) {
            if (rowsToClear[i]) {
                for (int j = 0; j < SIZE; j++) {
                    if (grid[i][j] != 0) {
                        clearedPoints.add(new Point(j, i));
                        grid[i][j] = 0;
                    }
                }
            }
        }

        // Xoá cột đầy và ghi nhận toạ độ (bỏ qua ô đã xoá bởi bước trên)
        for (int j = 0; j < SIZE; j++) {
            if (colsToClear[j]) {
                for (int i = 0; i < SIZE; i++) {
                    if (grid[i][j] != 0) {
                        clearedPoints.add(new Point(j, i));
                        grid[i][j] = 0;
                    }
                }
            }
        }
        return clearedPoints;
    }

    /**
     * Kiểm tra xem một khối có thể đặt vừa vào BẤT KỲ vị trí nào trên bảng không.
     * Dùng để xác định trạng thái "hết nước đi" cho một khối đơn lẻ.
     */
    public boolean canPlaceAnyPosition(Block block) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (canPlaceBlock(block, x, y)) return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra Game Over: trả về true nếu KHÔNG CÒN khối nào trong khay
     * (chưa sử dụng) có thể đặt vừa vào bảng hiện tại.
     */
    public boolean isGameOver(Block[] currentBlocks, boolean[] isBlockUsed) {
        for (int i = 0; i < currentBlocks.length; i++) {
            if (!isBlockUsed[i] && canPlaceAnyPosition(currentBlocks[i])) {
                return false; // Vẫn còn ít nhất một nước đi hợp lệ
            }
        }
        return true; // Không còn nước đi nào → Game Over
    }
}
