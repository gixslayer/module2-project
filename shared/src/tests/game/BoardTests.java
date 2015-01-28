package tests.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import findfour.shared.game.Board;
import findfour.shared.game.Disc;

public class BoardTests {
    private Board b1;
    private Board b2;
    private Board b3;

    @Before
    public void setUp() throws Exception {
        b1 = new Board();
        b2 = new Board();
        b3 = new Board();

        b2.setSlot(0, 5, Disc.Red);
        b2.setSlot(0, 4, Disc.Red);
        b2.setSlot(0, 3, Disc.Red);
        b2.setSlot(0, 2, Disc.Red);

        b3.setSlot(0, 5, Disc.Yellow);
        b3.setSlot(1, 5, Disc.Yellow);
        b3.setSlot(2, 5, Disc.Red);
        b3.setSlot(3, 5, Disc.Yellow);
        b3.setSlot(4, 5, Disc.Yellow);
        b3.setSlot(5, 5, Disc.Red);
        b3.setSlot(6, 5, Disc.Red);
        b3.setSlot(0, 4, Disc.Red);
        b3.setSlot(1, 4, Disc.Yellow);
        b3.setSlot(1, 3, Disc.Yellow);
        b3.setSlot(1, 2, Disc.Red);
        b3.setSlot(1, 1, Disc.Yellow);
        b3.setSlot(1, 0, Disc.Yellow);
    }

    @Test
    public void testBoard() {
        assertNotNull(b1);
        assertNotNull(b2);
        assertNotNull(b3);
    }

    @Test
    public void testGetNextFreeSlot() {
        assertEquals(b1.getNextFreeSlot(0), 5);
        assertEquals(b2.getNextFreeSlot(0), 1);
        assertEquals(b3.getNextFreeSlot(0), 3);
    }

    @Test
    public void testIsMoveValid() {
        assertTrue(b1.isMoveValid(0, Disc.Red));
        assertTrue(b2.isMoveValid(0, Disc.Red));
        assertTrue(b3.isMoveValid(0, Disc.Yellow));
        assertFalse(b1.isMoveValid(8, Disc.Red));
        assertFalse(b2.isMoveValid(4, Disc.None));
        assertFalse(b3.isMoveValid(1, Disc.Yellow));
    }

    @Test
    public void testGetSlot() {
        assertTrue(b1.getSlot(0, 5) == Disc.None);
        assertTrue(b2.getSlot(0, 3) == Disc.Red);
        assertTrue(b3.getSlot(0, 5) == Disc.Yellow);
        assertTrue(b3.getSlot(1, 1) == Disc.Yellow);
    }

    @Test
    public void testSyncTo() {
        b1.syncTo(b3);

        for (int i = 0; i < Board.COLUMNS; i++) {
            for (int j = 0; j < Board.ROWS; j++) {
                assertTrue(b1.getSlot(i, j) == b3.getSlot(i, j));
            }
        }
    }

    @Test
    public void testMakeMove() {
        b1.makeMove(0, Disc.Red);
        b3.makeMove(2, Disc.Yellow);

        assertTrue(b1.getSlot(0, 5) == Disc.Red);
        assertTrue(b3.getSlot(2, 4) == Disc.Yellow);
    }

    @Test
    public void testHasWinner() {
        assertTrue(b2.hasWinner());
        assertFalse(b1.hasWinner());
        assertFalse(b3.hasWinner());
    }

    @Test
    public void testGetWinner() {
        assertTrue(b1.getWinner() == Disc.None);
        assertTrue(b2.getWinner() == Disc.Red);
        assertTrue(b3.getWinner() == Disc.None);
    }

    @Test
    public void testIsFull() {
        for (int i = 0; i < Board.COLUMNS; i++) {
            for (int j = 0; j < Board.ROWS; j++) {
                b1.setSlot(i, j, Disc.Red);
            }
        }

        assertTrue(b1.isFull());
        assertFalse(b2.isFull());
        assertFalse(b3.isFull());
    }

    @Test
    public void testSetSlot() {
        b1.setSlot(0, 0, Disc.Red);
        b2.setSlot(4, 3, Disc.Yellow);
        b3.setSlot(0, 0, Disc.None);

        assertTrue(b1.getSlot(0, 0) == Disc.Red);
        assertTrue(b2.getSlot(4, 3) == Disc.Yellow);
        assertTrue(b3.getSlot(0, 0) == Disc.None);
    }

}
