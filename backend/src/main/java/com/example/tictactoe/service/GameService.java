package com.example.tictactoe.service;

import com.example.tictactoe.exception.InvalidMoveException;
import com.example.tictactoe.model.Board;
import com.example.tictactoe.model.GameResponse;
import org.springframework.stereotype.Service;


@Service
public class GameService {

    private final Board board;

    public GameService() {
        this.board = new Board();
    }

    

    
    public GameResponse newGame() {
        board.setScoreX(0);
        board.setScoreO(0);
        board.initGame();
        return buildResponse("New game started. X goes first.");
    }

    
    public GameResponse reset() {
        board.initGame();
        return buildResponse("Game reset. X goes first.");
    }

    
    public GameResponse getStatus() {
        return buildResponse(statusMessage());
    }

    
    public GameResponse makeMove(int row, int col) {
        if (board.isGameOver()) {
            throw new InvalidMoveException("The game is already over. Please reset or start a new game.");
        }
        if (!board.getCells()[row][col].isEmpty()) {
            throw new InvalidMoveException("Cell (" + row + ", " + col + ") is already occupied.");
        }

        
        board.getCells()[row][col] = board.getCurrentPlayer();
        board.setTurns(board.getTurns() + 1);

        
        checkWinner();

        
        if (!board.isGameOver()) {
            String next = board.getCurrentPlayer().equals("X") ? "O" : "X";
            board.setCurrentPlayer(next);
        }

        return buildResponse(statusMessage());
    }

    

    
    private void checkWinner() {
        String[][] c = board.getCells();

        
        for (int r = 0; r < 3; r++) {
            if (!c[r][0].isEmpty()
                    && c[r][0].equals(c[r][1])
                    && c[r][1].equals(c[r][2])) {
                declareWinner(new int[][]{{r, 0}, {r, 1}, {r, 2}});
                return;
            }
        }

        
        for (int col = 0; col < 3; col++) {
            if (!c[0][col].isEmpty()
                    && c[0][col].equals(c[1][col])
                    && c[1][col].equals(c[2][col])) {
                declareWinner(new int[][]{{0, col}, {1, col}, {2, col}});
                return;
            }
        }

        
        if (!c[0][0].isEmpty()
                && c[0][0].equals(c[1][1])
                && c[1][1].equals(c[2][2])) {
            declareWinner(new int[][]{{0, 0}, {1, 1}, {2, 2}});
            return;
        }

        
        if (!c[0][2].isEmpty()
                && c[0][2].equals(c[1][1])
                && c[1][1].equals(c[2][0])) {
            declareWinner(new int[][]{{0, 2}, {1, 1}, {2, 0}});
            return;
        }

        
        if (board.getTurns() == 9) {
            board.setDraw(true);
            board.setGameOver(true);
        }
    }

    
    private void declareWinner(int[][] winningCells) {
        board.setWinner(board.getCurrentPlayer());
        board.setWinningCells(winningCells);
        board.setGameOver(true);

        
        if ("X".equals(board.getCurrentPlayer())) {
            board.setScoreX(board.getScoreX() + 1);
        } else {
            board.setScoreO(board.getScoreO() + 1);
        }
    }

    

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
