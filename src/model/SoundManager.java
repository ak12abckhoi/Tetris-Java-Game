package model;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.*;

/**
 * SoundManager — Quản lý toàn bộ âm thanh của trò chơi.
 *
 * Hỗ trợ hai loại âm thanh:
 *   - Nhạc nền (BGM): phát lặp liên tục từ file .wav
 *   - Hiệu ứng âm (SFX): sinh tổng hợp bằng PCM, không cần file ngoài
 *
 * Âm lượng được điều chỉnh riêng cho BGM và SFX, phạm vi 0.0 – 1.0.
 */
public class SoundManager {

    // ── Cấu hình âm lượng ────────────────────────────────────────

    private static float volumeBGM = 0.6f;
    private static float volumeSFX = 0.8f;

    private static boolean musicEnabled = true;
    private static boolean sfxEnabled   = true;

    // ── Nhạc nền (BGM) ───────────────────────────────────────────

    private static Clip bgmClip = null;

    // Thread pool cho SFX — tái sử dụng thread, daemon để tự tắt khi app đóng
    private static final ExecutorService sfxPool =
            Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "SFX-Thread");
                t.setDaemon(true);
                return t;
            });

    /**
     * Phát nhạc nền lặp liên tục từ file .wav.
     * Dừng nhạc cũ trước khi bắt đầu nhạc mới.
     *
     * @param filePath đường dẫn tới file, ví dụ "assets/bgm.wav"
     */
    public static void startBGM(String filePath) {
        if (!musicEnabled) return;
        stopBGM();
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filePath));
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);
            setClipVolume(bgmClip, volumeBGM);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("[SoundManager] Không tải được BGM: " + filePath);
        }
    }

    /** Dừng và giải phóng nhạc nền. */
    public static void stopBGM() {
        if (bgmClip != null) {
            if (bgmClip.isRunning()) bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }

    /** Tạm dừng nhạc nền (khi game Pause). */
    public static void pauseBGM() {
        if (bgmClip != null && bgmClip.isRunning()) bgmClip.stop();
    }

    /** Tiếp tục nhạc nền (khi Resume). */
    public static void resumeBGM() {
        if (!musicEnabled) return;
        if (bgmClip != null && !bgmClip.isRunning()) bgmClip.start();
    }

    // ── Hiệu ứng âm (SFX) ────────────────────────────────────────

    /** Tiếng kêu ngắn khi đặt khối thành công. */
    public static void playClick() {
        if (!sfxEnabled) return;
        sfxPool.execute(() -> playTone(600, 80, volumeSFX * 0.5f));
    }

    /** Arpeggio Do–Mi–Sol ngắn khi xoá được hàng / ăn điểm. */
    public static void playScore() {
        if (!sfxEnabled) return;
        sfxPool.execute(() -> {
            playTone(523, 80, volumeSFX * 0.625f);
            sleep(90);
            playTone(659, 80, volumeSFX * 0.625f);
            sleep(90);
            playTone(784, 120, volumeSFX * 0.625f);
        });
    }

    /** Âm thanh combo khi xoá liên tiếp nhiều hàng. */
    public static void playCombo() {
        if (!sfxEnabled) return;
        sfxPool.execute(() -> {
            playTone(523,  80, volumeSFX * 0.75f);
            sleep(80);
            playTone(784,  80, volumeSFX * 0.75f);
            sleep(80);
            playTone(1047, 150, volumeSFX * 0.75f);
        });
    }

    /** Âm thanh xuống thang khi thua (Game Over). */
    public static void playGameOver() {
        if (!sfxEnabled) return;
        sfxPool.execute(() -> {
            playTone(440, 200, volumeSFX * 0.625f);
            sleep(210);
            playTone(370, 200, volumeSFX * 0.625f);
            sleep(210);
            playTone(311, 200, volumeSFX * 0.625f);
            sleep(210);
            playTone(261, 350, volumeSFX * 0.625f);
        });
    }

    /** Fanfare chiến thắng. */
    public static void playWin() {
        if (!sfxEnabled) return;
        sfxPool.execute(() -> {
            for (int f : new int[]{523, 659, 784, 1047, 1319}) {
                playTone(f, 100, volumeSFX * 0.75f);
                sleep(110);
            }
        });
    }

    // ── Điều khiển bật/tắt & âm lượng ───────────────────────────

    public static void setMusicEnabled(boolean on) {
        musicEnabled = on;
        if (!on) stopBGM();
    }

    public static void setSfxEnabled(boolean on) {
        sfxEnabled = on;
    }

    public static boolean isMusicEnabled() { return musicEnabled; }
    public static boolean isSfxEnabled()   { return sfxEnabled; }

    /** Điều chỉnh âm lượng nhạc nền (0.0 – 1.0). Áp dụng ngay nếu đang phát. */
    public static void setVolumeBGM(float v) {
        volumeBGM = clamp(v);
        if (bgmClip != null) setClipVolume(bgmClip, volumeBGM);
    }

    /** Điều chỉnh âm lượng hiệu ứng âm (0.0 – 1.0). */
    public static void setVolumeSFX(float v) {
        volumeSFX = clamp(v);
    }

    // ── Nội bộ ───────────────────────────────────────────────────

    /**
     * Tổng hợp và phát âm thanh hình sin qua PCM — không cần file .wav.
     *
     * @param freq   tần số Hz (440 = La)
     * @param millis thời lượng phát (ms)
     * @param vol    âm lượng 0.0 – 1.0
     */
    private static void playTone(int freq, int millis, float vol) {
        try {
            float sampleRate = 44100f;
            int samples = (int) (sampleRate * millis / 1000f);
            byte[] buf = new byte[samples * 2];

            for (int i = 0; i < samples; i++) {
                double angle    = 2.0 * Math.PI * i * freq / sampleRate;
                double envelope = 1.0 - (double) i / samples; // fade-out nhẹ, tránh clip
                short sample = (short) (Math.sin(angle) * envelope * vol * Short.MAX_VALUE);
                buf[2 * i]     = (byte) (sample & 0xFF);
                buf[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
            }

            AudioFormat fmt = new AudioFormat(sampleRate, 16, 1, true, false);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, fmt));
            line.open(fmt);
            line.start();
            line.write(buf, 0, buf.length);
            line.drain();
            line.close();
        } catch (LineUnavailableException e) {
            // Bỏ qua nếu thiết bị âm thanh đang bận
        }
    }

    /** Chuyển âm lượng 0.0–1.0 sang đơn vị dB và áp dụng lên Clip. */
    private static void setClipVolume(Clip clip, float vol) {
        try {
            FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log10(Math.max(vol, 0.0001)) * 20);
            fc.setValue(Math.max(fc.getMinimum(), Math.min(fc.getMaximum(), dB)));
        } catch (IllegalArgumentException ignored) {}
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}