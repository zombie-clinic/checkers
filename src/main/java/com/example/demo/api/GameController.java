package com.example.demo.api;

import com.example.demo.model.GameResponse;
import com.example.demo.model.StartGameRequest;
import com.example.demo.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@RestController
public class GameController implements GameApi {

    private final GameService gameService;

    @Override
    public ResponseEntity<GameResponse> getGameById(String gameId) {
        GameResponse gameResponse = gameService.getGameById(gameId);
        if (gameResponse == null) {
            throw new IllegalArgumentException(String.format("Game %s not found", gameId));
        }
        return ok(gameResponse);
    }

    // TODO Better to separate getAll and getByProgress in order to be able to return
    //  an error when filters fail, instead of all games
    @Override
    public ResponseEntity<List<GameResponse>> getGamesByProgress(List<String> progress) {
        if (progress == null) {
            return ok(gameService.getGamesByProgress(Collections.emptyList()));
        }
        return ok(gameService.getGamesByProgress(progress));
    }

    @Override
    public ResponseEntity<GameResponse> startGame(StartGameRequest startGameRequest) {
        return ok(gameService.startGame(startGameRequest.getPlayerId()));
    }
}
