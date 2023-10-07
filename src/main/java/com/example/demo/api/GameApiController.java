package com.example.demo.api;

import com.example.demo.domain.GameResponse;
import com.example.demo.domain.MoveRequest;
import com.example.demo.service.GameService;
import com.example.demo.service.MoveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class GameApiController implements GameApi {

    private final GameService gameService;

    private final MoveService moveService;

    @SneakyThrows
    @Override
    public ResponseEntity<String> move(String gameId, MoveRequest moveRequest) {
        MoveResponse moveResponse = moveService.saveMove(
                gameId, moveRequest
        );
        ObjectMapper objectMapper = new ObjectMapper();
        return ResponseEntity.ok(objectMapper.writeValueAsString(moveResponse));
    }


    @Override
    public ResponseEntity<List<GameResponse>> getGamesByStatus(List<String> statusList) {
        if (statusList == null) {
            return ResponseEntity.ok(gameService.getGamesByStatus(Collections.emptyList()));
        }
        return ResponseEntity.ok(gameService.getGamesByStatus(statusList));
    }

    @Override
    public ResponseEntity<GameResponse> getGameById(String gameId) {
        GameResponse gameResponse = gameService.getGameById(gameId);
        if (gameResponse == null) {
            return null;
        }
        return ResponseEntity.ok(gameResponse);
    }

    @Override
    public ResponseEntity<GameResponse> startGame() {
        GameResponse gameResponse = gameService.startGame();
        return ResponseEntity.ok(gameResponse);
    }
}
