package model;

/**
 * GameSettings — Singleton lưu trữ và đồng bộ cài đặt âm thanh của trò chơi.
 *
 * Khi thay đổi giá trị, class này tự động gọi sang SoundManager để áp dụng ngay.
 * Cài đặt được lưu xuống file "settings.properties" để ghi nhớ giữa các lần chạy.
 */

import java.io.*;
import java.util.Properties;

public class GameSettings {

    private static GameSettings instance;
    private static final String FILE = "settings.properties";

    private boolean soundEnabled = true;
    private int volume = 80; // phạm vi: 0–100

    private GameSettings() {
        load();
    }

    /** Trả về instance duy nhất (Singleton). */
    public static GameSettings getInstance() {
        if (instance == null) instance = new GameSettings();
        return instance;
    }

    // ── Getter / Setter ──────────────────────────────────────────

    public boolean isSoundEnabled() { return soundEnabled; }

    /**
     * Bật/tắt toàn bộ âm thanh (nhạc nền + hiệu ứng).
     * Thay đổi có hiệu lực ngay lập tức thông qua SoundManager.
     */
    public void setSoundEnabled(boolean v) {
        soundEnabled = v;
        SoundManager.setSfxEnabled(v);
        SoundManager.setMusicEnabled(v);
        save();
    }

    public int getVolume() { return volume; }

    /**
     * Điều chỉnh âm lượng (0–100).
     * Thay đổi có hiệu lực ngay lập tức thông qua SoundManager.
     */
    public void setVolume(int v) {
        volume = Math.max(0, Math.min(100, v));
        float f = volume / 100f;
        SoundManager.setVolumeSFX(f);
        SoundManager.setVolumeBGM(f);
        save();
    }

    // ── Lưu / Tải ────────────────────────────────────────────────

    /** Đọc cài đặt từ file và áp dụng ngay vào SoundManager. */
    private void load() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(FILE)) {
            props.load(in);
            soundEnabled = Boolean.parseBoolean(props.getProperty("sound", "true"));
            volume       = Integer.parseInt(props.getProperty("volume", "80"));
        } catch (Exception ignored) {
            // Nếu file chưa tồn tại hoặc bị lỗi, dùng giá trị mặc định
        }
        // Áp dụng ngay sau khi đọc
        SoundManager.setSfxEnabled(soundEnabled);
        SoundManager.setMusicEnabled(soundEnabled);
        float f = volume / 100f;
        SoundManager.setVolumeSFX(f);
        SoundManager.setVolumeBGM(f);
    }

    /** Ghi cài đặt hiện tại ra file. */
    public void save() {
        Properties props = new Properties();
        props.setProperty("sound",  String.valueOf(soundEnabled));
        props.setProperty("volume", String.valueOf(volume));
        try (OutputStream out = new FileOutputStream(FILE)) {
            props.store(out, "Neon Tetris Settings");
        } catch (Exception e) {
            System.err.println("[GameSettings] Không thể lưu cài đặt: " + e.getMessage());
        }
    }
}
