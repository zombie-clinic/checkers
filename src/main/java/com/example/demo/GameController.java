package com.example.demo;

import com.example.demo.api.GameApi;
import com.example.demo.domain.GameResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class GameController implements GameApi {

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

    @GetMapping("/")
    @ResponseBody
    public ResponseEntity<?> index() {
        Map<String, String> response = new HashMap<>();
        response.put("choice1", "white");
        response.put("choice2", "black");
        response.put("choice3", "random");
        response.put("button", "start");
        return ResponseEntity.ok(response);
    }
}
