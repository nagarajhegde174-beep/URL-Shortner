package com.example.tictactoe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a player attempts an illegal move
 * (e.g., occupied cell or game already over).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidMoveException extends RuntimeException {

    public InvalidMoveException(String message) {
        super(message);
    }
}
