package findfour.shared.game;

import findfour.shared.ArgumentException;

public final class GameState {
    public static final Disc STARTING_COLOR = Disc.Red;

    private final Board board;
    private Disc currentTurn;

    public GameState() {
        this.board = new Board();

        // Set the game state to the initial state.
        reset();
    }

    public void reset() {
        currentTurn = STARTING_COLOR;
        board.clear();
    }

    public void syncTo(GameState state) {
        this.board.syncTo(state.board);
        this.currentTurn = state.currentTurn;
    }

    public boolean isMoveValid(int column, Disc disc) {
        return getCurrentTurn() == disc && board.isMoveValid(column, disc);
    }

    public void makeMove(int column, Disc disc) {
        if (!isMoveValid(column, disc)) {
            throw new ArgumentException("column", "Invalid move");
        }

        board.makeMove(column, disc);
        swapTurns();
    }

    public boolean isGameOver() {
        return board.isGameOver();
    }

    public Disc getWinner() {
        return board.getWinner();
    }

    public Disc getCurrentTurn() {
        return currentTurn;
    }

    public Board getBoard() {
        return board;
    }

    private void swapTurns() {
        currentTurn = currentTurn == Disc.Red ? Disc.Yellow : Disc.Red;
    }
}
