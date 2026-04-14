package view;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class NeonTheme {
    // --- BẢNG MÀU CHÍNH (Trích xuất từ Tailwind Config) ---
    public static final Color BG_SURFACE = new Color(4, 6, 56);       // #040638
    public static final Color SURFACE_CONTAINER = new Color(13, 17, 76); // #0d114c
    public static final Color PRIMARY_PINK = new Color(255, 140, 152);   // #ff8c98
    public static final Color SECONDARY_CYAN = new Color(0, 238, 252);   // #00eefc
    public static final Color TERTIARY_YELLOW = new Color(190, 238, 0);  // #beee00
    public static final Color OUTLINE = new Color(110, 113, 165);       // #6e71a5
    
    // --- MÀU GRADIENT CHO KHỐI 3D ---
    public static final Color ORANGE_START = new Color(255, 140, 152);
    public static final Color ORANGE_END = new Color(255, 87, 114);
    public static final Color PURPLE_START = new Color(164, 167, 222);
    public static final Color PURPLE_END = new Color(110, 113, 165);

    // --- CẤU HÌNH BO GÓC ---
    public static final int ROUNDNESS_LG = 16;
    public static final int ROUNDNESS_XL = 32;

    // --- HÀM HELPER: KHỬ RĂNG CƯA (Antialiasing) ---
    public static void applyQualityRendering(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // --- HÀM HELPER: VẼ HIỆU ỨNG PHÁT SÁNG (Neon Glow) ---
    public static void drawNeonGlow(Graphics2D g2d, Shape shape, Color color, int thickness) {
        for (int i = thickness; i > 0; i--) {
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
            g2d.setStroke(new BasicStroke(i * 2));
            g2d.draw(shape);
        }
    }
}