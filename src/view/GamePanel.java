package view;

import controller.ControllerGame;
import model.Block;
import model.Board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel {
    private MainContainer parent;
    private ControllerGame controller;
    private int CELL_SIZE = 30;
    private int BOARD_X = 75;
    private int BOARD_Y = 160; // Lowered to make room for score
    
    private Color[] slotColors = {NeonTheme.CYAN, NeonTheme.YELLOW, NeonTheme.PINK};
    
    // Drag & Drop state
    private int draggedPieceIndex = -1;
    private int dragX = -1;
    private int dragY = -1;
    private int startDragX = -1;
    private int startDragY = -1;
    private int trayX[], trayY[]; // Positions for the 3 tray elements
    private Timer snapBackTimer;
    private double currentSnapX, currentSnapY;

    public GamePanel(MainContainer parent) {
        this.parent = parent;
        setLayout(null);
        setBackground(NeonTheme.BACKGROUND);
        
        // Setup Controller - Ideally passed from constructor but instantiate here for simplicity now
        controller = new ControllerGame();
        controller.startGame();

        trayX = new int[]{50, 180, 310}; // Distributed evenly roughly
        trayY = new int[]{600, 600, 600};

        setupMouseListeners();
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Check if clicked in any tray slot
                Block[] pieces = controller.getCurrentPieces();
                boolean[] used = controller.getIsUsed();

                for (int i = 0; i < 3; i++) {
                    if (used[i]) continue;
                    Block p = pieces[i];
                    Rectangle bounds = getPieceBounds(p, trayX[i], trayY[i]);
                    if (bounds.contains(e.getPoint())) {
                        draggedPieceIndex = i;
                        dragX = e.getX() - bounds.width / 2;
                        dragY = e.getY() - bounds.height / 2;
                        startDragX = trayX[i];
                        startDragY = trayY[i];
                        repaint();
                        break;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (draggedPieceIndex != -1) {
                    Block p = controller.getCurrentPieces()[draggedPieceIndex];
                    // Calculate board coordinates
                    int gridX = Math.round((float)(dragX - BOARD_X) / CELL_SIZE);
                    int gridY = Math.round((float)(dragY - BOARD_Y) / CELL_SIZE);

                    if (controller.getBoard().canPlaceBlock(p, gridX, gridY)) {
                        controller.placePiece(draggedPieceIndex, gridX, gridY);
                        draggedPieceIndex = -1; // Reset drag
                    } else {
                        startSnapBackAnimation();
                    }
                    repaint();
                } else if (controller.getGameState().isGameOver()) {
                    handleGameOverClicks(e.getPoint());
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedPieceIndex != -1) {
                    Block p = controller.getCurrentPieces()[draggedPieceIndex];
                    int pw = p.getShape()[0].length * CELL_SIZE;
                    int ph = p.getShape().length * CELL_SIZE;
                    dragX = e.getX() - pw / 2;
                    dragY = e.getY() - ph / 2;
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (controller.getGameState().isGameOver()) {
                    if (isOverGameOverButton(e.getPoint())) {
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                } else {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }

    private boolean isOverGameOverButton(Point p) {
        int bannerY = BOARD_Y + (Board.SIZE * CELL_SIZE) / 2 - 60;
        int my = bannerY + 110;
        int rBoxY = my + 195;
        Rectangle playAgainRect = new Rectangle((getWidth() - 300) / 2, rBoxY + 70, 300, 50);
        Rectangle homeRect = new Rectangle((getWidth() - 300) / 2, rBoxY + 130, 300, 50);
        return playAgainRect.contains(p) || homeRect.contains(p);
    }

    private void startSnapBackAnimation() {
        currentSnapX = dragX;
        currentSnapY = dragY;
        
        final int targetX = startDragX;
        final int targetY = startDragY;

        snapBackTimer = new Timer(15, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Linear interpolation towards target
                double dx = targetX - currentSnapX;
                double dy = targetY - currentSnapY;

                if (Math.abs(dx) < 2 && Math.abs(dy) < 2) {
                    draggedPieceIndex = -1;
                    ((Timer)e.getSource()).stop();
                } else {
                    currentSnapX += dx * 0.2; // 20% speed per frame
                    currentSnapY += dy * 0.2;
                    dragX = (int)currentSnapX;
                    dragY = (int)currentSnapY;
                }
                repaint();
            }
        });
        snapBackTimer.start();
    }

    private Rectangle getPieceBounds(Block block, int x, int y) {
        int w = block.getShape()[0].length * CELL_SIZE;
        int h = block.getShape().length * CELL_SIZE;
        return new Rectangle(x, y, w, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBoard(g2d);
        drawTrayPieces(g2d);
        
        // Draw dashed line and ghost ONLY when dragging (and not animating snapback)
        if (draggedPieceIndex != -1 && (snapBackTimer == null || !snapBackTimer.isRunning())) {
            Block activePiece = controller.getCurrentPieces()[draggedPieceIndex];
            
            // Draw Dashed Line
            int pieceCenterX = dragX + (activePiece.getShape()[0].length * CELL_SIZE) / 2;
            int pieceCenterY = dragY + (activePiece.getShape().length * CELL_SIZE) / 2;
            int trayCenterX = startDragX + (activePiece.getShape()[0].length * CELL_SIZE) / 2;
            int trayCenterY = startDragY + (activePiece.getShape().length * CELL_SIZE) / 2;
            
            Stroke old = g2d.getStroke();
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6}, 0));
            g2d.setColor(new Color(255, 140, 152, 100)); // Faded pink trace
            g2d.drawLine(trayCenterX, trayCenterY, pieceCenterX, pieceCenterY);
            g2d.setStroke(old);

            // Draw Ghost Block
            int gridX = Math.round((float)(dragX - BOARD_X) / CELL_SIZE);
            int gridY = Math.round((float)(dragY - BOARD_Y) / CELL_SIZE);
            
            if (controller.getBoard().canPlaceBlock(activePiece, gridX, gridY)) {
                drawGhostBlock(g2d, activePiece, BOARD_X + gridX * CELL_SIZE, BOARD_Y + gridY * CELL_SIZE);
            }

            // Draw actully dragged block
            drawPiece(g2d, activePiece, dragX, dragY, true, slotColors[draggedPieceIndex]);
        } else if (draggedPieceIndex != -1 && snapBackTimer != null && snapBackTimer.isRunning()) {
            Block activePiece = controller.getCurrentPieces()[draggedPieceIndex];
            drawPiece(g2d, activePiece, dragX, dragY, false, slotColors[draggedPieceIndex]); // No glow while snapping back
        }

        // --- NEW: Header Score & Profile ---
        drawHeader(g2d);

        // --- NEW: Game Over Overlay ---
        if (controller.getGameState().isGameOver()) {
            drawGameOverOverlay(g2d);
        }
    }

    private void drawHeader(Graphics2D g2d) {
        // Simple Profile Mock
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.fillOval(20, 40, 50, 50);
        g2d.setColor(Color.WHITE);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(12f));
        g2d.drawString("NGƯỜI CHƠI", 80, 55);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 16f));
        g2d.drawString("NGƯỜI DÙNG", 80, 75);

        // Score
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(12f));
        g2d.setColor(NeonTheme.PURPLE);
        g2d.drawString("ĐIỂM SỐ", 280, 55);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 24f));
        g2d.setColor(Color.WHITE);
        String scoreStr = String.format("%,d", controller.getScoreManager().getScore());
        g2d.drawString(scoreStr, 280, 80);
    }

    private void drawGameOverOverlay(Graphics2D g2d) {
        // Dim background
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // --- NEW: TOP BANNER (Image 3 style) ---
        int bannerY = BOARD_Y + (Board.SIZE * CELL_SIZE) / 2 - 60;
        g2d.setColor(new Color(255, 115, 133, 200)); // Pinkish red glassmorphism
        g2d.fillRect(0, bannerY, getWidth(), 100);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(NeonTheme.LOGO_FONT.deriveFont(32f));
        FontMetrics fmBanner = g2d.getFontMetrics();
        String msg = "HẾT CHỖ TRỐNG!";
        g2d.drawString(msg, (getWidth() - fmBanner.stringWidth(msg)) / 2, bannerY + 45);
        
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(14f));
        fmBanner = g2d.getFontMetrics();
        g2d.drawString("GAME OVER", (getWidth() - fmBanner.stringWidth("GAME OVER")) / 2, bannerY + 75);

        // Modal Box (Image 1 style) - Shorter to not cover banner 
        int mw = 360, mh = 380;
        int mx = (getWidth() - mw) / 2;
        int my = bannerY + 110; // Position below banner
        
        if (my + mh > getHeight()) my = getHeight() - mh - 20;

        // Gradient modal bg
        GradientPaint modalGp = new GradientPaint(mx, my, new Color(40, 45, 110), mx, my + mh, new Color(15, 20, 70));
        g2d.setPaint(modalGp);
        g2d.fillRoundRect(mx, my, mw, mh, 30, 30);
        g2d.setColor(new Color(255, 255, 255, 50));
        g2d.drawRoundRect(mx, my, mw, mh, 30, 30);

        // "KẾT THÚC!" Title
        g2d.setColor(Color.WHITE);
        g2d.setFont(NeonTheme.LOGO_FONT.deriveFont(Font.ITALIC | Font.BOLD, 36f));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "KẾT THÚC!";
        g2d.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, my + 60);

        // Score Box
        int boxW = 300, boxH = 90;
        int boxX = (getWidth() - boxW) / 2;
        int boxY = my + 90;
        g2d.setColor(new Color(255, 255, 255, 10));
        g2d.fillRoundRect(boxX, boxY, boxW, boxH, 20, 20);
        g2d.setColor(NeonTheme.CYAN);
        g2d.drawRoundRect(boxX, boxY, boxW, boxH, 20, 20);
        
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(12f));
        fm = g2d.getFontMetrics();
        g2d.drawString("ĐIỂM ĐẠT ĐƯỢC", (getWidth() - fm.stringWidth("ĐIỂM ĐẠT ĐƯỢC")) / 2, boxY + 25);
        
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 42f));
        fm = g2d.getFontMetrics();
        String scoreVal = String.format("%,d", controller.getScoreManager().getScore());
        g2d.drawString(scoreVal, (getWidth() - fm.stringWidth(scoreVal)) / 2, boxY + 70);

        // Record Box
        int rBoxY = boxY + 105;
        g2d.setColor(new Color(255, 255, 255, 15));
        g2d.fillRoundRect(boxX, rBoxY, boxW, 50, 15, 15);
        g2d.setColor(NeonTheme.YELLOW);
        g2d.drawRoundRect(boxX, rBoxY, boxW, 50, 15, 15);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 18f));
        fm = g2d.getFontMetrics();
        g2d.drawString("★  KỶ LỤC MỚI!  ★", (getWidth() - fm.stringWidth("★  KỶ LỤC MỚI!  ★")) / 2, rBoxY + 32);

        // Buttons
        drawOverlayButton(g2d, "↻  CHƠI LẠI", (getWidth() - 300) / 2, rBoxY + 70, 300, 50, NeonTheme.YELLOW);
        drawOverlayButton(g2d, "⌂  TRANG CHỦ", (getWidth() - 300) / 2, rBoxY + 130, 300, 50, new Color(40, 60, 150));
    }

    private void drawOverlayButton(Graphics2D g2d, String text, int x, int y, int w, int h, Color color) {
        g2d.setColor(color);
        g2d.fillRoundRect(x, y, w, h, 20, 20);
        g2d.setColor(color.equals(NeonTheme.YELLOW) ? NeonTheme.BACKGROUND : Color.WHITE);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 18f));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + 35);
    }

    private void handleGameOverClicks(Point p) {
        int bannerY = BOARD_Y + (Board.SIZE * CELL_SIZE) / 2 - 60;
        int my = bannerY + 110;
        int rBoxY = my + 195;

        // Play Again
        Rectangle playAgainRect = new Rectangle((getWidth() - 300) / 2, rBoxY + 70, 300, 50);
        if (playAgainRect.contains(p)) {
            controller.restartGame();
            repaint();
        }

        // Home
        Rectangle homeRect = new Rectangle((getWidth() - 300) / 2, rBoxY + 130, 300, 50);
        if (homeRect.contains(p)) {
            controller.goToMenu();
            parent.showScreen("HOME");
        }
    }

    private void drawBoard(Graphics2D g2d) {
        // Outline border
        NeonTheme.drawNeonDashedBorder(g2d, BOARD_X, BOARD_Y, Board.SIZE * CELL_SIZE, Board.SIZE * CELL_SIZE, NeonTheme.CYAN);

        // Grid Background 
        Color cellBg = new Color(24, 28, 97, 100); 
        Color borderColor = new Color(64, 68, 116, 50);

        int[][] grid = controller.getBoard().getGrid();

        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                int px = BOARD_X + j * CELL_SIZE;
                int py = BOARD_Y + i * CELL_SIZE;
                
                if (grid[i][j] == 0) {
                    g2d.setColor(cellBg);
                    g2d.fillRoundRect(px, py, CELL_SIZE-2, CELL_SIZE-2, 4, 4);
                    g2d.setColor(borderColor);
                    g2d.drawRoundRect(px, py, CELL_SIZE-2, CELL_SIZE-2, 4, 4);
                } else {
                    // For now, drawing filled blocks with a generic color 
                    NeonTheme.draw3DBlock(g2d, px, py, CELL_SIZE-2, NeonTheme.CYAN, true);
                }
            }
        }
    }

    private void drawTrayPieces(Graphics2D g2d) {
        Block[] pieces = controller.getCurrentPieces();
        boolean[] used = controller.getIsUsed();

        for (int i = 0; i < 3; i++) {
            if (!used[i] && i != draggedPieceIndex) {
                 drawPiece(g2d, pieces[i], trayX[i], trayY[i], true, slotColors[i]);
            }
        }
    }

    private void drawPiece(Graphics2D g2d, Block block, int x, int y, boolean glowing, Color baseColor) {
        int[][] shape = block.getShape();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    NeonTheme.draw3DBlock(g2d, x + j * CELL_SIZE, y + i * CELL_SIZE, CELL_SIZE-2, baseColor, glowing);
                }
            }
        }
    }

    private void drawGhostBlock(Graphics2D g2d, Block block, int x, int y) {
        int[][] shape = block.getShape();
        g2d.setColor(new Color(255, 255, 255, 50)); // Semi-transparent white
        
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    g2d.fillRect(x + j * CELL_SIZE, y + i * CELL_SIZE, CELL_SIZE-2, CELL_SIZE-2);
                    g2d.setColor(new Color(255, 255, 255, 100));
                    g2d.drawRect(x + j * CELL_SIZE, y + i * CELL_SIZE, CELL_SIZE-2, CELL_SIZE-2);
                    g2d.setColor(new Color(255, 255, 255, 50));
                }
            }
        }
    }
}
