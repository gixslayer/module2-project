package findfour.shared.game;

import findfour.shared.ArgumentException;
import findfour.shared.ArgumentNullException;
import findfour.shared.ArgumentOutOfRangeException;

public class Board {
    public static final int COLUMNS = 7;
    public static final int ROWS = 6;

    private final Disc[] grid;

    public Board() {
        this.grid = new Disc[COLUMNS * ROWS];
    }

    public void clear() {
        for (int i = 0; i < grid.length; i++) {
            grid[i] = Disc.None;
        }
    }

    public Disc getSlot(int column, int row) {
        if (column < 0 || column >= COLUMNS) {
            throw new ArgumentOutOfRangeException("column", 0, COLUMNS);
        }
        if (row < 0 || row >= ROWS) {
            throw new ArgumentOutOfRangeException("row", 0, ROWS);
        }

        return grid[row * COLUMNS + column];
    }

    public void setFrom(Disc[] newGrid) {
        if (newGrid == null) {
            throw new ArgumentNullException("newGrid");
        }
    }

    public boolean isMoveValid(int column, Disc disc) {
        if (column < 0 || column >= COLUMNS) {
            return false;
        } else if (disc == Disc.None) {
            return false;
        } else if (!hasFreeSlot(column)) {
            return false;
        }

        return true;
    }

    public void makeMove(int column, Disc disc) {
        if (!isMoveValid(column, disc)) {
            throw new ArgumentException("column", "invalid move");
        }

        setSlot(column, getNextFreeSlot(column), disc);
    }

    public int getNextFreeSlot(int column) {
        if (column < 0 || column >= COLUMNS) {
            throw new ArgumentOutOfRangeException("column", 0, COLUMNS);
        }

        for (int row = 0; row < ROWS; row++) {
            if (getSlot(column, row) != Disc.None) {
                return row - 1;
            }
        }

        return ROWS - 1;
    }

    private void setSlot(int column, int row, Disc disc) {
        assert column >= 0 && column < COLUMNS;
        assert row >= 0 && row < ROWS;

        grid[row * COLUMNS + column] = disc;
    }

    private boolean hasFreeSlot(int column) {
        assert column >= 0 && column < COLUMNS;

        return getSlot(column, 0) == Disc.None;
    }
}
