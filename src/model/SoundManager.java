package model;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.*;


public class SoundManager {

    // ── Cài đặt âm lượng ─────────────────────────────────────
    private static float volumeBGM = 0.6f;  // 0.0 – 1.0
    private static float volumeSFX = 0.8f;

    private static boolean musicEnabled = true;
    private static boolean sfxEnabled   = true;

    // Clip nhạc nền (loop)
    private static Clip bgmClip = null;

    // Thread pool cho SFX — tránh tạo thread mới liên tục
    private static final ExecutorService sfxPool =
            Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "SFX-Thread");
                t.setDaemon(true); // tự tắt khi app thoát
                return t;
            });

    // ── Nhạc nền ─────────────────────────────────────────────

    /**
     * Phát nhạc nền lặp lại liên tục.
     * @param filePath đường dẫn file .wav, ví dụ "assets/bgm.wav"
     */
    public static void startBGM(String filePath) {
        if (!musicEnabled) return;
        stopBGM(); // dừng nhạc cũ nếu đang phát
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filePath));
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);
            setClipVolume(bgmClip, volumeBGM);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY); // lặp vô hạn
            bgmClip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("[SoundManager] Không tải được BGM: " + filePath);
        }
    }

    /** Dừng nhạc nền */
    public static void stopBGM() {
        if (bgmClip != null) {
            if (bgmClip.isRunning()) {
                bgmClip.stop();
            }
            bgmClip.close();  // luôn close để giải phóng tài nguyên
            bgmClip = null;
        }
    }

    /** Tạm dừng nhạc nền (khi game Pause) */
    public static void pauseBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
        }
    }

    /** Tiếp tục nhạc nền (khi Resume) */
    public static void resumeBGM() {
        if (!musicEnabled) return;
        if (bgmClip != null && !bgmClip.isRunning()) {
            bgmClip.start();
        }
    }

    // ── SFX tổng hợp (không cần file .wav) ───────────────────

    /** Tiếng click khi đặt khối */
    public static void playClick() {
        if (!sfxEnabled) return;
        sfxPool.execute(() -> playTone(600, 80, volumeSFX * 0.5f));
    }

    /** Tiếng vui khi xóa được hàng / ăn điểm */
    public static void playScore() {
        if (!sfxEnabled) return;
        // Arpeggio ngắn: Do – Mi – Sol
        sfxPool.execute(() -> {
            playTone(523, 80, volumeSFX * 0.625f);
            sleep(90);
            playTone(659, 80, volumeSFX * 0.625f);
            sleep(90);
            playTone(784, 120, volumeSFX * 0.625f);
        });
    }

    /** Combo! — âm thanh cao hơn khi xóa nhiều hàng */
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

    /** Âm thanh xuống thang khi thua game */
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

    /** Fanfare chiến thắng */
    public static void playWin() {
        if (!sfxEnabled) return;
        sfxPool.execute(() -> {
            int[] notes = {523, 659, 784, 1047, 1319};
            for (int f : notes) {
                playTone(f, 100, volumeSFX * 0.75f);
                sleep(110);
            }
        });
    }

    // ── Cài đặt âm lượng & bật/tắt ───────────────────────────

    public static void setMusicEnabled(boolean on) {
        musicEnabled = on;
        if (!on) stopBGM();
    }

    public static void setSfxEnabled(boolean on) {
        sfxEnabled = on;
    }

    public static boolean isMusicEnabled() { return musicEnabled; }
    public static boolean isSfxEnabled()   { return sfxEnabled; }

    public static void setVolumeBGM(float v) {
        volumeBGM = clamp(v);
        if (bgmClip != null) setClipVolume(bgmClip, volumeBGM);
    }

    public static void setVolumeSFX(float v) {
        volumeSFX = clamp(v);
    }

    // ── Nội bộ ───────────────────────────────────────────────

    /**
     * Sinh âm thanh beep tổng hợp qua PCM — không cần file .wav.
     * @param freq   tần số Hz (ví dụ 440 = La)
     * @param millis thời lượng milliseconds
     * @param vol    âm lượng 0.0 – 1.0
     */
    private static void playTone(int freq, int millis, float vol) {
        try {
            float sampleRate = 44100f;
            int samples = (int) (sampleRate * millis / 1000f);
            byte[] buf = new byte[samples * 2];

            for (int i = 0; i < samples; i++) {
                // Sóng sine
                double angle = 2.0 * Math.PI * i * freq / sampleRate;
                // Envelope: fade-out nhẹ để tránh click
                double envelope = 1.0 - (double) i / samples;
                short sample = (short) (Math.sin(angle) * envelope * vol * Short.MAX_VALUE);
                buf[2 * i]     = (byte) (sample & 0xFF);
                buf[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
            }

            AudioFormat fmt = new AudioFormat(sampleRate, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(fmt);
            line.start();
            line.write(buf, 0, buf.length);
            line.drain();
            line.close();
        } catch (LineUnavailableException e) {
            // bỏ qua nếu thiết bị âm thanh bận
        }
    }

    private static void setClipVolume(Clip clip, float vol) {
        try {
            FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            // Chuyển 0.0–1.0 sang dB
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