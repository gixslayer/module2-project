package findfour.shared.game;

import findfour.shared.ArgumentException;
import findfour.shared.ArgumentOutOfRangeException;

public class Board {
    public static final int COLUMNS = 7;
    public static final int ROWS = 6;

    private final Disc[] grid;

    public Board() {
        this.grid = new Disc[COLUMNS * ROWS];
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

    public Disc getSlot(int column, int row) {
        if (column < 0 || column >= COLUMNS) {
            throw new ArgumentOutOfRangeException("column", 0, COLUMNS);
        }
        if (row < 0 || row >= ROWS) {
            throw new ArgumentOutOfRangeException("row", 0, ROWS);
        }

        return grid[row * COLUMNS + column];
    }

    void syncTo(Board board) {
        for (int i = 0; i < board.grid.length; i++) {
            this.grid[i] = board.grid[i];
        }
    }

    void clear() {
        for (int i = 0; i < grid.length; i++) {
            grid[i] = Disc.None;
        }
    }

    void makeMove(int column, Disc disc) {
        if (!isMoveValid(column, disc)) {
            throw new ArgumentException("column", "invalid move");
        }

        setSlot(column, getNextFreeSlot(column), disc);
    }

    boolean isGameOver() {
        return isFull() || hasWinner();
    }

    boolean hasWinner() {
        return isWinner(Disc.Yellow) || isWinner(Disc.Red);
    }

    Disc getWinner() {
        if (!hasWinner()) {
            return Disc.None;
        }

        return isWinner(Disc.Yellow) ? Disc.Yellow : Disc.Red;
    }

    private boolean isFull() {
        for (int i = 0; i < grid.length; i++) {
            if (grid[i] == Disc.None) {
                return false;
            }
        }

        return true;
    }

    private boolean isWinner(Disc disc) {
        return hasHorizontalWin(disc) || hasVerticalWin(disc) || hasDiagonalWin(disc);
    }

    private boolean hasHorizontalWin(Disc disc) {
        for (int row = 0; row < ROWS; row++) {
            int connectedCount = 0;

            for (int column = 0; column < COLUMNS; column++) {
                if (getSlot(column, row) == disc) {
                    connectedCount++;

                    if (connectedCount == 4) {
                        return true;
                    }
                } else {
                    connectedCount = 0;
                }
            }
        }

        return false;
    }

    private boolean hasVerticalWin(Disc disc) {
        for (int column = 0; column < COLUMNS; column++) {
            int connectedCount = 0;

            for (int row = 0; row < ROWS; row++) {
                if (getSlot(column, row) == disc) {
                    connectedCount++;

                    if (connectedCount == 4) {
                        return true;
                    }
                } else {
                    connectedCount = 0;
                }
            }
        }

        return false;
    }

    private boolean hasDiagonalWin(Disc disc) {
        return checkDiagonalLeftToRight(disc) || checkDiagonalRightToLeft(disc);
    }

    /*
     * 3456XXX
     * 23456XX
     * 123456X
     * X123456
     * XX12345
     * XXX1234
     */
    private boolean checkDiagonalLeftToRight(Disc disc) {
        int scanLines = ROWS - 3 + COLUMNS - 4;

        for (int i = 0; i < scanLines; i++) {
            int column = 0;
            int row = ROWS - 4 - i;

            if (row < 0) {
                column = -row;
                row = 0;
            }

            if (checkDiagonalLeftToRight(disc, column, row)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDiagonalLeftToRight(Disc disc, int startColumn, int startRow) {
        int connectedCount = 0;
        int column = startColumn;
        int row = startRow;

        while (column < COLUMNS && row < ROWS) {
            if (getSlot(column, row) == disc) {
                connectedCount++;

                if (connectedCount == 4) {
                    return true;
                }
            } else {
                connectedCount = 0;
            }

            column++;
            row++;
        }

        return false;
    }

    /*
     * XXX6543
     * XX65432
     * X654321
     * 654321X
     * 54321XX
     * 4321XXX
     */
    private boolean checkDiagonalRightToLeft(Disc disc) {
        int scanLines = ROWS - 3 + COLUMNS - 4;

        for (int i = 0; i < scanLines; i++) {
            int column = COLUMNS - 1;
            int row = ROWS - 3 - i;

            if (row < 0) {
                column = COLUMNS - 1 + row;
                row = 0;
            }

            if (checkDiagonalRightToLeft(disc, column, row)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkDiagonalRightToLeft(Disc disc, int startColumn, int startRow) {
        int connectedCount = 0;
        int column = startColumn;
        int row = startRow;

        while (column >= 0 && row < ROWS) {
            if (getSlot(column, row) == disc) {
                connectedCount++;

                if (connectedCount == 4) {
                    return true;
                }
            } else {
                connectedCount = 0;
            }

            column--;
            row++;
        }

        return false;
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
