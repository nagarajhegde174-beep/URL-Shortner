package com.example.tictactoe.controller;

import com.example.tictactoe.model.GameResponse;
import com.example.tictactoe.model.MoveRequest;
import com.example.tictactoe.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    
    @GetMapping
    public ResponseEntity<GameResponse> getGame() {
        return ResponseEntity.ok(gameService.getStatus());
    }

    
    @GetMapping("/status")
    public ResponseEntity<GameResponse> getStatus() {
        return ResponseEntity.ok(gameService.getStatus());
    }

    
    @PostMapping("/new")
    public ResponseEntity<GameResponse> newGame() {
        return ResponseEntity.ok(gameService.newGame());
    }

    
    @PostMapping("/reset")
    public ResponseEntity<GameResponse> reset() {
        return ResponseEntity.ok(gameService.reset());
    }

    
    @PostMapping("/move")
    public ResponseEntity<GameResponse> move(@Valid @RequestBody MoveRequest request) {
        return ResponseEntity.ok(gameService.makeMove(request.getRow(), request.getCol()));
    }
}
