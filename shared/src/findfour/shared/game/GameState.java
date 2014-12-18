package findfour.shared.game;

public final class GameState {
    public static final Disc STARTING_COLOR = Disc.Red;

    private final Board board;
    private Disc currentTurn;

    public GameState() {
        this.board = new Board();
        this.currentTurn = STARTING_COLOR;

        board.clear();
    }

    public void syncTo(GameState state) {
        this.board.syncTo(state.board);
        this.currentTurn = state.currentTurn;
    }

    public void swapTurns() {
        currentTurn = currentTurn == Disc.Red ? Disc.Yellow : Disc.Red;
    }

    public Disc getCurrentTurn() {
        return currentTurn;
    }

    public Board getBoard() {
        return board;
    }
}
