package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * BottomNavBar — Thanh điều hướng cố định phía dưới cửa sổ.
 *
 * Gồm 3 nút: Trang chủ, Xếp hạng, Hồ sơ.
 * Nút đang active được tô nền neon và viền cyan rõ ràng.
 */
public class BottomNavBar extends JPanel {

    private final MainContainer parentContainer;
    private String activeScreen = "HOME";

    public BottomNavBar(MainContainer parentContainer) {
        this.parentContainer = parentContainer;
        setLayout(new GridLayout(1, 3, 0, 0));
        setBackground(NeonTheme.SURFACE);
        setOpaque(true);

        add(createNavButton("HOME",    "Trang chủ"));
        add(createNavButton("RANK",    "Xếp hạng"));
        add(createNavButton("PROFILE", "Hồ sơ"));
    }

    /** Cập nhật màn hình đang active và vẽ lại để phản ánh thay đổi. */
    public void setActive(String screenName) {
        this.activeScreen = screenName;
        repaint();
    }

    /**
     * Tạo một nút điều hướng được vẽ tuỳ chỉnh.
     * Bao gồm biểu tượng vector và nhãn văn bản bên dưới.
     *
     * @param id        Mã màn hình tương ứng (ví dụ: "HOME")
     * @param labelText Nhãn hiển thị bên dưới biểu tượng
     */
    private JPanel createNavButton(String id, String labelText) {
        JPanel btnPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w       = getWidth();
                int h       = getHeight();
                boolean isActive = id.equals(activeScreen);

                // Biểu tượng nằm phía trên, nhãn sát đáy
                int iconCy = h / 2 - 6;
                int textY  = h - 6;

                if (isActive) {
                    // Nền phát sáng khi được chọn
                    g2d.setColor(new Color(0, 153, 204, 50));
                    g2d.fillRoundRect(6, 3, w - 12, h - 6, 16, 16);

                    // Viền neon
                    g2d.setColor(NeonTheme.CYAN);
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawRoundRect(6, 3, w - 12, h - 6, 16, 16);

                    drawNavIcon(g2d, id, w / 2, iconCy, true);

                    g2d.setColor(NeonTheme.CYAN);
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
                } else {
                    drawNavIcon(g2d, id, w / 2, iconCy, false);

                    g2d.setColor(new Color(160, 160, 180));
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
                }

                // Nhãn văn bản căn giữa
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (w - fm.stringWidth(labelText)) / 2;
                g2d.drawString(labelText, textX, textY);
            }

            /**
             * Vẽ biểu tượng vector tương ứng với từng mục điều hướng.
             */
            private void drawNavIcon(Graphics2D g2d, String id, int cx, int cy, boolean active) {
                g2d.setColor(active ? NeonTheme.CYAN : new Color(160, 160, 180));
                g2d.setStroke(new BasicStroke(1.5f));

                switch (id) {
                    case "HOME":
                        // Hình ngôi nhà đơn giản
                        int[] xh = {cx - 7, cx, cx + 7, cx + 7, cx - 7};
                        int[] yh = {cy, cy - 7, cy, cy + 7, cy + 7};
                        g2d.drawPolygon(xh, yh, 5);
                        break;
                    case "RANK":
                        // Biểu đồ cột 3 bậc
                        g2d.drawRect(cx - 8, cy,     4, 7);
                        g2d.drawRect(cx - 2, cy - 4,  4, 11);
                        g2d.drawRect(cx + 4, cy + 2,  4, 5);
                        break;
                    case "PROFILE":
                        // Đầu người + cung vai
                        g2d.drawOval(cx - 4, cy - 7, 8, 8);
                        g2d.drawArc(cx - 7, cy + 1, 14, 10, 0, 180);
                        break;
                }
            }
        };

        btnPanel.setOpaque(false);
        btnPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parentContainer.showScreen(id);
            }
        });
        return btnPanel;
    }
}
