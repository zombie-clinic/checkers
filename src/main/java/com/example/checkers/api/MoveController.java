package com.example.checkers.api;

import com.example.checkers.domain.Side;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.MoveResponse;
import com.example.checkers.service.GameService;
import com.example.checkers.service.MoveService;
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
            return ok(moveService.generateMoveResponse(gameId, Side.DARK));
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
