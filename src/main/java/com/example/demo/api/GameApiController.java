package com.example.demo.api;

import com.example.demo.domain.GameResponse;
import com.example.demo.domain.MoveRequest;
import com.example.demo.domain.MoveResponse;
import com.example.demo.service.GameService;
import com.example.demo.service.MoveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class GameApiController implements GameApi {

    private final GameService gameService;

    private final MoveService moveService;

    @Override
    public ResponseEntity<MoveResponse> move(String gameId, MoveRequest moveRequest) {
        MoveResponse moveResponse = moveService.saveMove(
                gameId, moveRequest
        );
        return ResponseEntity.ok(moveResponse);
    }

    @Override
    public ResponseEntity<List<GameResponse>> getGamesByProgress(List<String> progressList) {
        if (progressList == null) {
            return ResponseEntity.ok(gameService.getGamesByStatus(Collections.emptyList()));
        }
        return ResponseEntity.ok(gameService.getGamesByStatus(progressList));
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
