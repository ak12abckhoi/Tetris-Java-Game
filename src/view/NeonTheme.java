package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

/**
 * NeonTheme — Bộ màu sắc và phông chữ thống nhất cho giao diện Neon Tetris Blocks.
 *
 * Tất cả màu sắc và phông đều được định nghĩa ở đây để dễ bảo trì và thay đổi chủ đề.
 */
public class NeonTheme {

    // ── Màu nền ──────────────────────────────────────────────────

    public static final Color BACKGROUND = new Color(4, 6, 56);    // Xanh đêm đậm  #040638
    public static final Color SURFACE    = new Color(13, 17, 76);  // Bề mặt card    #0d114c

    // ── Màu Neon chính ───────────────────────────────────────────

    public static final Color PINK   = new Color(255, 140, 152); // Neon hồng  #ff8c98
    public static final Color CYAN   = new Color(0, 238, 252);   // Neon xanh  #00eefc
    public static final Color YELLOW = new Color(243, 255, 202); // Neon vàng  #f3ffca
    public static final Color LIME   = new Color(190, 238, 0);   // Neon lá    #beee00
    public static final Color PURPLE = new Color(164, 167, 222); // Neon tím   #a4a7de
    public static final Color ORANGE = new Color(255, 115, 133); // Neon cam   #ff7385

    // ── Màu phụ giao diện ────────────────────────────────────────

    public static final Color BUTTON_BG      = new Color(255, 165, 0);  // Cam cho nút "CHƠI NGAY"
    public static final Color TEXT_ON_BUTTON = new Color(100, 0, 28);   // Chữ trên nút (đỏ đậm)
    public static final Color TEXT_GOLD      = new Color(243, 255, 202);// Vàng nhạt neon

    // ── Phông chữ ─────────────────────────────────────────────────

    public static final Font MAIN_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font LOGO_FONT = new Font("SansSerif", Font.ITALIC | Font.BOLD, 72);

    // ── Hàm vẽ dùng chung ────────────────────────────────────────

    /**
     * Vẽ một ô gạch 3D có hiệu ứng neon sáng, bóng đổ và viền phản quang.
     *
     * @param g2d       Graphics2D để vẽ
     * @param x, y      Toạ độ góc trên trái
     * @param size      Kích thước cạnh ô (pixel)
     * @param baseColor Màu chủ đạo của khối
     * @param glowing   true = thêm hào quang neon xung quanh
     */
    public static void draw3DBlock(Graphics2D g2d, int x, int y, int size, Color baseColor, boolean glowing) {
        // Hào quang Neon (glow nhiều lớp dần mờ ra ngoài)
        if (glowing) {
            for (int i = 0; i < 4; i++) {
                g2d.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 60 - i * 15));
                g2d.fillRoundRect(x - i*2, y - i*2, size + i*4, size + i*4, 10, 10);
            }
        }

        // Bóng đổ (góc phải – dưới)
        g2d.setColor(baseColor.darker().darker());
        g2d.fillRoundRect(x + 2, y + 2, size, size, 8, 8);

        // Gradient mặt chính từ sáng → tối
        GradientPaint gp = new GradientPaint(x, y, baseColor, x + size, y + size, baseColor.darker());
        g2d.setPaint(gp);
        g2d.fillRoundRect(x, y, size, size, 8, 8);

        // Viền phản quang (góc trái – trên)
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.drawRoundRect(x + 1, y + 1, size - 2, size - 2, 6, 6);
    }

    /**
     * Vẽ viền nét đứt phát sáng xung quanh bảng chơi.
     *
     * @param color Màu viền (thường đổi theo lượt: CYAN cho người chơi, PINK cho AI)
     */
    public static void drawNeonDashedBorder(Graphics2D g2d, int x, int y, int w, int h, Color color) {
        Stroke oldStroke = g2d.getStroke();
        float[] dash = {4.0f, 4.0f};
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f));
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 120));
        g2d.drawRoundRect(x - 2, y - 2, w + 4, h + 4, 12, 12);
        g2d.setStroke(oldStroke);
    }

    /**
     * Vẽ hiệu ứng phát sáng mờ dần xung quanh một hình bất kỳ.
     *
     * @param blurRadius  Bán kính (số lớp) của glow
     */
    public static void drawGlow(Graphics2D g2d, Shape shape, Color color, int blurRadius) {
        for (int i = blurRadius; i > 0; i--) {
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40 / i));
            g2d.setStroke(new BasicStroke(i * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(shape);
        }
    }
}
