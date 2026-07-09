package com.example.tictactoe.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Payload sent by the client when making a move.
 */
public class MoveRequest {

    @Min(value = 0, message = "Row must be between 0 and 2")
    @Max(value = 2, message = "Row must be between 0 and 2")
    private int row;

    @Min(value = 0, message = "Column must be between 0 and 2")
    @Max(value = 2, message = "Column must be between 0 and 2")
    private int col;

    public MoveRequest() {}

    public MoveRequest(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }

    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }
}
