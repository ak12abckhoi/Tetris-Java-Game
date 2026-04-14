package model;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Block {
    private int[][] shape;

    // Constructor
    public Block(int type) {
        this.shape = getShapeByType(type);
    }

    public int[][] getShape() {
        // Trả về bản sao để bảo vệ dữ liệu nội bộ
        int[][] copy = new int[shape.length][];
        for (int i = 0; i < shape.length; i++) {
            copy[i] = shape[i].clone();
        }
        return copy;
    }

    // Định nghĩa các loại hình dáng khối gạch (1 là có gạch, 0 là trống)
    private int[][] getShapeByType(int type) {
        switch (type) {
            case 1: return new int[][]{{1}}; // 1 ô đơn
            case 2: return new int[][]{{1, 1}}; // 2 ô ngang
            case 3: return new int[][]{{1}, {1}}; // 2 ô dọc
            case 4: return new int[][]{{1, 0}, {1, 1}}; // L nhỏ
            case 5: return new int[][]{{1, 1}, {0, 1}}; // L nhỏ ngược
            case 6: return new int[][]{{1, 1}, {1, 1}}; // Hình vuông 2x2
            case 7: return new int[][]{{1}, {1}, {1}}; // 3 ô dọc
            case 8: return new int[][]{{1, 0}, {1, 0}, {1, 1}}; // L 
            case 9: return new int[][]{{1, 1}, {0, 1}, {0, 1}}; // L ngược
            case 10: return new int[][]{{0, 1}, {1, 1}, {0, 1}}; // T ngang trái
            case 11: return new int[][]{{1, 0}, {1, 1}, {1, 0}}; // T ngang phải
            case 12: return new int[][]{{1, 1, 0}, {0, 1, 1}}; // Z
            case 13: return new int[][]{{0, 1, 1}, {1, 1, 0}}; // Z ngược
            case 14: return new int[][]{{1, 1, 1}}; // 3 ô ngang
            case 15: return new int[][]{{1, 1, 1}, {1, 1, 1}, {1, 1, 1}}; // Vuông 3x3
            case 16: return new int[][]{{1}, {1}, {1}, {1}}; // 4 ô dọc
            case 17: return new int[][]{{1, 1, 1, 1}}; // 4 ô ngang
            default: return new int[][]{{1}}; 
        }
    }

    // Hàm sinh 3 khối ngẫu nhiên không trùng lặp cho khay chờ
    public static Block[] generateUniqueBlocks() {
        // Chọn 3 số ngẫu nhiên không trùng trong [1..17] — hiệu quả hơn shuffle
        Set<Integer> chosen = new HashSet<>();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        while (chosen.size() < 3) {
            chosen.add(rng.nextInt(1, 18)); // [1, 17]
        }

        Block[] blocks = new Block[3];
        int idx = 0;
        for (int type : chosen) {
            blocks[idx++] = new Block(type);
        }
        return blocks;
    }
}
