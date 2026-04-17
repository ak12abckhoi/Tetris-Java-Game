package model;

import java.io.*;
import java.util.Properties;

/**
 * GameSettings — Singleton lưu trữ cài đặt trò chơi.
 * Gồm: âm thanh, level bắt đầu, giao diện màu sắc.
 */
public class GameSettings {

    private static GameSettings instance;
    private static final String SETTINGS_FILE = "settings.properties";

    private boolean soundEnabled = true;
    private int volume = 80;         // 0–100
    private int startLevel = 1;      // 1–10
    private String colorTheme = "NEON_BLUE"; // NEON_BLUE | NEON_PINK | NEON_GREEN

    private GameSettings() {
        load();
    }

    public static GameSettings getInstance() {
        if (instance == null) instance = new GameSettings();
        return instance;
    }

    // ── Getters / Setters ─────────────────────────────────────

    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean v) { soundEnabled = v; save(); }

    public int getVolume() { return volume; }
    public void setVolume(int v) { volume = Math.max(0, Math.min(100, v)); save(); }

    public int getStartLevel() { return startLevel; }
    public void setStartLevel(int v) { startLevel = Math.max(1, Math.min(10, v)); save(); }

    public String getColorTheme() { return colorTheme; }
    public void setColorTheme(String t) { colorTheme = t; save(); }

    // ── Persistence ───────────────────────────────────────────

    private void load() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(SETTINGS_FILE)) {
            props.load(in);
            soundEnabled = Boolean.parseBoolean(props.getProperty("sound", "true"));
            volume       = Integer.parseInt(props.getProperty("volume", "80"));
            startLevel   = Integer.parseInt(props.getProperty("level", "1"));
            colorTheme   = props.getProperty("theme", "NEON_BLUE");
        } catch (Exception ignored) { /* dùng giá trị mặc định */ }
    }

    public void save() {
        Properties props = new Properties();
        props.setProperty("sound",  String.valueOf(soundEnabled));
        props.setProperty("volume", String.valueOf(volume));
        props.setProperty("level",  String.valueOf(startLevel));
        props.setProperty("theme",  colorTheme);
        try (OutputStream out = new FileOutputStream(SETTINGS_FILE)) {
            props.store(out, "Neon Tetris Settings");
        } catch (Exception e) {
            System.err.println("[GameSettings] Cannot save: " + e.getMessage());
        }
    }
}
