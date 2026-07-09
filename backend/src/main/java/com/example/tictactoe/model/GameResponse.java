package com.example.tictactoe.model;

/**
 * Unified JSON response returned to the frontend after every API call.
 */
public class GameResponse {

    /** 3x3 board cells ("X", "O", or ""). */
    private String[][] board;

    /** Player who moves next ("X" or "O"). */
    private String currentPlayer;

    /** Whether the game has ended. */
    private boolean gameOver;

    /** Winning player, or null. */
    private String winner;

    /** Whether the game ended in a draw. */
    private boolean draw;

    /** Cumulative score for X. */
    private int scoreX;

    /** Cumulative score for O. */
    private int scoreO;

    /**
     * Winning cells for highlighting on the frontend.
     * Array of [row, col] pairs, or null.
     */
    private int[][] winningCells;

    /** Human-readable status message. */
    private String message;

    // ── Builder-style factory ──────────────────────────────────────────────

    public static GameResponse from(Board b, String message) {
        GameResponse r = new GameResponse();
        r.board = b.getCells();
        r.currentPlayer = b.getCurrentPlayer();
        r.gameOver = b.isGameOver();
        r.winner = b.getWinner();
        r.draw = b.isDraw();
        r.scoreX = b.getScoreX();
        r.scoreO = b.getScoreO();
        r.winningCells = b.getWinningCells();
        r.message = message;
        return r;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String[][] getBoard() { return board; }
    public void setBoard(String[][] board) { this.board = board; }

    public String getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    public boolean isDraw() { return draw; }
    public void setDraw(boolean draw) { this.draw = draw; }

    public int getScoreX() { return scoreX; }
    public void setScoreX(int scoreX) { this.scoreX = scoreX; }

    public int getScoreO() { return scoreO; }
    public void setScoreO(int scoreO) { this.scoreO = scoreO; }

    public int[][] getWinningCells() { return winningCells; }
    public void setWinningCells(int[][] winningCells) { this.winningCells = winningCells; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
