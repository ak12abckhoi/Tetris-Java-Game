package model;

/**
 * GameState — Quản lý vòng đời trạng thái của một ván chơi đơn.
 *
 * Luồng trạng thái hợp lệ:
 *   MENU → PLAYING → PAUSED → PLAYING → GAME_OVER → PLAYING (chơi lại)
 *                                                  → MENU
 */
public class GameState {

    public enum State {
        MENU,
        PLAYING,
        PAUSED,
        GAME_OVER
    }

    private State current;
    private State previous; // Lưu lại trạng thái trước, dự phòng nếu cần resume

    public GameState() {
        this.current  = State.MENU;
        this.previous = State.MENU;
    }

    // ── Kiểm tra trạng thái ──────────────────────────────────────

    public State getCurrent()   { return current; }
    public boolean isMenu()     { return current == State.MENU; }
    public boolean isPlaying()  { return current == State.PLAYING; }
    public boolean isPaused()   { return current == State.PAUSED; }
    public boolean isGameOver() { return current == State.GAME_OVER; }

    // ── Chuyển trạng thái ────────────────────────────────────────

    /** MENU / GAME_OVER → PLAYING */
    public void startGame() {
        if (current == State.MENU || current == State.GAME_OVER) {
            transition(State.PLAYING);
        }
    }

    /** PLAYING → PAUSED */
    public void pauseGame() {
        if (current == State.PLAYING) transition(State.PAUSED);
    }

    /** PAUSED → PLAYING */
    public void resumeGame() {
        if (current == State.PAUSED) transition(State.PLAYING);
    }

    /** PLAYING → GAME_OVER */
    public void endGame() {
        if (current == State.PLAYING) transition(State.GAME_OVER);
    }

    /** GAME_OVER → PLAYING (chơi lại không qua màn Menu) */
    public void restartGame() {
        if (current == State.GAME_OVER) transition(State.PLAYING);
    }

    /** Bất kỳ trạng thái → MENU */
    public void goToMenu() {
        transition(State.MENU);
    }

    // ── Nội bộ ───────────────────────────────────────────────────

    private void transition(State next) {
        this.previous = this.current;
        this.current  = next;
    }
}