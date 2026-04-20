package view;

import model.GameSettings;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.*;

/**
 * SettingsPanel — Màn hình Cài đặt âm thanh.
 *
 * Gồm hai tuỳ chỉnh:
 *   - Nút gạt bật/tắt âm thanh (Toggle Switch tuỳ chỉnh)
 *   - Thanh kéo điều chỉnh âm lượng 0–100% (Slider tuỳ chỉnh)
 *
 * Mọi thay đổi được ghi nhận ngay lập tức thông qua GameSettings,
 * class này đồng bộ tới SoundManager để áp dụng tức thì.
 */
public class SettingsPanel extends JPanel {

    private final MainContainer parent;
    private final GameSettings  settings;

    private boolean soundOn;
    private int     volume;

    private JPanel  soundToggle;
    private JLabel  volumeValueLabel;
    private JSlider volumeSlider;

    public SettingsPanel(MainContainer parent) {
        this.parent   = parent;
        this.settings = GameSettings.getInstance();
        this.soundOn  = settings.isSoundEnabled();
        this.volume   = settings.getVolume();

        setLayout(null);
        setBackground(NeonTheme.BACKGROUND);

        // Rebuild khi resize hoặc màn hình hiện ra lại
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { buildUI(); }
            @Override public void componentShown(ComponentEvent e) {
                // Đồng bộ lại giá trị từ settings khi quay trở lại màn hình này
                soundOn = settings.isSoundEnabled();
                volume  = settings.getVolume();
                buildUI();
            }
        });

        buildUI();
    }

    /** Xây dựng lại toàn bộ giao diện theo kích thước cửa sổ hiện tại. */
    private void buildUI() {
        removeAll();

        int w = getWidth()  > 0 ? getWidth()  : 390;
        int h = getHeight() > 0 ? getHeight() : 600;

        // ── Header ───────────────────────────────────────────────
        JButton backBtn = new JButton("←");
        backBtn.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 22f));
        backBtn.setForeground(Color.WHITE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> parent.showScreen("HOME"));
        backBtn.setBounds(10, 20, 50, 40);
        add(backBtn);

        JLabel title = new JLabel("CÀI ĐẶT", SwingConstants.CENTER);
        title.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 26f));
        title.setForeground(NeonTheme.CYAN);
        title.setBounds(0, 20, w, 40);
        add(title);

        // Đường kẻ ngang phân cách header
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 30));
        sep.setBounds(20, 70, w - 40, 1);
        add(sep);

        // ── Dòng 1: Âm thanh + Toggle ────────────────────────────
        int rowY  = 110;
        int rowH  = 60;
        int labelX   = 30;
        int controlX = w - 130;

        JLabel soundLabel = makeLabel("Âm thanh");
        soundLabel.setBounds(labelX, rowY, 200, rowH);
        add(soundLabel);

        soundToggle = makeToggle(soundOn);
        soundToggle.setBounds(controlX, rowY + 12, 72, 36);
        add(soundToggle);

        // Đường kẻ ngang phân cách dòng
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(255, 255, 255, 20));
        sep2.setBounds(20, rowY + rowH + 5, w - 40, 1);
        add(sep2);

        // ── Dòng 2: Âm lượng + Slider ────────────────────────────
        rowY += rowH + 20;

        JLabel volLabel = makeLabel("Âm lượng");
        volLabel.setBounds(labelX, rowY, 160, 40);
        add(volLabel);

        volumeValueLabel = new JLabel(volume + "%", SwingConstants.CENTER);
        volumeValueLabel.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 14f));
        volumeValueLabel.setForeground(NeonTheme.CYAN);
        volumeValueLabel.setBounds(w - 62, rowY, 50, 40);
        add(volumeValueLabel);

        volumeSlider = new JSlider(0, 100, volume);
        volumeSlider.setOpaque(false);
        styleSlider(volumeSlider);
        volumeSlider.setBounds(labelX + 110, rowY + 10, w - labelX - 110 - 65, 28);
        volumeSlider.addChangeListener(e -> {
            volume = volumeSlider.getValue();
            volumeValueLabel.setText(volume + "%");
            settings.setVolume(volume); // Áp dụng ngay lập tức qua SoundManager
        });
        add(volumeSlider);

        updateControlsEnabled();
        revalidate();
        repaint();
    }

    // ── Hàm phụ ──────────────────────────────────────────────────

    /** Tạo nhãn chữ phong cách trắng đậm. */
    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 17f));
        l.setForeground(Color.WHITE);
        return l;
    }

    /**
     * Tạo nút gạt Toggle Switch được vẽ tuỳ chỉnh bằng paintComponent.
     * Trạng thái "on/off" lưu trong clientProperty "on".
     */
    private JPanel makeToggle(boolean on) {
        JPanel toggle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean state = (Boolean) getClientProperty("on");
                int tw = getWidth(), th = getHeight();

                // Thanh nền (track): xanh khi bật, xám khi tắt
                g2.setColor(state ? NeonTheme.CYAN : new Color(80, 80, 120));
                g2.fillRoundRect(0, 0, tw, th, th, th);

                // Viền glow khi bật
                if (state) {
                    g2.setColor(new Color(0, 240, 255, 60));
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRoundRect(1, 1, tw - 2, th - 2, th, th);
                }

                // Nút tròn (thumb): bên phải khi bật, bên trái khi tắt
                int thumbX = state ? tw - th + 3 : 3;
                g2.setColor(Color.WHITE);
                g2.fillOval(thumbX, 3, th - 6, th - 6);
            }
        };
        toggle.setOpaque(false);
        toggle.putClientProperty("on", on);
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                soundOn = !soundOn;
                settings.setSoundEnabled(soundOn); // Đồng bộ tới SoundManager
                toggle.putClientProperty("on", soundOn);
                toggle.repaint();
                updateControlsEnabled();
            }
        });
        return toggle;
    }

    /** Bật/tắt slider và nhãn % theo trạng thái âm thanh hiện tại. */
    private void updateControlsEnabled() {
        if (volumeSlider     != null) volumeSlider.setEnabled(soundOn);
        if (volumeValueLabel != null)
            volumeValueLabel.setForeground(soundOn ? NeonTheme.CYAN : new Color(120, 120, 160));
    }

    /**
     * Tuỳ chỉnh giao diện JSlider bằng BasicSliderUI override.
     * Track: dải nền tối (đã fill) + phần đã kéo màu cyan.
     * Thumb: chấm tròn trắng với glow nhẹ.
     */
    private void styleSlider(JSlider s) {
        s.setUI(new BasicSliderUI(s) {
            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Rectangle t  = trackRect;
                int cy = t.y + t.height / 2;

                // Phần nền (chưa fill)
                g2.setColor(new Color(60, 65, 130));
                g2.fillRoundRect(t.x, cy - 3, t.width, 6, 6, 6);

                // Phần đã kéo (fill đến vị trí thumb)
                int thumbPos = thumbRect.x + thumbRect.width / 2;
                g2.setColor(soundOn ? NeonTheme.CYAN : new Color(80, 80, 120));
                g2.fillRoundRect(t.x, cy - 3, thumbPos - t.x, 6, 6, 6);
            }

            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int x = thumbRect.x, y = thumbRect.y;
                int d = Math.min(thumbRect.width, thumbRect.height);

                // Vòng glow xung quanh thumb
                g2.setColor(new Color(0, 240, 255, 50));
                g2.fillOval(x - 2, y + (thumbRect.height - d) / 2 - 2, d + 4, d + 4);

                // Thumb chính
                g2.setColor(soundOn ? Color.WHITE : new Color(140, 140, 180));
                g2.fillOval(x, y + (thumbRect.height - d) / 2, d, d);
            }
        });
    }
}
