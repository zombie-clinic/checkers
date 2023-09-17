package com.example.demo.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class GameApiController implements GameApi {

    @Override
    public ResponseEntity<GameResponse> getGameById(UUID gameId) {
        GameResponse body = new GameResponse();
        body.setId(UUID.randomUUID());
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<String> startGame() {
        return ResponseEntity.ok(UUID.randomUUID().toString());
    }
}
