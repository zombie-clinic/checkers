package com.example.demo.api;

import com.example.demo.service.GameService;
import com.example.demo.service.MoveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class GameApiController implements GameApi {

    private final GameService gameService;

    private final MoveService moveService;


    @Override
    public ResponseEntity<String> move(String gameId, MoveRequest moveRequest) {
        MoveResponse moveResponse = moveService.saveMove(
                gameId, moveRequest
        );
        return ResponseEntity.ok(moveResponse.toString());
    }

    @Override
    public ResponseEntity<List<GameResponse>> getGamesByState(String state) {
        return ResponseEntity.ok(gameService.getGamesByStatus(state));
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
    public ResponseEntity<String> startGame() {
        GameResponse gameResponse = gameService.startGame();
        return ResponseEntity.ok(gameResponse.toString());
    }
}
