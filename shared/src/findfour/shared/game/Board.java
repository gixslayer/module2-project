package findfour.shared.game;

import findfour.shared.ArgumentOutOfRangeException;

/**
 * Represents a connect4 game board.
 * @author ciske
 *
 */
public class Board {
    /**
     * The number of columns the game board has.
     */
    public static final int COLUMNS = 7;
    /**
     * The number of rows the game board has.
     */
    public static final int ROWS = 6;

    private final Disc[] grid;

    /**
     * Creates a new board instance.
     */
    //@ ensures grid != null;
    public Board() {
        this.grid = new Disc[COLUMNS * ROWS];

        // Clear the board at creation so that there are never null references in the grid.
        clear();
    }

    /**
     * Returns the next free row of a column or ROWS - 1 if the row is full.
     * @param column The index of the column to get the next free slot of
     */
    /*@ pure */
    //@ requires column >= 0 && column < COLUMNS;
    //@ ensures \result >= 0 && \result <= ROWS - 1;
    public int getNextFreeSlot(int column) {
        if (column < 0 || column >= COLUMNS) {
            throw new ArgumentOutOfRangeException("column", 0, COLUMNS);
        }

        //@ loop_invariant 0 <= row && row <= ROWS;
        for (int row = 0; row < ROWS; row++) {
            if (getSlot(column, row) != Disc.None && getSlot(column, row) != null) {
                return row - 1;
            }
        }

        return ROWS - 1;
    }

    /**
     * Checks if a move is valid.
     * @param column The index of the column on which to make the move
     * @param disc The color of the disc
     */
    /*@ pure */
    //@ requires column >= 0 && column < COLUMNS;
    //@ requires disc != null;
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

    /**
     * Returns the current disc at a given column and row.
     * @param column The index of the column
     * @param row The index of the row
     */
    /*@ pure */
    //@ requires column >= 0 && column < COLUMNS;
    //@ requires row >= 0 && row < ROWS;
    //@ ensures \result == grid[row * COLUMNS + column];
    public Disc getSlot(int column, int row) {
        if (column < 0 || column >= COLUMNS) {
            throw new ArgumentOutOfRangeException("column", 0, COLUMNS);
        }
        if (row < 0 || row >= ROWS) {
            throw new ArgumentOutOfRangeException("row", 0, ROWS);
        }

        return grid[row * COLUMNS + column];
    }

    /**
     * Synchronized the board state to the given board state.
     * @param board The board state to synchronize to
     */
    //@ requires board != null;
    //@ ensures (\forall int i; 0 <= i & i < board.grid.length; grid[i] == board.grid[i]);
    public void syncTo(Board board) {
        //@ loop_invarient 0 <= i && i <= board.grid.length;
        for (int i = 0; i < board.grid.length; i++) {
            this.grid[i] = board.grid[i];
        }
    }

    /**
     * Clears the board to an empty state.
     */
    //@ ensures (\forall int i; 0 <= i & i < grid.length; grid[i] == Disc.None);
    void clear() {
        //@ loop_invariant 0 <= i && i <= grid.length;
        for (int i = 0; i < grid.length; i++) {
            grid[i] = Disc.None;
        }
    }

    /**
     * Makes a move on the current board.
     * @param column The index of the column on which to make the move
     * @param disc The disc to place
     */
    //@ requires disc != null && disc == Disc.Red || disc == Disc.Yellow;
    //@ requires isMoveValid(column, disc) == true;
    public void makeMove(int column, Disc disc) {
        // TODO: add check, fixed the bug that broke it :0
        //
        // before calling this method.
        //if (!isMoveValid(column, disc)) {
        //throw new ArgumentException("column", "invalid move");
        //}

        setSlot(column, getNextFreeSlot(column), disc);
    }

    /**
     * Returns whether the game is over, either due to a full board or a winner.
     */
    /*@ pure */
    //@ ensures \result == isFull() || hasWinner();
    boolean isGameOver() {
        return isFull() || hasWinner();
    }

    /**
     * Returns whether the game has a winner.
     */
    /*@ pure */
    //@ ensures \result == isWinner(Disc.Yellow) || isWinner(Disc.Red);
    public boolean hasWinner() {
        return isWinner(Disc.Yellow) || isWinner(Disc.Red);
    }

    /**
     * Returns the disc color of the winner, or Disc.None if no one has won yet.
     */
    /*@ pure */
    //@ ensures \result == Disc.None || isWinner(\result) == true;
    public Disc getWinner() {
        if (!hasWinner()) {
            return Disc.None;
        }

        return isWinner(Disc.Yellow) ? Disc.Yellow : Disc.Red;
    }

    /**
     * Returns whether the board is full with discs other than Disc.None.
     */
    /*@ pure */
    //@ ensures \result == !(\exists int i; 0 <= i && i < grid.length; grid[i] != Disc.None);  
    public boolean isFull() {
        //@ loop_invariant o <= i && i <= grid.length;
        for (int i = 0; i < grid.length; i++) {
            if (grid[i] == Disc.None) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the given disc color has won.
     * @param disc The color of the disc to check
     */
    /*@ pure */
    //@ requires disc != null && disc != Disc.None;
    //@ ensures \result == hasHorizontalWin(disc) || hasVerticalWin(disc) || hasDiagonalWin(disc);
    private boolean isWinner(Disc disc) {
        return hasHorizontalWin(disc) || hasVerticalWin(disc) || hasDiagonalWin(disc);
    }

    /**
     * Checks if the given color has won with a horizontal sequence.
     * @param disc The color of the disc to check
     */
    /*@ pure */
    //@ requires disc != null && disc != Disc.None;
    private boolean hasHorizontalWin(Disc disc) {
        //@ loop_invariant 0 <= row && row <= ROWS;
        for (int row = 0; row < ROWS; row++) {
            int connectedCount = 0;

            //@ loop_invariant 0 <= connectedCount && connectedCount <= 4;
            //@ loop_invariant 0 <= column && column <= COLUMNS;
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

    /**
     * Checks if the given color has won with a vertical sequence.
     * @param disc The color of the disc to check
     */
    /*@ pure */
    //@ requires disc != null && disc != Disc.None;
    private boolean hasVerticalWin(Disc disc) {
        //@ loop_invariant 0 <= column && column <= COLUMNS;
        for (int column = 0; column < COLUMNS; column++) {
            int connectedCount = 0;

            //@ loop_invariant 0 <= connectedCount && connectedCount <= 4;
            //@ loop_invariant 0 <= row && row <= ROWS;
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

    /**
     * Checks if the given color has won with a diagonal sequence.
     * @param disc The color of the disc to check
     */
    /*@ pure */
    //@ requires disc != null && disc != Disc.None;
    /*@ ensures 
        \result == checkDiagonalLeftToRight(disc) || \result == checkDiagonalRightToLeft(disc);
      @*/
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
    /**
     * Checks if the given color has won with a diagonal sequence from the top-left to the
     * bottom-right.
     * @param disc The color of the disc to check
     */
    /*@ pure */
    //@ requires disc != null && disc != Disc.None;
    private boolean checkDiagonalLeftToRight(Disc disc) {
        int scanLines = ROWS - 3 + COLUMNS - 4;

        //@ loop_invariant 0 <= i && i <= scanLines;
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

    /**
     * Checks if the given color has won with a diagonal sequence from the top-left to the
     * bottom-right starting at a given column and row.
     * @param disc The color of the disc to check
     * @param startColumn The column to start checking from
     * @param startRow The row to start checking from
     */
    /*@ pure */
    //@ requires disc != null && disc != Disc.None;
    //@ requires startColumn >= 0 && startColumn < COLUMNS;
    //@ requires startRow >= 0 && startRow < ROWS;
    private boolean checkDiagonalLeftToRight(Disc disc, int startColumn, int startRow) {
        int connectedCount = 0;
        int column = startColumn;
        int row = startRow;

        //@ loop_invariant 0 <= column && column <= COLUMNS;
        //@ loop_invariant 0 <= row && row <= ROWS;
        //@ loop_invariant 0 <= connectedCount && connectedCount <= 4;
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
    /**
     * Checks if the given color has won with a diagonal sequence from the top-right to the
     * bottom-left.
     * @param disc The color of the disc to check
     */
    /*@ pure */
    //@ requires disc != null && disc != Disc.None;
    private boolean checkDiagonalRightToLeft(Disc disc) {
        int scanLines = ROWS - 3 + COLUMNS - 4;

        //@ loop_invariant 0 <= i && i <= scanLines;
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

    /**
     * Checks if the given color has won with a diagonal sequence from the top-right to the
     * bottom-left starting at a given column and row.
     * @param disc The color of the disc to check
     * @param startColumn The column to start checking from
     * @param startRow The row to start checking from
     */
    /*@ pure */
    //@ requires disc != null && disc != Disc.None;
    //@ requires startColumn >= 0 && startColumn < COLUMNS;
    //@ requires startRow >= 0 && startRow < ROWS;
    private boolean checkDiagonalRightToLeft(Disc disc, int startColumn, int startRow) {
        int connectedCount = 0;
        int column = startColumn;
        int row = startRow;

        //@ loop_invariant 0 <= column && column <= COLUMNS;
        //@ loop_invariant 0 <= row && row <= ROWS;
        //@ loop_invariant 0 <= connectedCount && connectedCount <= 4;
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

    /**
     * Sets a slot to the specified disc.
     * @param column The index of the column
     * @param row The index of the row
     * @param disc The disc to place in the slot
     */
    //@ requires column >= 0 && column < COLUMS;
    //@ requires row >= 0 && row < ROWS;
    //@ requires disc != null;
    //@ ensures grid[row * COLUMNS + column] == disc;
    public void setSlot(int column, int row, Disc disc) {
        assert column >= 0 && column < COLUMNS;
        assert row >= 0 && row < ROWS;

        grid[row * COLUMNS + column] = disc;
    }

    /**
     * Checks if a column has a free slot.
     * @param column The column to check
     */
    /*@ pure */
    //@ requires column >= 0 && column < COLUMNS;
    //@ ensures \result == getSlot(column, 0) == Disc.None;
    private boolean hasFreeSlot(int column) {
        assert column >= 0 && column < COLUMNS;

        return getSlot(column, 0) == Disc.None;
    }
}
