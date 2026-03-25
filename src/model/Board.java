package model;

public class Board {
    public static final int SIZE = 10; // Kích thước bảng 10x10
    private int[][] grid;

    public Board() {
        grid = new int[SIZE][SIZE]; // Khởi tạo toàn bộ là 0
    }

    public int[][] getGrid() {
        return grid;
    }

    // Kiểm tra tính hợp lệ khi đặt gạch tại tọa độ (startX, startY)
    public boolean canPlaceBlock(Block block, int startX, int startY) {
        int[][] shape = block.getShape();
        
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int boardX = startX + j;
                    int boardY = startY + i;
                    
                    // Kiểm tra tràn viền
                    if (boardX < 0 || boardX >= SIZE || boardY < 0 || boardY >= SIZE) {
                        return false; 
                    }
                    // Kiểm tra bị đè lên gạch đã có sẵn
                    if (grid[boardY][boardX] == 1) {
                        return false; 
                    }
                }
            }
        }
        return true; // Hợp lệ
    }

    // Đặt khối gạch vào bảng (Cập nhật ma trận)
    public void placeBlock(Block block, int startX, int startY) {
        int[][] shape = block.getShape();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    grid[startY + i][startX + j] = 1;
                }
            }
        }
    }

    // Quét và xóa các hàng/cột lấp đầy, trả về số ô đã xóa để tính điểm
    public int clearFullLines() {
        int clearedCells = 0;
        boolean[] rowsToClear = new boolean[SIZE];
        boolean[] colsToClear = new boolean[SIZE];

        // Xác định hàng nào đã đầy
        for (int i = 0; i < SIZE; i++) {
            boolean full = true;
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == 0) { full = false; break; }
            }
            if (full) rowsToClear[i] = true;
        }

        // Xác định cột nào đã đầy
        for (int j = 0; j < SIZE; j++) {
            boolean full = true;
            for (int i = 0; i < SIZE; i++) {
                if (grid[i][j] == 0) { full = false; break; }
            }
            if (full) colsToClear[j] = true;
        }

        // Thực hiện xóa và đếm số ô
        for (int i = 0; i < SIZE; i++) {
            if (rowsToClear[i]) {
                for (int j = 0; j < SIZE; j++) {
                    grid[i][j] = 0;
                    clearedCells++;
                }
            }
        }
        for (int j = 0; j < SIZE; j++) {
            if (colsToClear[j]) {
                for (int i = 0; i < SIZE; i++) {
                    if (grid[i][j] == 1) { // Tránh đếm trùng ô giao nhau giữa hàng và cột
                        grid[i][j] = 0;
                        clearedCells++;
                    }
                }
            }
        }
        return clearedCells;
    }

    // Thuật toán kiểm tra Game Over: Khối gạch hiện tại có thể đặt ở đâu?
    public boolean canPlaceAnyPosition(Block block) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (canPlaceBlock(block, x, y)) {
                    return true; // Chỉ cần tìm thấy 1 vị trí đặt được là False Game Over
                }
            }
        }
        return false;
    }

    // Kiểm tra tổng quát cho cả 3 khối trong khay
    public boolean isGameOver(Block[] currentBlocks, boolean[] isBlockUsed) {
        for (int i = 0; i < currentBlocks.length; i++) {
            if (!isBlockUsed[i]) { // Nếu khối này chưa được dùng
                if (canPlaceAnyPosition(currentBlocks[i])) {
                    return false; // Vẫn còn đường sống
                }
            }
        }
        return true; // Không khối nào đặt được -> Game Over
    }
}
