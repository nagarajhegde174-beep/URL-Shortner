package com.example.tictactoe.controller;

import com.example.tictactoe.model.GameResponse;
import com.example.tictactoe.model.MoveRequest;
import com.example.tictactoe.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing the Tic-Tac-Toe game API.
 * All endpoints return {@link GameResponse} JSON.
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * GET /api/game
     * Returns the current board state.
     */
    @GetMapping
    public ResponseEntity<GameResponse> getGame() {
        return ResponseEntity.ok(gameService.getStatus());
    }

    /**
     * GET /api/game/status
     * Alias for /api/game — returns the current game status.
     */
    @GetMapping("/status")
    public ResponseEntity<GameResponse> getStatus() {
        return ResponseEntity.ok(gameService.getStatus());
    }

    /**
     * POST /api/game/new
     * Starts a fresh game and resets the scoreboard.
     */
    @PostMapping("/new")
    public ResponseEntity<GameResponse> newGame() {
        return ResponseEntity.ok(gameService.newGame());
    }

    /**
     * POST /api/game/reset
     * Resets the current game while keeping scores.
     */
    @PostMapping("/reset")
    public ResponseEntity<GameResponse> reset() {
        return ResponseEntity.ok(gameService.reset());
    }

    /**
     * POST /api/game/move
     * Makes a move for the current player.
     * Body: { "row": 0-2, "col": 0-2 }
     */
    @PostMapping("/move")
    public ResponseEntity<GameResponse> move(@Valid @RequestBody MoveRequest request) {
        return ResponseEntity.ok(gameService.makeMove(request.getRow(), request.getCol()));
    }
}
