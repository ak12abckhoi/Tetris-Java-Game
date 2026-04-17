package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

/**
 * NeonTheme defines the color palette and fonts for the Neon Edition of Tetris.
 */
public class NeonTheme {
    // Core Colors
    public static final Color BACKGROUND = new Color(4, 6, 56);      // #040638
    public static final Color SURFACE = new Color(13, 17, 76);        // #0d114c
    
    // Neon Colors
    public static final Color PINK = new Color(255, 140, 152);       // #ff8c98
    public static final Color CYAN = new Color(0, 238, 252);         // #00eefc
    public static final Color YELLOW = new Color(243, 255, 202);     // #f3ffca
    public static final Color LIME = new Color(190, 238, 0);         // #beee00
    public static final Color PURPLE = new Color(164, 167, 222);     // #a4a7de
    public static final Color ORANGE = new Color(255, 115, 133);     // #ff7385
    
    // UI Colors
    public static final Color BUTTON_BG = new Color(255, 165, 0);     // Orange for "CHƠI NGAY"
    public static final Color TEXT_ON_BUTTON = new Color(100, 0, 28); // Dark red/burgundy
    public static final Color TEXT_GOLD = new Color(243, 255, 202);   // #f3ffca
    
    // Fonts (Approximation of Material/Jakarta Sans)
    public static final Font MAIN_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font LOGO_FONT = new Font("SansSerif", Font.ITALIC | Font.BOLD, 72);

    /**
     * Draws a block with a 3D bevel effect and neon glow.
     */
    public static void draw3DBlock(Graphics2D g2d, int x, int y, int size, Color baseColor, boolean glowing) {
        // Draw glow if glowing
        if (glowing) {
            for (int i = 0; i < 4; i++) {
                g2d.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 60 - i * 15));
                g2d.fillRoundRect(x - i*2, y - i*2, size + i*4, size + i*4, 10, 10);
            }
        }

        // Draw shadow (bottom right)
        g2d.setColor(baseColor.darker().darker());
        g2d.fillRoundRect(x + 2, y + 2, size, size, 8, 8);

        // Draw base center gradient
        GradientPaint gp = new GradientPaint(x, y, baseColor, x + size, y + size, baseColor.darker());
        g2d.setPaint(gp);
        g2d.fillRoundRect(x, y, size, size, 8, 8);

        // Draw inner highlight (top left)
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.drawRoundRect(x + 1, y + 1, size - 2, size - 2, 6, 6);
    }

    /**
     * Draws a subtle outer dashed border for glowing grid cells or selections.
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
     * Draws a generic glow effect around a shape.
     */
    public static void drawGlow(Graphics2D g2d, Shape shape, Color color, int blurRadius) {
        for (int i = blurRadius; i > 0; i--) {
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40 / i));
            g2d.setStroke(new BasicStroke(i * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(shape);
        }
    }
}
