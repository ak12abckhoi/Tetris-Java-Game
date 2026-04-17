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
    private int CELL_SIZE = 24;
    private int BOARD_Y = 100;
    
    private void computeLayout() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Target Board Width: 85% of window width, but not exceeding 60% of window height
        int targetBoardW = (int)(w * 0.85);
        int targetBoardH_limit = (int)(h * 0.55);
        int targetSize = Math.min(targetBoardW, targetBoardH_limit);
        
        // Scale CELL_SIZE
        CELL_SIZE = Math.max(15, Math.min(45, targetSize / Board.SIZE));
        
        // Vertical positioning — more space from header
        BOARD_Y = Math.max(110, (int)(h * 0.16)); 
        
        updateTrayPositions();
    }
    
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

    // Board X is computed dynamically to center the board
    private int getBoardX() {
        return (getWidth() - Board.SIZE * CELL_SIZE) / 2;
    }

    public GamePanel(MainContainer parent) {
        this.parent = parent;
        setLayout(null);
        setBackground(NeonTheme.BACKGROUND);
        
        // Setup Controller
        controller = new ControllerGame();
        controller.startGame();

        // Tray positions will be recalculated dynamically in paintComponent
        trayX = new int[3];
        trayY = new int[3];

        setupMouseListeners();
    }

    private void updateTrayPositions() {
        int h = getHeight();
        int boardBottom = BOARD_Y + Board.SIZE * CELL_SIZE;
        int trayTop = boardBottom + (int)((h - boardBottom) * 0.15); 
        int w = getWidth();
        int spacing = w / 3;
        for (int i = 0; i < 3; i++) {
            trayX[i] = spacing * i + spacing / 2 - CELL_SIZE; 
            trayY[i] = trayTop;
        }
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                computeLayout();
                if (controller.getGameState().isGameOver()) return;
                
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
                    int gridX = Math.round((float)(dragX - getBoardX()) / CELL_SIZE);
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
        int bannerY = BOARD_Y + (Board.SIZE * CELL_SIZE) / 2 - 50;
        int mh = 300;
        int my = bannerY + 110;
        if ((my + mh) > getHeight()) {
            my = getHeight() - mh - 20;
        }
        
        int rBoxY = my + 155;
        Rectangle playAgainRect = new Rectangle((getWidth() - 260) / 2, rBoxY + 55, 260, 36);
        Rectangle homeRect = new Rectangle((getWidth() - 260) / 2, rBoxY + 145, 260, 36);
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
        computeLayout();
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
            int bx = getBoardX();
            int gridX = Math.round((float)(dragX - bx) / CELL_SIZE);
            int gridY = Math.round((float)(dragY - BOARD_Y) / CELL_SIZE);
            
            if (controller.getBoard().canPlaceBlock(activePiece, gridX, gridY)) {
                drawGhostBlock(g2d, activePiece, bx + gridX * CELL_SIZE, BOARD_Y + gridY * CELL_SIZE);
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
        g2d.fillOval(15, 20, 40, 40);
        g2d.setColor(Color.WHITE);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(10f));
        g2d.drawString("NGƯỜI CHƠI", 62, 35);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
        g2d.drawString("NGƯỜI DÙNG", 62, 52);

        // Score — right-aligned
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(10f));
        g2d.setColor(NeonTheme.PURPLE);
        int scoreAreaX = getWidth() - 120;
        g2d.drawString("ĐIỂM SỐ", scoreAreaX, 35);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 20f));
        g2d.setColor(Color.WHITE);
        String scoreStr = String.format("%,d", controller.getScoreManager().getScore());
        g2d.drawString(scoreStr, scoreAreaX, 58);

        // Thin separator line
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.fillRect(15, 75, getWidth() - 30, 1);
    }

    private void drawGameOverOverlay(Graphics2D g2d) {
        // Dim background
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int w = getWidth();
        int bannerY = BOARD_Y + (Board.SIZE * CELL_SIZE) / 2 - 50;

        // Top Banner
        g2d.setColor(new Color(255, 115, 133, 200));
        g2d.fillRect(0, bannerY, w, 80);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(NeonTheme.LOGO_FONT.deriveFont(24f));
        FontMetrics fmBanner = g2d.getFontMetrics();
        String msg = "HẾT CHỖ TRỐNG!";
        g2d.drawString(msg, (w - fmBanner.stringWidth(msg)) / 2, bannerY + 35);
        
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(12f));
        fmBanner = g2d.getFontMetrics();
        g2d.drawString("GAME OVER", (w - fmBanner.stringWidth("GAME OVER")) / 2, bannerY + 58);

        // Modal Box — compact for smaller window
        int mw = Math.min(320, w - 40);
        int mh = 300;
        int mx = (w - mw) / 2;
        int my = bannerY + 90;
        
        if (my + mh > getHeight()) my = getHeight() - mh - 10;

        GradientPaint modalGp = new GradientPaint(mx, my, new Color(40, 45, 110), mx, my + mh, new Color(15, 20, 70));
        g2d.setPaint(modalGp);
        g2d.fillRoundRect(mx, my, mw, mh, 24, 24);
        g2d.setColor(new Color(255, 255, 255, 50));
        g2d.drawRoundRect(mx, my, mw, mh, 24, 24);

        // Title
        g2d.setColor(Color.WHITE);
        g2d.setFont(NeonTheme.LOGO_FONT.deriveFont(Font.ITALIC | Font.BOLD, 28f));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "KẾT THÚC!";
        g2d.drawString(title, (w - fm.stringWidth(title)) / 2, my + 45);

        // Score Box
        int boxW = mw - 40, boxH = 70;
        int boxX = (w - boxW) / 2;
        int boxY = my + 60;
        g2d.setColor(new Color(255, 255, 255, 10));
        g2d.fillRoundRect(boxX, boxY, boxW, boxH, 16, 16);
        g2d.setColor(NeonTheme.CYAN);
        g2d.drawRoundRect(boxX, boxY, boxW, boxH, 16, 16);
        
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(10f));
        fm = g2d.getFontMetrics();
        g2d.drawString("ĐIỂM ĐẠT ĐƯỢC", (w - fm.stringWidth("ĐIỂM ĐẠT ĐƯỢC")) / 2, boxY + 20);
        
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 32f));
        fm = g2d.getFontMetrics();
        String scoreVal = String.format("%,d", controller.getScoreManager().getScore());
        g2d.drawString(scoreVal, (w - fm.stringWidth(scoreVal)) / 2, boxY + 55);

        // Record Box
        int rBoxY = boxY + 80;
        g2d.setColor(new Color(255, 255, 255, 15));
        g2d.fillRoundRect(boxX, rBoxY, boxW, 40, 12, 12);
        g2d.setColor(NeonTheme.YELLOW);
        g2d.drawRoundRect(boxX, rBoxY, boxW, 40, 12, 12);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
        fm = g2d.getFontMetrics();
        g2d.drawString("★  KỶ LỤC MỚI!  ★", (w - fm.stringWidth("★  KỶ LỤC MỚI!  ★")) / 2, rBoxY + 26);

        // Buttons
        int btnW = mw - 40;
        drawOverlayButton(g2d, "↻  CHƠI LẠI", (w - btnW) / 2, rBoxY + 55, btnW, 36, NeonTheme.YELLOW);
        drawOverlayButton(g2d, "★  XẾP HẠNG", (w - btnW) / 2, rBoxY + 100, btnW, 36, NeonTheme.CYAN);
        drawOverlayButton(g2d, "⌂  TRANG CHỦ", (w - btnW) / 2, rBoxY + 145, btnW, 36, new Color(40, 60, 150));
    }

    private void drawOverlayButton(Graphics2D g2d, String text, int x, int y, int w, int h, Color color) {
        g2d.setColor(color);
        g2d.fillRoundRect(x, y, w, h, 16, 16);
        g2d.setColor(color.equals(NeonTheme.YELLOW) ? NeonTheme.BACKGROUND : Color.WHITE);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + (h + fm.getAscent()) / 2 - 2);
    }

    private void handleGameOverClicks(Point p) {
        int w = getWidth();
        int bannerY = BOARD_Y + (Board.SIZE * CELL_SIZE) / 2 - 50;
        int mw = Math.min(320, w - 40);
        int mh = 300;
        int my = bannerY + 90;
        if ((my + mh) > getHeight()) {
            my = getHeight() - mh - 10;
        }
        
        int boxY = my + 60;
        int rBoxY = boxY + 80;
        int btnW = mw - 40;
        int btnX = (w - btnW) / 2;

        // Play Again
        Rectangle playAgainRect = new Rectangle(btnX, rBoxY + 55, btnW, 36);
        if (playAgainRect.contains(p)) {
            controller.restartGame();
            repaint();
        }

        // Rank
        Rectangle rankRect = new Rectangle(btnX, rBoxY + 100, btnW, 36);
        if (rankRect.contains(p)) {
            controller.goToMenu();
            parent.showScreen("RANK");
        }

        // Home
        Rectangle homeRect = new Rectangle(btnX, rBoxY + 145, btnW, 36);
        if (homeRect.contains(p)) {
            controller.goToMenu();
            parent.showScreen("HOME");
        }
    }

    private void drawBoard(Graphics2D g2d) {
        int BOARD_X = getBoardX();
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
