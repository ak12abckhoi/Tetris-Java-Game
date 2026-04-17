package view;

import model.GameSettings;
import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private MainContainer parent;
    private GameSettings settings;

    public SettingsPanel(MainContainer parent) {
        this.parent = parent;
        this.settings = GameSettings.getInstance();
        setLayout(new BorderLayout());
        setBackground(NeonTheme.BACKGROUND);
        initUI();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 24));
        backBtn.setForeground(Color.WHITE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> parent.showScreen("HOME"));
        header.add(backBtn, BorderLayout.WEST);

        JLabel titleStr = new JLabel("CÀI ĐẶT", SwingConstants.CENTER);
        titleStr.setFont(NeonTheme.MAIN_FONT.deriveFont(Font.BOLD, 28f));
        titleStr.setForeground(NeonTheme.CYAN);
        header.add(titleStr, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // Content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Sound Toggle
        JPanel soundPanel = createRow("Âm thanh");
        JCheckBox soundCheck = new JCheckBox();
        soundCheck.setSelected(settings.isSoundEnabled());
        soundCheck.setOpaque(false);
        soundCheck.addActionListener(e -> settings.setSoundEnabled(soundCheck.isSelected()));
        soundPanel.add(soundCheck, BorderLayout.EAST);
        centerPanel.add(soundPanel);
        centerPanel.add(Box.createVerticalStrut(20));

        // Volume Slider
        JPanel volumePanel = createRow("Âm lượng");
        JSlider volumeSlider = new JSlider(0, 100, settings.getVolume());
        volumeSlider.setOpaque(false);
        volumeSlider.setForeground(NeonTheme.CYAN);
        volumeSlider.addChangeListener(e -> settings.setVolume(volumeSlider.getValue()));
        volumePanel.add(volumeSlider, BorderLayout.EAST);
        centerPanel.add(volumePanel);
        centerPanel.add(Box.createVerticalStrut(20));

        // Level Slider
        JPanel levelPanel = createRow("Cấp độ (1-10)");
        JSlider levelSlider = new JSlider(1, 10, settings.getStartLevel());
        levelSlider.setOpaque(false);
        levelSlider.setMajorTickSpacing(1);
        levelSlider.setPaintTicks(true);
        levelSlider.setPaintLabels(true);
        levelSlider.setForeground(NeonTheme.CYAN);
        levelSlider.addChangeListener(e -> settings.setStartLevel(levelSlider.getValue()));
        levelPanel.add(levelSlider, BorderLayout.EAST);
        centerPanel.add(levelPanel);
        centerPanel.add(Box.createVerticalStrut(20));

        // Theme Combobox
        JPanel themePanel = createRow("Giao diện");
        String[] themes = {"NEON_BLUE", "NEON_PINK", "NEON_GREEN"};
        JComboBox<String> themeBox = new JComboBox<>(themes);
        themeBox.setSelectedItem(settings.getColorTheme());
        themeBox.addActionListener(e -> settings.setColorTheme((String)themeBox.getSelectedItem()));
        themePanel.add(themeBox, BorderLayout.EAST);
        centerPanel.add(themePanel);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createRow(String label) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(NeonTheme.MAIN_FONT.deriveFont(18f));
        lbl.setForeground(Color.WHITE);
        row.add(lbl, BorderLayout.WEST);
        
        return row;
    }
}
