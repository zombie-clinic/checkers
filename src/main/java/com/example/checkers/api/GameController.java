package com.example.checkers.api;

import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.example.checkers.domain.GameProgress;
import com.example.checkers.model.GameResponse;
import com.example.checkers.model.JoinLobbyRequest;
import com.example.checkers.model.StartLobbyRequest;
import com.example.checkers.service.GameStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RequiredArgsConstructor
@RestController
public class GameController implements GameApi {

  private final GameStateService gameStateService;

  @Override
  public ResponseEntity<GameResponse> getGameById(String gameId) {
    GameResponse gameResponse = gameStateService.getGameById(gameId);
    if (gameResponse == null) {
      throw new IllegalArgumentException(String.format("Game %s not found", gameId));
    }
    return ok(gameResponse);
  }

  @Override
  public ResponseEntity<List<GameResponse>> getGamesByProgress(List<String> progress) {
    validateProgress(progress);
    return ok(gameStateService.getGamesByProgress(progress));
  }

  @Override
  public ResponseEntity<GameResponse> startLobby(StartLobbyRequest request, Boolean isImport) {
    String side = request.getSide();
    if (side == null) {
      throw new IllegalArgumentException("Starting player must choose a side.");
    }
    if (isImport != null && isImport) {
      return ok(gameStateService.startImportedGameLobby(request.getPlayerId(), side,
          request.getState()
      ));
    }
    return ok(gameStateService.startLobby(request.getPlayerId(), side));
  }

  @Override
  public ResponseEntity<GameResponse> joinLobby(JoinLobbyRequest request) {
    log.info("JoinLobbyRequest: {}", request);
    String gameId = request.getGameId();
    Long playerId = request.getPlayerId();
    gameStateService.lobbyExistsAndPlayerIsDifferent(gameId, playerId);
    return ok(gameStateService.startGame(gameId, playerId));
  }

  private void validateProgress(List<String> progress) {
    progress.forEach(GameProgress::valueOf);
  }
}
