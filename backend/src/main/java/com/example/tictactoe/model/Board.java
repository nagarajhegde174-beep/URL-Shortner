package com.example.tictactoe.model;


public class Board {

    
    private String[][] cells;

    
    private String currentPlayer;

    
    private int turns;

    
    private boolean gameOver;

    
    private String winner;

    
    private boolean draw;

    
    private int scoreX;

    
    private int scoreO;

    
    private int[][] winningCells;

    public Board() {
        this.cells = new String[3][3];
        this.scoreX = 0;
        this.scoreO = 0;
        initGame();
    }

    
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
