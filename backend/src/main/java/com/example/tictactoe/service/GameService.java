package com.example.tictactoe.service;

import com.example.tictactoe.exception.InvalidMoveException;
import com.example.tictactoe.model.Board;
import com.example.tictactoe.model.GameResponse;
import org.springframework.stereotype.Service;

/**
 * Core game logic — ported directly from the original TicTacToe.java Swing class.
 * All Swing/AWT code has been removed; pure domain logic remains.
 *
 * <p>The single {@link Board} instance is held in memory (no database required).
 */
@Service
public class GameService {

    private final Board board;

    public GameService() {
        this.board = new Board();
    }

    // ── Public API ─────────────────────────────────────────────────────────

    /** Start a completely new game and reset the scoreboard. */
    public GameResponse newGame() {
        board.setScoreX(0);
        board.setScoreO(0);
        board.initGame();
        return buildResponse("New game started. X goes first.");
    }

    /** Restart the current game while preserving the score. */
    public GameResponse reset() {
        board.initGame();
        return buildResponse("Game reset. X goes first.");
    }

    /** Return the current game state without mutating anything. */
    public GameResponse getStatus() {
        return buildResponse(statusMessage());
    }

    /**
     * Apply a player move at the given row/column.
     *
     * <p>Logic mirrors the original {@code ActionListener} in TicTacToe.java:
     * <ol>
     *   <li>Validate the move.</li>
     *   <li>Place the current player's mark.</li>
     *   <li>Check for a winner (rows → columns → diagonals).</li>
     *   <li>Check for a draw (turns == 9).</li>
     *   <li>Switch player if the game continues.</li>
     * </ol>
     */
    public GameResponse makeMove(int row, int col) {
        if (board.isGameOver()) {
            throw new InvalidMoveException("The game is already over. Please reset or start a new game.");
        }
        if (!board.getCells()[row][col].isEmpty()) {
            throw new InvalidMoveException("Cell (" + row + ", " + col + ") is already occupied.");
        }

        // Place the mark
        board.getCells()[row][col] = board.getCurrentPlayer();
        board.setTurns(board.getTurns() + 1);

        // Check outcome (ported from checkWinner())
        checkWinner();

        // Switch player only if the game continues
        if (!board.isGameOver()) {
            String next = board.getCurrentPlayer().equals("X") ? "O" : "X";
            board.setCurrentPlayer(next);
        }

        return buildResponse(statusMessage());
    }

    // ── Winner / draw detection (ported from TicTacToe.java) ──────────────

    /**
     * Checks all win conditions and draw in the same order as the original Swing code:
     * horizontal rows → vertical columns → main diagonal → anti-diagonal → draw.
     */
    private void checkWinner() {
        String[][] c = board.getCells();

        // Horizontal rows
        for (int r = 0; r < 3; r++) {
            if (!c[r][0].isEmpty()
                    && c[r][0].equals(c[r][1])
                    && c[r][1].equals(c[r][2])) {
                declareWinner(new int[][]{{r, 0}, {r, 1}, {r, 2}});
                return;
            }
        }

        // Vertical columns
        for (int col = 0; col < 3; col++) {
            if (!c[0][col].isEmpty()
                    && c[0][col].equals(c[1][col])
                    && c[1][col].equals(c[2][col])) {
                declareWinner(new int[][]{{0, col}, {1, col}, {2, col}});
                return;
            }
        }

        // Main diagonal (top-left → bottom-right)
        if (!c[0][0].isEmpty()
                && c[0][0].equals(c[1][1])
                && c[1][1].equals(c[2][2])) {
            declareWinner(new int[][]{{0, 0}, {1, 1}, {2, 2}});
            return;
        }

        // Anti-diagonal (top-right → bottom-left)
        if (!c[0][2].isEmpty()
                && c[0][2].equals(c[1][1])
                && c[1][1].equals(c[2][0])) {
            declareWinner(new int[][]{{0, 2}, {1, 1}, {2, 0}});
            return;
        }

        // Draw — all 9 cells filled with no winner
        if (board.getTurns() == 9) {
            board.setDraw(true);
            board.setGameOver(true);
        }
    }

    /** Marks the game as won and updates the cumulative score. */
    private void declareWinner(int[][] winningCells) {
        board.setWinner(board.getCurrentPlayer());
        board.setWinningCells(winningCells);
        board.setGameOver(true);

        // Update score (mirrors updateScore() in original code)
        if ("X".equals(board.getCurrentPlayer())) {
            board.setScoreX(board.getScoreX() + 1);
        } else {
            board.setScoreO(board.getScoreO() + 1);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String statusMessage() {
        if (board.getWinner() != null) {
            return board.getWinner() + " wins!";
        }
        if (board.isDraw()) {
            return "It's a draw!";
        }
        return board.getCurrentPlayer() + "'s turn";
    }

    private GameResponse buildResponse(String message) {
        return GameResponse.from(board, message);
    }
}
