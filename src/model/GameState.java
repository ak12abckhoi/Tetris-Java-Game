package model;

/**
 * Quản lý trạng thái toàn bộ luồng game.
 * Luồng: MENU -> PLAYING -> PAUSED -> GAME_OVER -> (PLAYING lại)
 */
public class GameState {

    public enum State {
        MENU,
        PLAYING,
        PAUSED,
        GAME_OVER
    }

    private State current;
    private State previous; // để biết resume từ đâu

    public GameState() {
        this.current = State.MENU;
        this.previous = State.MENU;
    }

    // ── Getters ──────────────────────────────────────────────

    public State getCurrent() {
        return current;
    }

    public boolean isMenu()     { return current == State.MENU; }
    public boolean isPlaying()  { return current == State.PLAYING; }
    public boolean isPaused()   { return current == State.PAUSED; }
    public boolean isGameOver() { return current == State.GAME_OVER; }

    // ── Chuyển trạng thái ────────────────────────────────────

    /** MENU → PLAYING */
    public void startGame() {
        if (current == State.MENU || current == State.GAME_OVER) {
            transition(State.PLAYING);
        }
    }

    /** PLAYING → PAUSED */
    public void pauseGame() {
        if (current == State.PLAYING) {
            transition(State.PAUSED);
        }
    }

    /** PAUSED → PLAYING */
    public void resumeGame() {
        if (current == State.PAUSED) {
            transition(State.PLAYING);
        }
    }

    /** PLAYING → GAME_OVER */
    public void endGame() {
        if (current == State.PLAYING) {
            transition(State.GAME_OVER);
        }
    }

    /** GAME_OVER → PLAYING (chơi lại) */
    public void restartGame() {
        if (current == State.GAME_OVER) {
            transition(State.PLAYING);
        }
    }

    /** Bất kỳ trạng thái → MENU */
    public void goToMenu() {
        transition(State.MENU);
    }

    // ── Nội bộ ───────────────────────────────────────────────

    private void transition(State next) {
        this.previous = this.current;
        this.current  = next;
    }
}