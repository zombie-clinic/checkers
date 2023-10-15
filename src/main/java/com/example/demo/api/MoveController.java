package com.example.demo.api;

import com.example.demo.model.MoveRequest;
import com.example.demo.model.MoveResponse;
import com.example.demo.service.GameService;
import com.example.demo.service.MoveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@RestController
public class MoveController implements MoveApi {

    private final GameService gameService;

    private final MoveService moveService;

    @Override
    public ResponseEntity<MoveResponse> getCurrentState(String gameId) {
        if (gameService.isGameValid(gameId)) {
            return ok(moveService.getCurrentState(gameId));
        }
        throw new IllegalArgumentException(String.format("Game %s deleted or not started", gameId));
    }

    @Override
    public ResponseEntity<MoveResponse> move(String gameId, MoveRequest moveRequest) {
        MoveResponse moveResponse = moveService.saveMove(
                gameId, moveRequest
        );
        return ok(moveResponse);
    }
}
