package com.example.demo.api;

import com.example.demo.domain.*;
import com.example.demo.service.GameService;
import com.example.demo.service.MoveService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@CrossOrigin(origins = "http://localhost:8080")
@RequiredArgsConstructor
@RestController
public class GameApiController implements GameApi {

    private final GameService gameService;

    private final MoveService moveService;

    private final UserService userService;

    @Override
    public ResponseEntity<UserResponse> getUserById(Long userId) {
        UserResponse userById = userService.findUserById(userId);
        if (userById != null) {
            return ok(userById);
        }

        throw new IllegalArgumentException("No such user");
    }

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
    public ResponseEntity<GameResponse> startGame(StartGameRequest startGameRequest) {
        return ok(gameService.startGame(startGameRequest.getUserId()));
    }
}
