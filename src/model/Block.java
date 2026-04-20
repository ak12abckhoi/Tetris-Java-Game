package model;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Block — Đại diện cho một khối gạch trong trò chơi.
 *
 * Có 17 loại hình dáng khác nhau, từ đơn giản (1 ô) đến phức tạp (3×3).
 * Hình dạng (shape) lưu dưới dạng ma trận 2D:  1 = có gạch,  0 = trống.
 */
public class Block {

    private int[][] shape;

    public Block(int type) {
        this.shape = getShapeByType(type);
    }

    /**
     * Trả về bản sao hình dạng để tránh code bên ngoài vô tình thay đổi dữ liệu nội bộ.
     */
    public int[][] getShape() {
        int[][] copy = new int[shape.length][];
        for (int i = 0; i < shape.length; i++) {
            copy[i] = shape[i].clone();
        }
        return copy;
    }

    /** Ánh xạ loại khối (type: 1–17) sang ma trận hình dạng tương ứng. */
    private int[][] getShapeByType(int type) {
        switch (type) {
            case 1:  return new int[][]{{1}};                              // 1 ô đơn
            case 2:  return new int[][]{{1, 1}};                           // 2 ô ngang
            case 3:  return new int[][]{{1}, {1}};                         // 2 ô dọc
            case 4:  return new int[][]{{1, 0}, {1, 1}};                   // L nhỏ
            case 5:  return new int[][]{{1, 1}, {0, 1}};                   // J nhỏ
            case 6:  return new int[][]{{1, 1}, {1, 1}};                   // Vuông 2×2
            case 7:  return new int[][]{{1}, {1}, {1}};                    // 3 ô dọc
            case 8:  return new int[][]{{1, 0}, {1, 0}, {1, 1}};           // L
            case 9:  return new int[][]{{1, 1}, {0, 1}, {0, 1}};           // J
            case 10: return new int[][]{{0, 1}, {1, 1}, {0, 1}};           // T (trỏ phải)
            case 11: return new int[][]{{1, 0}, {1, 1}, {1, 0}};           // T (trỏ trái)
            case 12: return new int[][]{{1, 1, 0}, {0, 1, 1}};             // Z
            case 13: return new int[][]{{0, 1, 1}, {1, 1, 0}};             // S
            case 14: return new int[][]{{1, 1, 1}};                        // 3 ô ngang
            case 15: return new int[][]{{1, 1, 1}, {1, 1, 1}, {1, 1, 1}}; // Vuông 3×3
            case 16: return new int[][]{{1}, {1}, {1}, {1}};               // 4 ô dọc
            case 17: return new int[][]{{1, 1, 1, 1}};                     // 4 ô ngang
            default: return new int[][]{{1}};
        }
    }

    /**
     * Sinh ngẫu nhiên 3 khối KHÔNG trùng lặp từ tập 17 loại.
     * Được dùng khi tạo khay mới cho cả chế độ chơi đơn và chế độ đối kháng.
     */
    public static Block[] generateUniqueBlocks() {
        Set<Integer> chosen = new HashSet<>();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        while (chosen.size() < 3) {
            chosen.add(rng.nextInt(1, 18)); // nextInt(1, 18) → kết quả nằm trong [1, 17]
        }
        Block[] blocks = new Block[3];
        int idx = 0;
        for (int type : chosen) {
            blocks[idx++] = new Block(type);
        }
        return blocks;
    }
}
