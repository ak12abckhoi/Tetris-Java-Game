package view;

import controller.ControllerGame;
import model.Block;
import model.Board;
import model.GameSettings;
import model.SoundManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GamePanel — Màn hình chơi đơn (Single Player).
 *
 * Hiển thị bảng 10×10, khay 3 khối kéo-thả, header điểm số và nút ⏸ góc trên phải.
 * Khi bấm tạm dừng, overlay hiện lên với 3 lựa chọn:
 *   - Tiếp tục
 *   - Thoát ra màn hình chính
 *   - Cài đặt (chỉnh âm thanh nội tuyến)
 *
 * NavBar của MainContainer được ẩn tự động khi màn này hiển thị.
 */
public class GamePanel extends JPanel {

    private final MainContainer  parent;
    private final ControllerGame controller;

    // ── Layout ───────────────────────────────────────────────────

    private int CELL_SIZE = 24;
    private int BOARD_Y   = 100;

    private void computeLayout() {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        BOARD_Y = Math.max(110, (int)(h * 0.16));
        int maxCellByHeight = (h - BOARD_Y - 20) / (Board.SIZE + 4);
        int maxCellByWidth  = (int)(w * 0.85) / Board.SIZE;
        CELL_SIZE = Math.max(12, Math.min(maxCellByHeight, maxCellByWidth));
        updateTrayPositions();
    }

    private int getBoardX() { return (getWidth() - Board.SIZE * CELL_SIZE) / 2; }

    // ── Màu khay ─────────────────────────────────────────────────

    private final Color[] slotColors = {NeonTheme.CYAN, NeonTheme.YELLOW, NeonTheme.PINK};

    // ── Kéo thả ──────────────────────────────────────────────────

    private int    draggedPieceIndex = -1;
    private int    dragX = -1, dragY = -1;
    private int    startDragX = -1, startDragY = -1;
    private int[]  trayX = new int[3];
    private int[]  trayY = new int[3];
    private Timer  snapBackTimer;
    private double currentSnapX, currentSnapY;

    // ── Hiệu ứng nhấp nháy ───────────────────────────────────────

    private final List<Point> flashingPoints    = new ArrayList<>();
    private Timer             flashTimer;
    private Timer             flashRepaintTimer;

    // ── Tạm dừng ─────────────────────────────────────────────────

    private boolean paused = false; // Trạng thái tạm dừng

    // Toạ độ và chiều cao bảng nút trong màn tạm dừng (tính lại mỗi lần vẽ)
    private int pauseMx, pauseMy, pauseMw, pauseMh; // Modal bounds
    private int pauseBtn1Y, pauseBtn2Y, pauseBtn3Y; // Y của 3 nút chính
    private int pauseBtnW, pauseBtnX;

    // Trạng thái cài đặt
    private boolean settingsExpanded = false; // Có đang hiện panel cài đặt nội tuyến không

    // Vùng nhấn cài đặt âm thanh — được ghi lại mỗi lần drawInlineSettings() chạy
    private final Rectangle settingsToggleRect = new Rectangle();
    private final Rectangle settingsSliderRect = new Rectangle();

    // ── Khởi tạo ─────────────────────────────────────────────────

    public GamePanel(MainContainer parent) {
        this.parent     = parent;
        this.controller = new ControllerGame();
        controller.startGame();

        setLayout(null);
        setBackground(NeonTheme.BACKGROUND);

        controller.setOnLinesCleared(pts -> {
            flashingPoints.addAll(pts);
            if (flashTimer       != null) flashTimer.stop();
            if (flashRepaintTimer!= null) flashRepaintTimer.stop();
            flashRepaintTimer = new Timer(30, ev -> repaint());
            flashRepaintTimer.start();
            flashTimer = new Timer(300, evt -> {
                flashingPoints.clear();
                if (flashRepaintTimer != null) flashRepaintTimer.stop();
                repaint();
            });
            flashTimer.setRepeats(false);
            flashTimer.start();
            repaint();
        });

        setupMouseListeners();

        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) {
                if (controller.getGameState().isGameOver() || controller.getGameState().isMenu()) {
                    controller.restartGame();
                }
                paused = false;
                settingsExpanded = false;
            }
        });
    }

    private void updateTrayPositions() {
        int boardBottom    = BOARD_Y + Board.SIZE * CELL_SIZE;
        int availableSpace = getHeight() - boardBottom;
        int trayTop        = boardBottom + availableSpace / 2;
        int spacing        = getWidth() / 3;
        for (int i = 0; i < 3; i++) {
            trayX[i] = spacing * i + spacing / 2;
            trayY[i] = trayTop;
        }
    }

    // ── Chuột ────────────────────────────────────────────────────

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Nút tạm dừng ⏸ (góc phải trên)
                if (getPauseButtonRect().contains(e.getPoint())) {
                    togglePause();
                    return;
                }

                if (paused) {
                    handlePauseOverlayClicks(e.getPoint());
                    return;
                }

                computeLayout();
                if (controller.getGameState().isGameOver()) return;

                Block[]   pieces = controller.getCurrentPieces();
                boolean[] used   = controller.getIsUsed();
                for (int i = 0; i < 3; i++) {
                    if (used[i]) continue;
                    Block p = pieces[i];
                    int bw  = p.getShape()[0].length * CELL_SIZE;
                    int bh  = p.getShape().length    * CELL_SIZE;
                    int px  = trayX[i] - bw / 2;
                    int py  = trayY[i] - bh / 2;
                    Rectangle bounds = new Rectangle(px, py, bw, bh);
                    bounds.grow(10, 10);
                    if (bounds.contains(e.getPoint())) {
                        draggedPieceIndex = i;
                        dragX = e.getX() - bw / 2;
                        dragY = e.getY() - bh / 2;
                        startDragX = px;
                        startDragY = py;
                        repaint();
                        break;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (paused) return;
                if (draggedPieceIndex != -1) {
                    Block p    = controller.getCurrentPieces()[draggedPieceIndex];
                    int gridX  = Math.round((float)(dragX - getBoardX()) / CELL_SIZE);
                    int gridY  = Math.round((float)(dragY - BOARD_Y) / CELL_SIZE);
                    if (controller.getBoard().canPlaceBlock(p, gridX, gridY)) {
                        controller.placePiece(draggedPieceIndex, gridX, gridY);
                        draggedPieceIndex = -1;
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
                if (paused) {
                    if (settingsExpanded && settingsSliderRect.contains(e.getPoint())) {
                        handleVolumeSliderClick(e.getPoint());
                    }
                    return;
                }
                if (draggedPieceIndex == -1) return;
                Block p = controller.getCurrentPieces()[draggedPieceIndex];
                dragX = e.getX() - p.getShape()[0].length * CELL_SIZE / 2;
                dragY = e.getY() - p.getShape().length    * CELL_SIZE / 2;
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (!paused && controller.getGameState().isGameOver()) {
                    setCursor(new Cursor(isOverGameOverButton(e.getPoint())
                            ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                } else if (getPauseButtonRect().contains(e.getPoint())) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }

    // ── Tạm dừng ─────────────────────────────────────────────────

    /** Hình chữ nhật của nút ⏸ góc phải trên. */
    private Rectangle getPauseButtonRect() {
        return new Rectangle(getWidth() - 50, 10, 40, 40);
    }

    /** Bật/tắt tạm dừng. */
    private void togglePause() {
        if (controller.getGameState().isGameOver()) return;
        paused = !paused;
        if (!paused) settingsExpanded = false;
        repaint();
    }

    /**
     * Xử lý click trong overlay tạm dừng.
     * Tính toạ độ dựa trên giá trị đã tính trong drawPauseOverlay().
     */
    private void handlePauseOverlayClicks(Point p) {
        int btnH = 44;

        // Nút Tiếp tục
        if (new Rectangle(pauseBtnX, pauseBtn1Y, pauseBtnW, btnH).contains(p)) {
            paused = false;
            settingsExpanded = false;
            repaint();
            return;
        }

        // Nút Thoát ra màn hình chính
        if (new Rectangle(pauseBtnX, pauseBtn2Y, pauseBtnW, btnH).contains(p)) {
            paused = false;
            settingsExpanded = false;
            controller.goToMenu();
            parent.showScreen("HOME");
            return;
        }

        // Nút Cài đặt — toggle panel âm thanh nội tuyến
        if (new Rectangle(pauseBtnX, pauseBtn3Y, pauseBtnW, btnH).contains(p)) {
            settingsExpanded = !settingsExpanded;
            repaint();
            return;
        }

        // Thanh âm lượng (khi panel cài đặt đang mở)
        if (settingsExpanded) {
            handleVolumeSliderClick(p);
        }
    }

    /**
     * Cập nhật âm thanh khi click/drag lên toggle hoặc slider.
     * Dùng settingsToggleRect và settingsSliderRect đã được ghi lại trong drawInlineSettings().
     */
    private void handleVolumeSliderClick(Point p) {
        // Toggle bật/tắt âm thanh
        if (settingsToggleRect.contains(p)) {
            GameSettings.getInstance().setSoundEnabled(!GameSettings.getInstance().isSoundEnabled());
            repaint();
            return;
        }
        // Thanh kéo âm lượng
        if (settingsSliderRect.contains(p)) {
            float ratio  = (float)(p.x - settingsSliderRect.x) / settingsSliderRect.width;
            int   newVol = Math.max(0, Math.min(100, (int)(ratio * 100)));
            GameSettings.getInstance().setVolume(newVol);
            repaint();
        }
    }

    // ── Vẽ ───────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        computeLayout();
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBoard(g2d);

        // Chỉ vẽ khay và kéo thả khi không tạm dừng và chưa kết thúc
        if (!paused && !controller.getGameState().isGameOver()) {
            drawTrayPieces(g2d);
            drawDragOverlay(g2d);
        } else if (!paused) {
            drawTrayPieces(g2d); // Hiện khay trong màn game over nhưng không cho kéo
        }

        drawHeader(g2d);
        drawPauseButton(g2d);

        if (paused) {
            drawPauseOverlay(g2d);
        } else if (controller.getGameState().isGameOver()) {
            drawGameOverOverlay(g2d);
        }
    }

    /** Vẽ nút ⏸ góc phải trên (hoặc ▶ khi đang tạm dừng). */
    private void drawPauseButton(Graphics2D g2d) {
        if (controller.getGameState().isGameOver()) return; // Không hiện khi game over

        Rectangle r = getPauseButtonRect();
        // Nền tròn mờ
        g2d.setColor(new Color(255, 255, 255, 25));
        g2d.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);

        // Biểu tượng ⏸ hoặc ▶
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2d.setColor(Color.WHITE);
        String icon = paused ? "▶" : "⏸";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(icon,
                r.x + (r.width  - fm.stringWidth(icon)) / 2,
                r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
    }

    /** Vẽ overlay tạm dừng: mờ nền + modal + 3 nút + panel cài đặt nội tuyến. */
    private void drawPauseOverlay(Graphics2D g2d) {
        int w = getWidth(), h = getHeight();

        // Lớp phủ mờ
        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRect(0, 0, w, h);

        // Kích thước modal — mở rộng khi panel cài đặt hiện
        int baseMh = settingsExpanded ? 340 : 250;
        pauseMw = Math.min(300, w - 40);
        pauseMh = baseMh;
        pauseMx = (w - pauseMw) / 2;
        pauseMy = (h - pauseMh) / 2;

        // Nền modal gradient
        g2d.setPaint(new GradientPaint(
                pauseMx, pauseMy, new Color(20, 28, 100),
                pauseMx, pauseMy + pauseMh, new Color(8, 14, 60)));
        g2d.fillRoundRect(pauseMx, pauseMy, pauseMw, pauseMh, 24, 24);
        g2d.setColor(new Color(255, 255, 255, 40));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(pauseMx, pauseMy, pauseMw, pauseMh, 24, 24);

        // Tiêu đề
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 20f));
        g2d.setColor(NeonTheme.CYAN);
        FontMetrics fm = g2d.getFontMetrics();
        String titleStr = "⏸  TẠM DỪNG";
        g2d.drawString(titleStr, (w - fm.stringWidth(titleStr)) / 2, pauseMy + 40);

        // Đường kẻ ngang
        g2d.setColor(new Color(255, 255, 255, 25));
        g2d.fillRect(pauseMx + 20, pauseMy + 52, pauseMw - 40, 1);

        // Tính toạ độ 3 nút chính
        int btnH  = 44;
        int gap   = 10;
        pauseBtnW = pauseMw - 40;
        pauseBtnX = (w - pauseBtnW) / 2;
        pauseBtn1Y = pauseMy + 68;
        pauseBtn2Y = pauseBtn1Y + btnH + gap;
        pauseBtn3Y = pauseBtn2Y + btnH + gap;

        drawPauseModalButton(g2d, "▶  Tiếp tục",              pauseBtnX, pauseBtn1Y, pauseBtnW, btnH, NeonTheme.CYAN);
        drawPauseModalButton(g2d, "⌂  Thoát ra màn hình chính",pauseBtnX, pauseBtn2Y, pauseBtnW, btnH, new Color(50, 70, 180));
        drawPauseModalButton(g2d, "⚙  Cài đặt âm thanh",      pauseBtnX, pauseBtn3Y, pauseBtnW, btnH,
                settingsExpanded ? NeonTheme.YELLOW : new Color(80, 80, 160));

        // Panel cài đặt âm thanh nội tuyến (mở rộng khi settingsExpanded = true)
        if (settingsExpanded) {
            drawInlineSettings(g2d, pauseBtnX, pauseBtn3Y + btnH + 10, pauseBtnW);
        }
    }

    /** Vẽ một nút trong modal tạm dừng. */
    private void drawPauseModalButton(Graphics2D g2d, String text, int x, int y, int w, int h, Color color) {
        // Nền với độ mờ
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
        g2d.fillRoundRect(x, y, w, h, 14, 14);
        // Viền màu
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, w, h, 14, 14);
        // Chữ
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, x + (w - fm.stringWidth(text)) / 2,
                y + (h + fm.getAscent() - fm.getDescent()) / 2);
    }

    /**
     * Vẽ panel cài đặt âm thanh nội tuyến bên trong modal tạm dừng.
     * Gồm: nút gạt tắt/bật âm thanh + thanh kéo âm lượng.
     */
    private void drawInlineSettings(Graphics2D g2d, int x, int y, int w) {
        // Nền nhẹ
        g2d.setColor(new Color(255, 255, 255, 8));
        g2d.fillRoundRect(x, y, w, 90, 12, 12);

        GameSettings settings = GameSettings.getInstance();
        boolean soundOn = settings.isSoundEnabled();
        int     volume  = settings.getVolume();

        int paddingX = x + 14;
        int innerW   = w - 28;

        // Dòng 1: "Âm thanh" + toggle
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 13f));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Âm thanh", paddingX, y + 20);

        // Toggle switch
        int toggleX  = x + w - 10 - 70;
        int toggleY  = y + 6;
        int toggleW  = 70;
        int toggleH  = 26;
        settingsToggleRect.setBounds(toggleX, toggleY, toggleW, toggleH);

        g2d.setColor(soundOn ? NeonTheme.CYAN : new Color(80, 80, 120));
        g2d.fillRoundRect(toggleX, toggleY, toggleW, toggleH, toggleH, toggleH);
        if (soundOn) {
            g2d.setColor(new Color(0, 240, 255, 60));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(toggleX, toggleY, toggleW, toggleH, toggleH, toggleH);
        }
        int thumbX = soundOn ? toggleX + toggleW - toggleH + 3 : toggleX + 3;
        g2d.setColor(Color.WHITE);
        g2d.fillOval(thumbX, toggleY + 3, toggleH - 6, toggleH - 6);

        // Dòng 2: "Âm lượng" + % + thanh kéo
        int row2Y = y + 50;
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 13f));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Âm lượng", paddingX, row2Y);

        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 12f));
        g2d.setColor(NeonTheme.CYAN);
        g2d.drawString(volume + "%", x + w - 40, row2Y);

        // Thanh kéo (không tương tác thực sự với JSlider — vẽ thủ công)
        int sliderX = paddingX + 70;
        int sliderW = innerW - 70 - 35;
        int sliderY = row2Y + 10;
        int sliderH = 20;
        settingsSliderRect.setBounds(sliderX, sliderY, sliderW, sliderH);

        // Nền track
        g2d.setColor(new Color(60, 65, 130));
        g2d.fillRoundRect(sliderX, sliderY + sliderH / 2 - 3, sliderW, 6, 6, 6);

        // Phần đã fill
        int filled = (int)(sliderW * volume / 100.0);
        g2d.setColor(soundOn ? NeonTheme.CYAN : new Color(80, 80, 120));
        g2d.fillRoundRect(sliderX, sliderY + sliderH / 2 - 3, filled, 6, 6, 6);

        // Thumb
        int thumbSliderX = sliderX + filled - 6;
        g2d.setColor(new Color(0, 240, 255, 60));
        g2d.fillOval(thumbSliderX - 2, sliderY + sliderH / 2 - 6, 14, 14);
        g2d.setColor(soundOn ? Color.WHITE : new Color(140, 140, 180));
        g2d.fillOval(thumbSliderX, sliderY + sliderH / 2 - 5, 11, 11);
    }

    // ── Vẽ bảng, khay, kéo thả ───────────────────────────────────

    private void drawHeader(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.fillOval(15, 20, 40, 40);
        g2d.setColor(Color.WHITE);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(10f));
        g2d.drawString("NGƯỜI CHƠI", 62, 35);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
        g2d.drawString("NGƯỜI DÙNG", 62, 52);

        // Điểm số — canh phải, để lại khoảng cho nút ⏸
        int scoreAreaX = getWidth() - 160;
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(10f));
        g2d.setColor(NeonTheme.PURPLE);
        g2d.drawString("ĐIỂM SỐ", scoreAreaX, 35);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 20f));
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.format("%,d", controller.getScoreManager().getScore()), scoreAreaX, 58);

        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.fillRect(15, 75, getWidth() - 30, 1);
    }

    private void drawBoard(Graphics2D g2d) {
        int BOARD_X  = getBoardX();
        Color cellBg = new Color(24, 28, 97, 100);
        Color border = new Color(64, 68, 116, 50);
        int[][] grid = controller.getBoard().getGrid();

        NeonTheme.drawNeonDashedBorder(g2d, BOARD_X, BOARD_Y,
                Board.SIZE * CELL_SIZE, Board.SIZE * CELL_SIZE, NeonTheme.CYAN);

        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                int px = BOARD_X + j * CELL_SIZE;
                int py = BOARD_Y + i * CELL_SIZE;
                if (grid[i][j] == 0) {
                    g2d.setColor(cellBg);
                    g2d.fillRoundRect(px, py, CELL_SIZE - 2, CELL_SIZE - 2, 4, 4);
                    g2d.setColor(border);
                    g2d.drawRoundRect(px, py, CELL_SIZE - 2, CELL_SIZE - 2, 4, 4);
                } else {
                    int cId = grid[i][j];
                    Color c = (cId >= 1 && cId <= 3) ? slotColors[cId - 1] : NeonTheme.CYAN;
                    NeonTheme.draw3DBlock(g2d, px, py, CELL_SIZE - 2, c, true);
                }
            }
        }

        if (!flashingPoints.isEmpty()) {
            boolean flashWhite = (System.currentTimeMillis() / 50) % 2 == 0;
            Color flashC = flashWhite ? Color.WHITE : NeonTheme.CYAN;
            for (Point p : flashingPoints) {
                NeonTheme.draw3DBlock(g2d, BOARD_X + p.x * CELL_SIZE, BOARD_Y + p.y * CELL_SIZE,
                        CELL_SIZE - 2, flashC, true);
            }
        }
    }

    private void drawTrayPieces(Graphics2D g2d) {
        Block[]   pieces = controller.getCurrentPieces();
        boolean[] used   = controller.getIsUsed();
        for (int i = 0; i < 3; i++) {
            if (used[i] || i == draggedPieceIndex) continue;
            int px = trayX[i] - (pieces[i].getShape()[0].length * CELL_SIZE) / 2;
            int py = trayY[i] - (pieces[i].getShape().length    * CELL_SIZE) / 2;
            drawPiece(g2d, pieces[i], px, py, true, slotColors[i]);
        }
    }

    private void drawDragOverlay(Graphics2D g2d) {
        if (draggedPieceIndex == -1) return;
        Block activePiece = controller.getCurrentPieces()[draggedPieceIndex];
        boolean isSnapping = snapBackTimer != null && snapBackTimer.isRunning();

        if (!isSnapping) {
            int pieceCX  = dragX + (activePiece.getShape()[0].length * CELL_SIZE) / 2;
            int pieceCY  = dragY + (activePiece.getShape().length    * CELL_SIZE) / 2;
            int trayCX   = startDragX + (activePiece.getShape()[0].length * CELL_SIZE) / 2;
            int trayCY   = startDragY + (activePiece.getShape().length    * CELL_SIZE) / 2;

            Stroke old = g2d.getStroke();
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{6}, 0));
            g2d.setColor(new Color(255, 140, 152, 100));
            g2d.drawLine(trayCX, trayCY, pieceCX, pieceCY);
            g2d.setStroke(old);

            int gridX = Math.round((float)(dragX - getBoardX()) / CELL_SIZE);
            int gridY = Math.round((float)(dragY - BOARD_Y) / CELL_SIZE);
            if (controller.getBoard().canPlaceBlock(activePiece, gridX, gridY)) {
                drawGhostBlock(g2d, activePiece, getBoardX() + gridX * CELL_SIZE, BOARD_Y + gridY * CELL_SIZE);
            }
            drawPiece(g2d, activePiece, dragX, dragY, true, slotColors[draggedPieceIndex]);
        } else {
            drawPiece(g2d, activePiece, dragX, dragY, false, slotColors[draggedPieceIndex]);
        }
    }

    private void drawPiece(Graphics2D g2d, Block block, int x, int y, boolean glowing, Color baseColor) {
        int[][] shape = block.getShape();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1)
                    NeonTheme.draw3DBlock(g2d, x + j * CELL_SIZE, y + i * CELL_SIZE,
                            CELL_SIZE - 2, baseColor, glowing);
            }
        }
    }

    private void drawGhostBlock(Graphics2D g2d, Block block, int x, int y) {
        int[][] shape = block.getShape();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    g2d.setColor(new Color(255, 255, 255, 50));
                    g2d.fillRect(x + j * CELL_SIZE, y + i * CELL_SIZE, CELL_SIZE - 2, CELL_SIZE - 2);
                    g2d.setColor(new Color(255, 255, 255, 100));
                    g2d.drawRect(x + j * CELL_SIZE, y + i * CELL_SIZE, CELL_SIZE - 2, CELL_SIZE - 2);
                }
            }
        }
    }

    // ── Snap-back ────────────────────────────────────────────────

    private void startSnapBackAnimation() {
        currentSnapX = dragX;
        currentSnapY = dragY;
        final int targetX = startDragX, targetY = startDragY;
        snapBackTimer = new Timer(15, e -> {
            double dx = targetX - currentSnapX, dy = targetY - currentSnapY;
            if (Math.abs(dx) < 2 && Math.abs(dy) < 2) {
                draggedPieceIndex = -1;
                ((Timer) e.getSource()).stop();
            } else {
                currentSnapX += dx * 0.2;
                currentSnapY += dy * 0.2;
                dragX = (int) currentSnapX;
                dragY = (int) currentSnapY;
            }
            repaint();
        });
        snapBackTimer.start();
    }

    // ── Màn Game Over ────────────────────────────────────────────

    private boolean isOverGameOverButton(Point p) {
        int[] gl = getGameOverLayout();
        int rBoxY = gl[0], btnW = gl[1], btnX = gl[2];
        return new Rectangle(btnX, rBoxY + 55, btnW, 36).contains(p)
            || new Rectangle(btnX, rBoxY + 100, btnW, 36).contains(p)
            || new Rectangle(btnX, rBoxY + 145, btnW, 36).contains(p);
    }

    private int[] getGameOverLayout() {
        int w  = getWidth();
        int mw = Math.min(320, w - 40);
        int mh = 350;
        int boardCenterY = BOARD_Y + (Board.SIZE * CELL_SIZE) / 2;
        int my = boardCenterY - mh / 2;
        if (my + mh > getHeight() - 10) my = getHeight() - mh - 10;
        int boxY  = my + 60;
        int rBoxY = boxY + 80;
        int btnW  = mw - 40;
        int btnX  = (w - btnW) / 2;
        return new int[]{rBoxY, btnW, btnX};
    }

    private void handleGameOverClicks(Point p) {
        int[] gl  = getGameOverLayout();
        int rBoxY = gl[0], btnW = gl[1], btnX = gl[2];
        if (new Rectangle(btnX, rBoxY + 55,  btnW, 36).contains(p)) { controller.restartGame(); repaint(); }
        if (new Rectangle(btnX, rBoxY + 100, btnW, 36).contains(p)) { controller.goToMenu(); parent.showScreen("RANK"); }
        if (new Rectangle(btnX, rBoxY + 145, btnW, 36).contains(p)) { controller.goToMenu(); parent.showScreen("HOME"); }
    }

    private void drawGameOverOverlay(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int w = getWidth();
        int mw = Math.min(320, w - 40);
        int mh = 350;
        int mx = (w - mw) / 2;
        int boardCenterY = BOARD_Y + (Board.SIZE * CELL_SIZE) / 2;
        int my = boardCenterY - mh / 2;
        if (my + mh > getHeight() - 10) my = getHeight() - mh - 10;
        int bannerY = my - 100;

        g2d.setColor(new Color(255, 115, 133, 200));
        g2d.fillRect(0, bannerY, w, 80);
        g2d.setColor(Color.WHITE);
        g2d.setFont(NeonTheme.LOGO_FONT.deriveFont(24f));
        FontMetrics fm = g2d.getFontMetrics();
        String msg = "HẾT CHỖ TRỐNG!";
        g2d.drawString(msg, (w - fm.stringWidth(msg)) / 2, bannerY + 35);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(12f));
        fm = g2d.getFontMetrics();
        g2d.drawString("GAME OVER", (w - fm.stringWidth("GAME OVER")) / 2, bannerY + 58);

        g2d.setPaint(new GradientPaint(mx, my, new Color(40, 45, 110), mx, my + mh, new Color(15, 20, 70)));
        g2d.fillRoundRect(mx, my, mw, mh, 24, 24);
        g2d.setColor(new Color(255, 255, 255, 50));
        g2d.drawRoundRect(mx, my, mw, mh, 24, 24);

        g2d.setColor(Color.WHITE);
        g2d.setFont(NeonTheme.LOGO_FONT.deriveFont(Font.ITALIC | Font.BOLD, 28f));
        fm = g2d.getFontMetrics();
        String title = "KẾT THÚC!";
        g2d.drawString(title, (w - fm.stringWidth(title)) / 2, my + 45);

        int boxW = mw - 40, boxH = 70, boxX = (w - boxW) / 2, boxY = my + 60;
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

        int rBoxY = boxY + 80;
        if (controller.getScoreManager().isNewRecord()) {
            g2d.setColor(new Color(255, 255, 255, 15));
            g2d.fillRoundRect(boxX, rBoxY, boxW, 40, 12, 12);
            g2d.setColor(NeonTheme.YELLOW);
            g2d.drawRoundRect(boxX, rBoxY, boxW, 40, 12, 12);
            g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
            fm = g2d.getFontMetrics();
            String newRec = "★  KỶ LỤC MỚI!  ★";
            g2d.drawString(newRec, (w - fm.stringWidth(newRec)) / 2, rBoxY + 26);
        } else {
            g2d.setColor(new Color(255, 255, 255, 10));
            g2d.fillRoundRect(boxX, rBoxY, boxW, 40, 12, 12);
            g2d.setColor(NeonTheme.PURPLE);
            g2d.drawRoundRect(boxX, rBoxY, boxW, 40, 12, 12);
            g2d.setColor(Color.WHITE);
            g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.PLAIN, 14f));
            fm = g2d.getFontMetrics();
            String rec = "KỶ LỤC: " + String.format("%,d", controller.getScoreManager().getHighScore());
            g2d.drawString(rec, (w - fm.stringWidth(rec)) / 2, rBoxY + 26);
        }

        int btnW = mw - 40, btnX = (w - btnW) / 2;
        drawOverlayButton(g2d, "↻  CHƠI LẠI",  btnX, rBoxY + 55,  btnW, 36, NeonTheme.YELLOW);
        drawOverlayButton(g2d, "★  XẾP HẠNG",  btnX, rBoxY + 100, btnW, 36, NeonTheme.CYAN);
        drawOverlayButton(g2d, "⌂  TRANG CHỦ", btnX, rBoxY + 145, btnW, 36, new Color(40, 60, 150));
    }

    private void drawOverlayButton(Graphics2D g2d, String text, int x, int y, int w, int h, Color color) {
        g2d.setColor(color);
        g2d.fillRoundRect(x, y, w, h, 16, 16);
        g2d.setColor(color.equals(NeonTheme.YELLOW) ? NeonTheme.BACKGROUND : Color.WHITE);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + (h + fm.getAscent()) / 2 - 2);
    }
}
