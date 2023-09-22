package com.example.demo.api;

import com.example.demo.CheckersService;
import com.example.demo.GameState;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class GameApiController implements GameApi {

    private final CheckersService checkersService;

    @Override
    public ResponseEntity<List<GameResponse>> getGamesByState(String state) {
        return ResponseEntity.ok(checkersService.getGamesByState(state));
    }

    @Override
    public ResponseEntity<GameResponse> getGameById(String gameId) {
        GameResponse gameResponse = checkersService.getGameById(gameId);
        if (gameResponse == null) {
            return null;
        }
        return ResponseEntity.ok(gameResponse);
    }

    @Override
    public ResponseEntity<String> startGame() {
        return ResponseEntity.ok(UUID.randomUUID().toString());
    }
}
