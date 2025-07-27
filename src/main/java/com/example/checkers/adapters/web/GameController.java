package com.example.checkers.adapters.web;

import static org.springframework.http.ResponseEntity.ok;

import com.example.checkers.api.GameApi;
import com.example.checkers.core.GameId;
import com.example.checkers.core.GameProgress;
import com.example.checkers.core.PlayerId;
import com.example.checkers.core.Side;
import com.example.checkers.core.State;
import com.example.checkers.core.exception.GameNotFoundException;
import com.example.checkers.model.GameResponse;
import com.example.checkers.model.JoinLobbyRequest;
import com.example.checkers.model.MoveResponse;
import com.example.checkers.model.StartLobbyRequest;
import com.example.checkers.service.PossibleMoveService;
import com.example.checkers.service.QueryService;
import com.example.checkers.service.ValidationService;
import com.example.checkers.usecase.UpdateGameLifeCycleUseCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RequiredArgsConstructor
@RestController
class GameController implements GameApi {

  private final QueryService queryService;

  private final ValidationService validationService;

  private final UpdateGameLifeCycleUseCase useCase;

  private final PossibleMoveService possibleMoveService;

  @Override
  public ResponseEntity<GameResponse> getGameById(String gameId) {
    GameResponse gameResponse = queryService.getGameById(gameId);
    if (gameResponse == null) {
      throw new GameNotFoundException(String.format("Game not found: %s", gameId));
    }
    return ok(gameResponse);
  }

  @Override
  public ResponseEntity<List<GameResponse>> getGamesByProgress(List<String> progress) {
    validateProgress(progress);
    return ok(queryService.getGamesByProgress(progress));
  }

  @Override
  public ResponseEntity<GameResponse> startLobby(StartLobbyRequest request, Boolean isImport) {
    String side = request.getSide();

    if (side == null) {
      throw new IllegalArgumentException("Starting player must choose a side.");
    }

    if (isImport != null && isImport) {
      return ok(createImportedGameLobby(request));
    }

    return ok(createNewGameLobby(request));
  }

  @Override
  public ResponseEntity<GameResponse> joinLobby(JoinLobbyRequest request) {
    log.info("JoinLobbyRequest: {}", request);
    String gameId = request.getGameId();
    Long playerId = request.getPlayerId();
    validationService.lobbyExistsAndPlayerIsDifferent(gameId, playerId);
    useCase.startGame(GameId.of(gameId), PlayerId.of(playerId));

    // TODO Food for thought - possible moves are not related to game and persistence and only
    //  are being passed to frontend.
    //  What if it should be fetched on frontend on demand?
    //  Or maybe it should be enriched elsewhere?
    MoveResponse nextMoves = possibleMoveService.getNextMoves(UUID.fromString(gameId));

    GameResponse game = queryService.getGameById(gameId);

    game.setPossibleMoves(nextMoves);
    return ok(game);
  }

  private GameResponse createNewGameLobby(StartLobbyRequest request) {
    GameId gameId = useCase.createNewGameLobby(PlayerId.of(request.getPlayerId()),
        Side.valueOf(request.getSide()));

    MoveResponse nextMoves = possibleMoveService.getNextMoves(gameId.value());

    GameResponse gameResponse = queryService.getGameById(gameId.value().toString());
    gameResponse.setPossibleMoves(nextMoves);
    return gameResponse;
  }

  private GameResponse createImportedGameLobby(StartLobbyRequest request) {
    GameId gameId = useCase.createImportedGameLobby(PlayerId.of(request.getPlayerId()),
        Side.valueOf(request.getSide()),
        State.from(request.getClientState()));

    MoveResponse nextMoves = possibleMoveService.getNextMoves(gameId.value());

    GameResponse gameResponse = queryService.getGameById(String.valueOf(gameId.value()));
    gameResponse.setPossibleMoves(nextMoves);
    return gameResponse;
  }

  private void validateProgress(List<String> progress) {
    progress.forEach(GameProgress::valueOf);
  }
}
