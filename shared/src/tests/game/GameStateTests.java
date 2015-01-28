package tests.game;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import findfour.shared.game.Disc;
import findfour.shared.game.GameState;

public class GameStateTests {
    private GameState state1;
    private GameState state2;

    @Before
    public void setUp() throws Exception {
        state1 = new GameState();
        state2 = new GameState();

        state2.makeMove(0, Disc.Red);
    }

    @Test
    public void testReset() {
        state2.reset();

        assertTrue(state2.getCurrentTurn() == Disc.Red);
    }

    @Test
    public void testSyncTo() {
        state1.syncTo(state2);

        assertTrue(state1.getCurrentTurn() == Disc.Yellow);
    }

    @Test
    public void testIsGameOver() {
        assertFalse(state1.isGameOver());
        assertFalse(state2.isGameOver());

        state2.makeMove(1, Disc.Yellow);
        state2.makeMove(0, Disc.Red);
        state2.makeMove(1, Disc.Yellow);
        state2.makeMove(0, Disc.Red);
        state2.makeMove(1, Disc.Yellow);
        state2.makeMove(0, Disc.Red);

        assertTrue(state2.isGameOver());
    }

    @Test
    public void testGetWinner() {
        assertTrue(state1.getWinner() == Disc.None);
        assertTrue(state2.getWinner() == Disc.None);

        state2.makeMove(1, Disc.Yellow);
        state2.makeMove(0, Disc.Red);
        state2.makeMove(1, Disc.Yellow);
        state2.makeMove(0, Disc.Red);
        state2.makeMove(1, Disc.Yellow);
        state2.makeMove(0, Disc.Red);

        assertTrue(state2.getWinner() == Disc.Red);
    }

    @Test
    public void testGetCurrentTurn() {
        assertTrue(state1.getCurrentTurn() == Disc.Red);
        assertTrue(state2.getCurrentTurn() == Disc.Yellow);
    }

    @Test
    public void testGetBoard() {
        assertTrue(state1.getBoard() != state2.getBoard());
    }

}
