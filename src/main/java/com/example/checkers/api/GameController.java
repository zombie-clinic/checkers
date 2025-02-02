package com.example.checkers.api;

import com.example.checkers.domain.GameProgress;
import com.example.checkers.model.GameResponse;
import com.example.checkers.model.JoinLobbyRequest;
import com.example.checkers.model.StartLobbyRequest;
import com.example.checkers.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
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

    @Override
    public ResponseEntity<List<GameResponse>> getGamesByProgress(List<String> progress) {
        validateProgress(progress);
        return ok(gameService.getGamesByProgress(progress));
    }

    private void validateProgress(List<String> progress) {
        progress.forEach(GameProgress::valueOf);
    }

    @Override
    public ResponseEntity<GameResponse> startLobby(StartLobbyRequest request) {
        String side = request.getSide();
        if (side == null) {
            throw new IllegalArgumentException("Starting player must choose a side.");
        }
        return ok(gameService.startLobby(request.getPlayerId(), side));
    }

    @Override
    public ResponseEntity<GameResponse> joinLobby(JoinLobbyRequest request) {
        // validation
        String gameId = request.getGameId();
        Long playerId = request.getPlayerId();
        gameService.lobbyExistsAndPlayerIsDifferent(gameId, playerId);
        return ok(gameService.startGame(gameId, playerId));
    }
}
