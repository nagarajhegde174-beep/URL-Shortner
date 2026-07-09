package com.example.tictactoe.model;

/**
 * Represents the Tic-Tac-Toe board state held in memory.
 * Core logic ported from the original TicTacToe.java (Swing version),
 * stripped of all GUI concerns.
 */
public class Board {

    /** 3x3 grid. Empty cell = "". */
    private String[][] cells;

    /** Current player: "X" or "O". X always starts. */
    private String currentPlayer;

    /** Number of moves made in the current game. */
    private int turns;

    /** Whether the current game has ended. */
    private boolean gameOver;

    /** Winning player, null if no winner yet. */
    private String winner;

    /** Whether the current game ended in a draw. */
    private boolean draw;

    /** Cumulative score for X across restarts. */
    private int scoreX;

    /** Cumulative score for O across restarts. */
    private int scoreO;

    /**
     * Winning cell coordinates [row][col] for highlighting.
     * Null when there is no winner.
     */
    private int[][] winningCells;

    public Board() {
        this.cells = new String[3][3];
        this.scoreX = 0;
        this.scoreO = 0;
        initGame();
    }

    /** Resets per-game state without clearing the score. */
    public void initGame() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                cells[r][c] = "";
            }
        }
        currentPlayer = "X";
        turns = 0;
        gameOver = false;
        winner = null;
        draw = false;
        winningCells = null;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String[][] getCells() { return cells; }
    public void setCells(String[][] cells) { this.cells = cells; }

    public String getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }

    public int getTurns() { return turns; }
    public void setTurns(int turns) { this.turns = turns; }

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
}
