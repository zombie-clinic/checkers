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

import static org.springframework.http.ResponseEntity.ok;

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
        return ok(moveResponse);
    }

    @Override
    public ResponseEntity<MoveResponse> getCurrentState(String gameId) {
        if (gameService.isGameValid(gameId)) {
            return ok(moveService.getCurrentState(gameId));
        }
        throw new IllegalArgumentException(String.format("Game %s deleted or not started", gameId));
    }

    @Override
    public ResponseEntity<List<GameResponse>> getGamesByProgress(List<String> progressList) {
        if (progressList == null) {
            return ok(gameService.getGamesByStatus(Collections.emptyList()));
        }
        return ok(gameService.getGamesByStatus(progressList));
    }

    @Override
    public ResponseEntity<GameResponse> getGameById(String gameId) {
        GameResponse gameResponse = gameService.getGameById(gameId);
        if (gameResponse == null) {
            throw new IllegalArgumentException(String.format("Game %s not found", gameId));
        }
        return ok(gameResponse);
    }

    @Override
    public ResponseEntity<GameResponse> startGame() {
        GameResponse gameResponse = gameService.startGame();
        return ok(gameResponse);
    }
}
