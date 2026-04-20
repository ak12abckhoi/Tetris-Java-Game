package view;

import controller.BattleController;
import model.Block;
import model.Board;
import model.GameSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BattlePanel — Màn hình chế độ Đấu Trí Luân Phiên (Turn-based Shared Board).
 *
 * Hiển thị bảng dùng chung, khay 3 khối, điểm hai bên và nút ⏸ góc phải trên.
 * Khi tạm dừng, overlay hiện lên với 3 lựa chọn:
 * - Tiếp tục
 * - Thoát ra màn hình chính
 * - Cài đặt âm thanh (nội tuyến có thể mở rộng)
 *
 * Trong thời gian tạm dừng, AI không thực hiện nước đi tiếp theo.
 */
public class BattlePanel extends JPanel {

    private static final Color[] SLOT_COLORS = { NeonTheme.CYAN, NeonTheme.YELLOW, NeonTheme.PINK };

    private final MainContainer parent;
    private BattleController battleController;

    // ── Layout ───────────────────────────────────────────────────

    private int CELL_SIZE = 22;
    private int BOARD_Y = 140;
    private int BOARD_X = 15;

    // ── Kéo thả ──────────────────────────────────────────────────

    private int draggedPieceIndex = -1;
    private int dragX = -1, dragY = -1;
    private int startDragX, startDragY;
    private int[] trayX = new int[3];
    private int[] trayY = new int[3];
    private Timer snapBackTimer;
    private double snapCurX, snapCurY;

    // ── UI & Hiệu ứng ─────────────────────────────────────────────

    private Timer uiTimer;
    private final List<Point> flashingPoints = new ArrayList<>();
    private Timer flashTimer;

    // ── Tạm dừng ─────────────────────────────────────────────────

    private boolean paused = false; // Trạng thái tạm dừng
    private boolean settingsExpanded = false; // Panel cài đặt âm thanh có đang mở không

    // Toạ độ các nút trong overlay tạm dừng (tính trong drawPauseOverlay)
    private int pauseMx, pauseMy, pauseMw, pauseMh;
    private int pauseBtn1Y, pauseBtn2Y, pauseBtn3Y;
    private int pauseBtnW, pauseBtnX;

    // Vùng nhấn cài đặt âm thanh — được ghi lại mỗi lần drawInlineSettings() chạy
    private final Rectangle settingsToggleRect = new Rectangle();
    private final Rectangle settingsSliderRect = new Rectangle();

    // ── Khởi tạo ─────────────────────────────────────────────────

    public BattlePanel(MainContainer parent) {
        this.parent = parent;
        setLayout(null);
        setBackground(NeonTheme.BACKGROUND);

        battleController = new BattleController();
        setupCallbacks();
        setupMouseListeners();
        setupUITimer();
    }

    /** Bắt đầu ván đấu mới: tạo lại Controller và reset trạng thái. */
    public void startBattle() {
        battleController = new BattleController();
        paused = false;
        settingsExpanded = false;
        setupCallbacks();
        computeLayout();
        battleController.startBattleLocal();
        repaint();
    }

    /** Đăng ký callback từ BattleController. */
    private void setupCallbacks() {
        battleController.setOnTick(this::repaint);
        battleController.setOnBattleEnd(this::repaint);
        battleController.setOnTurnChange(() -> {
            // Không đặt lại draggedPieceIndex khi đang tạm dừng để tránh mất trạng thái
            if (!paused)
                draggedPieceIndex = -1;
            repaint();
        });
        battleController.setOnLinesCleared(pts -> {
            flashingPoints.addAll(pts);
            if (flashTimer != null)
                flashTimer.stop();
            flashTimer = new Timer(300, evt -> {
                flashingPoints.clear();
                repaint();
            });
            flashTimer.setRepeats(false);
            flashTimer.start();
        });
    }

    /** Bộ đếm vẽ lại 30fps. */
    private void setupUITimer() {
        uiTimer = new Timer(30, e -> repaint());
        uiTimer.setRepeats(true);
        uiTimer.start();
    }

    // ── Layout ───────────────────────────────────────────────────

    private void computeLayout() {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0)
            return;
        BOARD_Y = 140;
        int maxCellByHeight = (h - BOARD_Y - 20) / (Board.SIZE + 4);
        int maxCellByWidth = (int) (w * 0.85) / Board.SIZE;
        CELL_SIZE = Math.max(12, Math.min(maxCellByHeight, maxCellByWidth));
        BOARD_X = (w - Board.SIZE * CELL_SIZE) / 2;
        updateTrayPositions();
    }

    private void updateTrayPositions() {
        int boardBottom = BOARD_Y + Board.SIZE * CELL_SIZE;
        int availableSpace = getHeight() - boardBottom;
        int trayTop = boardBottom + availableSpace / 2;
        int spacing = getWidth() / 3;
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
                // Nút ⏸ góc phải trên
                if (getPauseButtonRect().contains(e.getPoint())) {
                    togglePause();
                    return;
                }

                // Khi đang tạm dừng → xử lý click overlay
                if (paused) {
                    handlePauseOverlayClicks(e.getPoint());
                    return;
                }

                // Nếu đã có kết quả → xử lý nút kết quả
                if (battleController.getResult() != BattleController.Result.NONE) {
                    handleResultClicks(e.getPoint());
                    return;
                }

                computeLayout();
                if (!battleController.isPlayerTurn())
                    return;

                Block[] pieces = battleController.getSharedPieces();
                boolean[] used = battleController.getSharedUsed();
                for (int i = 0; i < 3; i++) {
                    if (used[i])
                        continue;
                    Block p = pieces[i];
                    int bw = p.getShape()[0].length * CELL_SIZE;
                    int bh = p.getShape().length * CELL_SIZE;
                    int px = trayX[i] - bw / 2;
                    int py = trayY[i] - bh / 2;
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
                // Nút quay lại (góc trên trái)
                if (new Rectangle(0, 15, 60, 40).contains(e.getPoint()) && !paused) {
                    battleController.stopBattle();
                    parent.showScreen("HOME");
                    return;
                }

                if (paused || battleController.getResult() != BattleController.Result.NONE)
                    return;

                if (draggedPieceIndex != -1) {
                    Block p = battleController.getSharedPieces()[draggedPieceIndex];
                    int gridX = Math.round((float) (dragX - BOARD_X) / CELL_SIZE);
                    int gridY = Math.round((float) (dragY - BOARD_Y) / CELL_SIZE);
                    if (battleController.placePiecePlayer(draggedPieceIndex, gridX, gridY)) {
                        draggedPieceIndex = -1;
                    } else {
                        startSnapBack();
                    }
                    repaint();
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
                if (draggedPieceIndex == -1 || !battleController.isPlayerTurn())
                    return;
                Block p = battleController.getSharedPieces()[draggedPieceIndex];
                dragX = e.getX() - p.getShape()[0].length * CELL_SIZE / 2;
                dragY = e.getY() - p.getShape().length * CELL_SIZE / 2;
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                setCursor(new Cursor(getPauseButtonRect().contains(e.getPoint())
                        ? Cursor.HAND_CURSOR
                        : Cursor.DEFAULT_CURSOR));
            }
        });
    }

    // ── Tạm dừng ─────────────────────────────────────────────────

    private Rectangle getPauseButtonRect() {
        return new Rectangle(getWidth() - 50, 10, 40, 40);
    }

    /** Bật/tắt tạm dừng. Khi tạm dừng, AI sẽ không nhận lệnh tính toán tiếp. */
    private void togglePause() {
        if (battleController.getResult() != BattleController.Result.NONE)
            return;
        paused = !paused;
        if (!paused)
            settingsExpanded = false;
        repaint();
    }

    /** Xử lý click trong overlay tạm dừng. */
    private void handlePauseOverlayClicks(Point p) {
        int btnH = 44;

        // Tiếp tục
        if (new Rectangle(pauseBtnX, pauseBtn1Y, pauseBtnW, btnH).contains(p)) {
            paused = false;
            settingsExpanded = false;
            repaint();
            return;
        }

        // Thoát ra màn hình chính
        if (new Rectangle(pauseBtnX, pauseBtn2Y, pauseBtnW, btnH).contains(p)) {
            paused = false;
            settingsExpanded = false;
            battleController.stopBattle();
            parent.showScreen("HOME");
            return;
        }

        // Cài đặt âm thanh — toggle
        if (new Rectangle(pauseBtnX, pauseBtn3Y, pauseBtnW, btnH).contains(p)) {
            settingsExpanded = !settingsExpanded;
            repaint();
            return;
        }

        // Thanh tương tác cài đặt
        if (settingsExpanded) {
            handleVolumeSliderClick(p);
        }
    }

    private void handleVolumeSliderClick(Point p) {
        // Toggle âm thanh
        if (settingsToggleRect.contains(p)) {
            boolean current = GameSettings.getInstance().isSoundEnabled();
            GameSettings.getInstance().setSoundEnabled(!current);
            repaint();
            return;
        }
        // Slider âm lượng
        if (settingsSliderRect.contains(p)) {
            float ratio = (float) (p.x - settingsSliderRect.x) / settingsSliderRect.width;
            int newVol = Math.max(0, Math.min(100, (int) (ratio * 100)));
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
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawHeaderAndScore(g2d);
        drawBoard(g2d);
        drawTrayPieces(g2d);

        if (!paused)
            drawDragLayer(g2d);

        drawPauseButton(g2d);

        if (paused) {
            drawPauseOverlay(g2d);
        } else if (battleController.getResult() != BattleController.Result.NONE) {
            drawResultOverlay(g2d);
        }
    }

    /** Vẽ nút ⏸ hoặc ▶ góc phải trên. */
    private void drawPauseButton(Graphics2D g2d) {
        if (battleController.getResult() != BattleController.Result.NONE)
            return;
        Rectangle r = getPauseButtonRect();
        g2d.setColor(new Color(255, 255, 255, 25));
        g2d.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2d.setColor(Color.WHITE);
        String icon = paused ? "▶" : "⏸";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(icon,
                r.x + (r.width - fm.stringWidth(icon)) / 2,
                r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
    }

    /** Vẽ overlay tạm dừng. */
    private void drawPauseOverlay(Graphics2D g2d) {
        int w = getWidth(), h = getHeight();

        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRect(0, 0, w, h);

        int baseMh = settingsExpanded ? 340 : 250;
        pauseMw = Math.min(300, w - 40);
        pauseMh = baseMh;
        pauseMx = (w - pauseMw) / 2;
        pauseMy = (h - pauseMh) / 2;

        g2d.setPaint(new GradientPaint(
                pauseMx, pauseMy, new Color(20, 28, 100),
                pauseMx, pauseMy + pauseMh, new Color(8, 14, 60)));
        g2d.fillRoundRect(pauseMx, pauseMy, pauseMw, pauseMh, 24, 24);
        g2d.setColor(new Color(255, 255, 255, 40));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(pauseMx, pauseMy, pauseMw, pauseMh, 24, 24);

        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 20f));
        g2d.setColor(NeonTheme.CYAN);
        FontMetrics fm = g2d.getFontMetrics();
        String titleStr = "⏸  TẠM DỪNG";
        g2d.drawString(titleStr, (w - fm.stringWidth(titleStr)) / 2, pauseMy + 40);

        g2d.setColor(new Color(255, 255, 255, 25));
        g2d.fillRect(pauseMx + 20, pauseMy + 52, pauseMw - 40, 1);

        int btnH = 44, gap = 10;
        pauseBtnW = pauseMw - 40;
        pauseBtnX = (w - pauseBtnW) / 2;
        pauseBtn1Y = pauseMy + 68;
        pauseBtn2Y = pauseBtn1Y + btnH + gap;
        pauseBtn3Y = pauseBtn2Y + btnH + gap;

        drawPauseModalButton(g2d, "▶  Tiếp tục", pauseBtnX, pauseBtn1Y, pauseBtnW, btnH, NeonTheme.CYAN);
        drawPauseModalButton(g2d, "⌂  Thoát ra màn hình chính", pauseBtnX, pauseBtn2Y, pauseBtnW, btnH,
                new Color(50, 70, 180));
        drawPauseModalButton(g2d, "⚙  Cài đặt âm thanh", pauseBtnX, pauseBtn3Y, pauseBtnW, btnH,
                settingsExpanded ? NeonTheme.YELLOW : new Color(80, 80, 160));

        if (settingsExpanded) {
            drawInlineSettings(g2d, pauseBtnX, pauseBtn3Y + btnH + 10, pauseBtnW);
        }
    }

    private void drawPauseModalButton(Graphics2D g2d, String text, int x, int y, int w, int h, Color color) {
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
        g2d.fillRoundRect(x, y, w, h, 14, 14);
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, w, h, 14, 14);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, x + (w - fm.stringWidth(text)) / 2,
                y + (h + fm.getAscent() - fm.getDescent()) / 2);
    }

    /** Vẽ panel cài đặt âm thanh nội tuyến (toggle + slider). */
    private void drawInlineSettings(Graphics2D g2d, int x, int y, int w) {
        g2d.setColor(new Color(255, 255, 255, 8));
        g2d.fillRoundRect(x, y, w, 90, 12, 12);

        GameSettings settings = GameSettings.getInstance();
        boolean soundOn = settings.isSoundEnabled();
        int volume = settings.getVolume();
        int paddingX = x + 14;

        // Dòng 1: Âm thanh + Toggle
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 13f));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Âm thanh", paddingX, y + 20);

        int tgX = x + w - 10 - 70, tgY = y + 6;
        settingsToggleRect.setBounds(tgX, tgY, 70, 26);

        g2d.setColor(soundOn ? NeonTheme.CYAN : new Color(80, 80, 120));
        g2d.fillRoundRect(tgX, tgY, 70, 26, 26, 26);
        if (soundOn) {
            g2d.setColor(new Color(0, 240, 255, 60));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(tgX, tgY, 70, 26, 26, 26);
        }
        int thumbX = soundOn ? tgX + 70 - 26 + 3 : tgX + 3;
        g2d.setColor(Color.WHITE);
        g2d.fillOval(thumbX, tgY + 3, 20, 20);

        // Dòng 2: Âm lượng + % + Slider
        int row2Y = y + 50;
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 13f));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Âm lượng", paddingX, row2Y);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 12f));
        g2d.setColor(NeonTheme.CYAN);
        g2d.drawString(volume + "%", x + w - 40, row2Y);

        int sliderX = paddingX + 70;
        int sliderW = w - 70 - 14 - 35;
        int sliderCY = row2Y + 12;
        settingsSliderRect.setBounds(sliderX, sliderCY - 10, sliderW, 20);

        g2d.setColor(new Color(60, 65, 130));
        g2d.fillRoundRect(sliderX, sliderCY - 3, sliderW, 6, 6, 6);

        int filled = (int) (sliderW * volume / 100.0);
        g2d.setColor(soundOn ? NeonTheme.CYAN : new Color(80, 80, 120));
        g2d.fillRoundRect(sliderX, sliderCY - 3, filled, 6, 6, 6);

        int tSX = sliderX + filled - 6;
        g2d.setColor(new Color(0, 240, 255, 60));
        g2d.fillOval(tSX - 2, sliderCY - 6, 14, 14);
        g2d.setColor(soundOn ? Color.WHITE : new Color(140, 140, 180));
        g2d.fillOval(tSX, sliderCY - 5, 11, 11);
    }

    // ── Các phương thức vẽ game ───────────────────────────────────

    private void drawHeaderAndScore(Graphics2D g2d) {
        int w = getWidth();
        g2d.setColor(new Color(13, 17, 76, 200));
        g2d.fillRect(0, 0, w, 60);

        // Nút quay lại
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 20f));
        g2d.setColor(Color.WHITE);
        g2d.drawString("←", 12, 38);

        // Thông báo lượt
        String turnStr = battleController.isPlayerTurn() ? "TỚI LƯỢT BẠN!" : "AI ĐANG NGHĨ...";
        Color turnColor = battleController.isPlayerTurn() ? NeonTheme.CYAN : NeonTheme.PINK;
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 18f));
        FontMetrics fm = g2d.getFontMetrics();
        // Lùi sang trái một chút để không đè lên nút ⏸
        g2d.setColor(turnColor);
        g2d.drawString(turnStr, (w - 50 - fm.stringWidth(turnStr)) / 2 + 12, 35);

        // Hai hộp điểm
        int scoreY = 70;
        int scoreBoxW = (w - 60) / 2;
        int scoreH = 50;

        // Người chơi
        g2d.setColor(new Color(NeonTheme.CYAN.getRed(), NeonTheme.CYAN.getGreen(), NeonTheme.CYAN.getBlue(), 30));
        g2d.fillRoundRect(20, scoreY, scoreBoxW, scoreH, 12, 12);
        g2d.setColor(NeonTheme.CYAN);
        g2d.drawRoundRect(20, scoreY, scoreBoxW, scoreH, 12, 12);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(10f));
        fm = g2d.getFontMetrics();
        g2d.drawString("ĐIỂM CỦA BẠN",
                20 + (scoreBoxW - fm.stringWidth("ĐIỂM CỦA BẠN")) / 2, scoreY + 14);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 20f));
        String pScore = String.valueOf(battleController.getPlayerScore());
        fm = g2d.getFontMetrics();
        g2d.drawString(pScore, 20 + (scoreBoxW - fm.stringWidth(pScore)) / 2, scoreY + 38);

        // AI
        int aiBoxX = w - 20 - scoreBoxW;
        g2d.setColor(new Color(NeonTheme.PINK.getRed(), NeonTheme.PINK.getGreen(), NeonTheme.PINK.getBlue(), 30));
        g2d.fillRoundRect(aiBoxX, scoreY, scoreBoxW, scoreH, 12, 12);
        g2d.setColor(NeonTheme.PINK);
        g2d.drawRoundRect(aiBoxX, scoreY, scoreBoxW, scoreH, 12, 12);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(10f));
        fm = g2d.getFontMetrics();
        g2d.drawString("ĐIỂM CỦA AI",
                aiBoxX + (scoreBoxW - fm.stringWidth("ĐIỂM CỦA AI")) / 2, scoreY + 14);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 20f));
        String aScore = String.valueOf(battleController.getAiScore());
        fm = g2d.getFontMetrics();
        g2d.drawString(aScore, aiBoxX + (scoreBoxW - fm.stringWidth(aScore)) / 2, scoreY + 38);
    }

    private void drawBoard(Graphics2D g2d) {
        int[][] grid = battleController.getBoard().getGrid();
        Color cellBg = new Color(24, 28, 97, 100);
        Color borderColor = new Color(64, 68, 116, 50);
        Color borderNeon = battleController.isPlayerTurn() ? NeonTheme.CYAN : NeonTheme.PINK;

        NeonTheme.drawNeonDashedBorder(g2d, BOARD_X, BOARD_Y,
                Board.SIZE * CELL_SIZE, Board.SIZE * CELL_SIZE, borderNeon);

        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                int px = BOARD_X + j * CELL_SIZE;
                int py = BOARD_Y + i * CELL_SIZE;
                if (grid[i][j] == 0) {
                    g2d.setColor(cellBg);
                    g2d.fillRoundRect(px, py, CELL_SIZE - 2, CELL_SIZE - 2, 4, 4);
                    g2d.setColor(borderColor);
                    g2d.drawRoundRect(px, py, CELL_SIZE - 2, CELL_SIZE - 2, 4, 4);
                } else {
                    int cId = grid[i][j];
                    Color c = (cId >= 1 && cId <= 3) ? SLOT_COLORS[cId - 1] : NeonTheme.CYAN;
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
        Block[] pieces = battleController.getSharedPieces();
        boolean[] used = battleController.getSharedUsed();
        if (pieces == null || used == null)
            return;
        for (int i = 0; i < 3; i++) {
            if (used[i] || i == draggedPieceIndex)
                continue;
            int px = trayX[i] - (pieces[i].getShape()[0].length * CELL_SIZE) / 2;
            int py = trayY[i] - (pieces[i].getShape().length * CELL_SIZE) / 2;
            drawPiece(g2d, pieces[i], px, py, true, SLOT_COLORS[i]);
        }
    }

    private void drawDragLayer(Graphics2D g2d) {
        if (draggedPieceIndex == -1)
            return;
        Block p = battleController.getSharedPieces()[draggedPieceIndex];
        boolean snapping = snapBackTimer != null && snapBackTimer.isRunning();

        if (!snapping) {
            int pieceCX = dragX + (p.getShape()[0].length * CELL_SIZE) / 2;
            int pieceCY = dragY + (p.getShape().length * CELL_SIZE) / 2;
            int trayCX = startDragX + (p.getShape()[0].length * CELL_SIZE) / 2;
            int trayCY = startDragY + (p.getShape().length * CELL_SIZE) / 2;

            Stroke old = g2d.getStroke();
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[] { 6 }, 0));
            g2d.setColor(new Color(255, 140, 152, 100));
            g2d.drawLine(trayCX, trayCY, pieceCX, pieceCY);
            g2d.setStroke(old);

            int gridX = Math.round((float) (dragX - BOARD_X) / CELL_SIZE);
            int gridY = Math.round((float) (dragY - BOARD_Y) / CELL_SIZE);
            if (battleController.getBoard().canPlaceBlock(p, gridX, gridY)) {
                drawGhost(g2d, p, BOARD_X + gridX * CELL_SIZE, BOARD_Y + gridY * CELL_SIZE);
            }
            drawPiece(g2d, p, dragX, dragY, true, SLOT_COLORS[draggedPieceIndex]);
        } else {
            drawPiece(g2d, p, dragX, dragY, false, SLOT_COLORS[draggedPieceIndex]);
        }
    }

    private void drawResultOverlay(Graphics2D g2d) {
        int w = getWidth(), h = getHeight();
        g2d.setColor(new Color(0, 0, 0, 190));
        g2d.fillRect(0, 0, w, h);

        BattleController.Result result = battleController.getResult();
        int pScore = battleController.getPlayerScore();
        int aScore = battleController.getAiScore();

        int mw = Math.min(300, w - 40);
        int mh = 270;
        int mx = (w - mw) / 2;
        int my = (h - mh) / 2;

        g2d.setPaint(new GradientPaint(mx, my, new Color(30, 35, 100), mx, my + mh, new Color(10, 15, 60)));
        g2d.fillRoundRect(mx, my, mw, mh, 24, 24);
        g2d.setColor(new Color(255, 255, 255, 50));
        g2d.drawRoundRect(mx, my, mw, mh, 24, 24);

        String titleStr;
        Color titleColor;
        switch (result) {
            case PLAYER_WIN:
                titleStr = "BẠN THẮNG! 🎉";
                titleColor = NeonTheme.CYAN;
                break;
            case AI_WIN:
                titleStr = "AI THẮNG!";
                titleColor = NeonTheme.PINK;
                break;
            default:
                titleStr = "HÒA!";
                titleColor = NeonTheme.YELLOW;
                break;
        }
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 22f));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(titleColor);
        g2d.drawString(titleStr, (w - fm.stringWidth(titleStr)) / 2, my + 44);

        int scoreRowY = my + 75;
        int colW = mw / 2;
        drawScoreColumn(g2d, "BẠN", pScore, mx + 10, scoreRowY, colW - 10, NeonTheme.CYAN);
        drawScoreColumn(g2d, "AI", aScore, mx + colW + 10, scoreRowY, colW - 20, NeonTheme.PINK);

        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.fillRect(mx + 20, my + 145, mw - 40, 1);

        int btnW = mw - 40, btnX = (w - btnW) / 2;
        drawButton(g2d, "↻  CHƠI LẠI", btnX, my + mh - 110, btnW, 38, NeonTheme.CYAN);
        drawButton(g2d, "⌂  TRANG CHỦ", btnX, my + mh - 60, btnW, 38, new Color(40, 60, 150));
    }

    private void handleResultClicks(Point p) {
        int w = getWidth(), h = getHeight();
        int mw = Math.min(300, w - 40);
        int mh = 270;
        int mx = (w - mw) / 2;
        int my = (h - mh) / 2;
        int btnW = mw - 40, btnX = (w - btnW) / 2;

        if (new Rectangle(btnX, my + mh - 110, btnW, 38).contains(p)) {
            startBattle();
        } else if (new Rectangle(btnX, my + mh - 60, btnW, 38).contains(p)) {
            battleController.stopBattle();
            parent.showScreen("HOME");
        }
    }

    private void startSnapBack() {
        snapCurX = dragX;
        snapCurY = dragY;
        int tx = startDragX, ty = startDragY;
        snapBackTimer = new Timer(15, e -> {
            double dx = tx - snapCurX, dy = ty - snapCurY;
            if (Math.abs(dx) < 2 && Math.abs(dy) < 2) {
                draggedPieceIndex = -1;
                ((Timer) e.getSource()).stop();
            } else {
                snapCurX += dx * 0.3;
                snapCurY += dy * 0.3;
                dragX = (int) snapCurX;
                dragY = (int) snapCurY;
            }
            repaint();
        });
        snapBackTimer.start();
    }

    private void drawScoreColumn(Graphics2D g2d, String label, int score,
            int x, int y, int w, Color color) {
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
        g2d.fillRoundRect(x, y, w, 60, 12, 12);
        g2d.setColor(color);
        g2d.drawRoundRect(x, y, w, 60, 12, 12);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(9f));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(NeonTheme.PURPLE);
        g2d.drawString(label, x + (w - fm.stringWidth(label)) / 2, y + 16);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 20f));
        fm = g2d.getFontMetrics();
        g2d.setColor(color);
        String s = String.valueOf(score);
        g2d.drawString(s, x + (w - fm.stringWidth(s)) / 2, y + 46);
    }

    private void drawButton(Graphics2D g2d, String text, int x, int y, int w, int h, Color color) {
        g2d.setColor(color);
        g2d.fillRoundRect(x, y, w, h, 16, 16);
        g2d.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 13f));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + (h + fm.getAscent()) / 2 - 3);
    }

    private void drawPiece(Graphics2D g2d, Block b, int x, int y, boolean glow, Color color) {
        int[][] shape = b.getShape();
        for (int i = 0; i < shape.length; i++)
            for (int j = 0; j < shape[i].length; j++)
                if (shape[i][j] == 1)
                    NeonTheme.draw3DBlock(g2d, x + j * CELL_SIZE, y + i * CELL_SIZE, CELL_SIZE - 2, color, glow);
    }

    private void drawGhost(Graphics2D g2d, Block b, int x, int y) {
        int[][] shape = b.getShape();
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
}
