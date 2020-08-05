package org.chelak.gomoku;

import android.util.Pair;

import java.io.Serializable;

/**
 * Created by sergey.chelak on 07.04.15.
 */
public class GomokuLogic implements Serializable {

    public static class WinLineItem extends Pair<Integer, Integer> {
        /**
         * Constructor for a Pair.
         *
         * @param first  the first object in the Pair
         * @param second the second object in the pair
         */
        public WinLineItem(Integer first, Integer second) {
            super(first, second);
        }
    }

    private enum Win {
        NULL,
        HORIZONTAL,
        DOWN_LEFT,
        DOWN_RIGHT,
        VERTICAL
    }

    public interface Delegate {
        void onGameOver(WinState winState, WinLineItem[] winLine);
        void onMoveComplete(int x, int y);
    }

    private transient Delegate delegate;

    // Size of the board
    private int boardSize;

    // Importance of attack (1..16)
    private final static int ATTACK_FACTOR = 8;

    // Value of having 0, 1,2,3,4 or 5 pieces in line
    private final static int[] WEIGHT = {0, 0, 4, 20, 100, 500, 0};

    public final static int NONE = 100;
    public final static int CROSS = 0;
    public final static int NOUGHT = 1;

    protected int[][] board;
    protected int player;         // The player whose move is next
    protected int totalLines;     // The number of empty lines left
    protected boolean isWon;      // Set if one of the players has won
    protected int[][][][] line;   // Number of pieces in each of all possible lines
    protected int[][][] value;    // Value of each square for each player


    public GomokuLogic(int boardSize) {
        this.boardSize = boardSize;
        board = new int[boardSize][boardSize];
        value = new int[boardSize][boardSize][2];
        line = new int[4][boardSize][boardSize][2];
        resetGame();
    }

    public void resetGame() {
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {      // Clear tables
                board[i][j] = NONE;
                for (int c = 0; c < 2; ++c) {
                    value[i][j][c] = 0;
                    for (int d = 0; d < 4; ++d) {
                        line[d][i][j][c] = 0;
                    }
                }
            }
        }
        player = CROSS;      // cross starts
        // Total number of lines
        totalLines = 2 * 2 * (boardSize * (boardSize - 4) + (boardSize - 4) * (boardSize - 4));
        isWon = false;
    }

    public boolean isGameOver() {
        return isWon || totalLines <= 0;
    }

    public int getPlayer() {
        return player;
    }


    private int getOpponent() {
        return player == CROSS ? NOUGHT : CROSS;
    }

    // Finds a move X,Y for player, simply by picking the one with the highest value
    private int[] findMove() {
        int opponent = getOpponent();
        int max = Integer.MIN_VALUE;
        // If no square has a high value then pick the one in the middle
        int x = (1 + boardSize) / 2;
        int y = (1 + boardSize) / 2;
        if ( board[x][y] == NONE ) {
            max = 4;
        }
        // The evaluation for a square is simply the value of the square
        // for the player (attack points) plus the value for the opponent
        // (defense points). Attack is more important than defense, since
        // it is better to get 5 in line yourself than to prevent the
        // opponent from getting it.
        for ( int i = 0; i < boardSize; ++i ) {        // For all empty squares
            for ( int j = 0; j < boardSize; ++j ) {
                if ( board[i][j] == NONE ) {
                    int evaluation = value[i][j][player] * (16 + ATTACK_FACTOR) / 16 +
                            value[i][j][opponent] + (int) (Math.random() * 4);
                    if ( evaluation > max ) {
                        max = evaluation;
                        x = i;
                        y = j;
                    }
                }
            }
        }
        return new int[]{x,y};
    }


    private boolean isValidRange(int x, int rate) {
        int val = x - 4*rate;
        int min = Math.min(x, val);
        int max = Math.max(x, val);
        return ( min >= 0 ) && ( max < boardSize);
    }


    private Win calculateWinLine(int x, int xRate, int y, int yRate, int position, Win winDirection, Win winningLine) {
        Win result = winningLine;
        int opponent = getOpponent();
        for (int k = 0; k < 5; ++k ) {
            int x1 = x + xRate*k;
            int y1 = y + yRate*k;
            if ( isValidRange(x1, xRate) && isValidRange(y1, yRate) ) {
                int[] ln = line[position][x1][y1];
                ln[player]++;
                int val = ln[player];
                if ( val == 1 ) {
                    totalLines--;
                } else if ( val == 5 ) {
                    isWon = true;
                }
                if ( isWon && winningLine == Win.NULL ) {
                    result = winDirection;
                }

                for ( int l = 0; l < 5; ++l ) {
                    // Updates the value of a square for each player, taking into
                    // account that player has placed an extra piece in the square.
                    // The value of a square in a usable line is Weight[Lin[Player]+1]
                    // where Lin[Player] is the number of pieces already placed
                    // in the line
                    int vx = x1 - xRate*l;
                    int vy = y1 - yRate*l;
                    if ( ln[opponent] == 0 ) {
                        // If the opponent has no pieces in the line, then simply
                        // update the value for player
                        value[vx][vy][player] += WEIGHT[ln[player]+1] - WEIGHT[ln[player]];
                    } else if ( ln[player] == 1 ) {
                        // If it is the first piece in the line, then the line is
                        // spoiled for the opponent
                        value[vx][vy][opponent] -= WEIGHT[ln[opponent]+1];
                    }
                }
            }
        }
        return result;
    }


    // Performs the move X,Y for player, and updates the global variables
    // (Board, Line, Value, Player, GameWon, TotalLines and the screen)
    private void makeMove(int x, int y) {
        Win winningLine = Win.NULL;
        //isWon = false;

        // Each square of the board is part of 20 different lines.
        // The procedure adds one to the number of pieces in each
        // of these lines. Then it updates the value for each of the 5
        // squares in each of the 20 lines. Finally Board is updated, and
        // the move is printed on the screen.
        winningLine = calculateWinLine(x, -1, y,  0, 0, Win.HORIZONTAL, winningLine);
        winningLine = calculateWinLine(x, -1, y, -1, 1, Win.DOWN_LEFT, winningLine);
        winningLine = calculateWinLine(x,  1, y, -1, 3, Win.DOWN_RIGHT, winningLine);
        winningLine = calculateWinLine(x,  0, y, -1, 2, Win.VERTICAL, winningLine);

        board[x][y] = player;
        if ( isGameOver() ) {
            WinLineItem[] highlightLine = null;
            WinState winState = player == CROSS ? WinState.CROSS_WIN : WinState.NOUGHT_WIN;
            if ( isWon ) {
                highlightLine = getWinningLine(x, y, winningLine);
            } else {
                winState = WinState.DRAW;
            }
            //if ( delegate != null )
                delegate.onGameOver(winState, highlightLine);
        } else {
            player = getOpponent();
            //if ( delegate != null )
                delegate.onMoveComplete(x, y);
        }
    }


    private WinLineItem[] getWinningLine(int x, int y, Win winDirection) {
        int dx = 0, dy = 0;
        switch ( winDirection ) {
            case HORIZONTAL:
                dx = 1;
                dy = 0;
                break;
            case DOWN_LEFT:
                dx = 1;
                dy = 1;
                break;
            case VERTICAL:
                dx = 0;
                dy = 1;
                break;
            case DOWN_RIGHT:
                dx = -1;
                dy = 1;
                break;
        }
        while ( x >= 0 && x < boardSize &&
                y >= 0 && y < boardSize &&
                board[x][y] == player ) {
            x += dx;
            y += dy;
        }
        WinLineItem[] winCells = new WinLineItem[5];
        for ( int i = 0; i < 5; ++i) {
            x-=dx;
            y-=dy;
            winCells[i] = new WinLineItem(x, y);
        }
        return winCells;
    }

    public void playerMove(int x, int y) {
        if ( board[x][y] == NONE ) {
            makeMove(x, y);
        }
    }

    public void programMove() {
        int[] move = findMove();
        makeMove(move[0], move[1]);
    }

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public int[][] getBoard() {
        return board;
    }
}
