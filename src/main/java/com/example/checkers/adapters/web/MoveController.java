package com.example.checkers.adapters.web;

import static org.springframework.http.ResponseEntity.ok;

import com.example.checkers.api.MoveApi;
import com.example.checkers.core.exception.GameNotFoundException;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.MoveResponse;
import com.example.checkers.service.ValidationService;
import com.example.checkers.service.MoveService;
import com.example.checkers.service.PossibleMoveService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

/**
 * MoveController is responsible for handling incoming move requests. It also allows to check
 * for the next possible moves given the current state of the game (state inferred from already
 * persisted moves).
 */
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RequiredArgsConstructor
@RestController
public class MoveController implements MoveApi {

  private final ValidationService validationService;

  private final MoveService moveService;

  private final PossibleMoveService possibleMoveService;

  @Override
  public ResponseEntity<MoveResponse> getCurrentState(String gameUuid) {
    UUID gameId = UUID.fromString(gameUuid);
    if (validationService.isGameValid(gameId.toString())) {
      MoveResponse nextMoves = possibleMoveService.getNextMoves(gameId);
      return ok(nextMoves);
    }
    throw new IllegalArgumentException(String.format("Game %s deleted or not started", gameId));
  }

  @Override
  public ResponseEntity<MoveResponse> move(String gameUuid, MoveRequest moveRequest) {
    UUID gameId = UUID.fromString(gameUuid);
    validateMoveRequest(gameId, moveRequest);
    moveService.saveMove(gameId, moveRequest);
    MoveResponse nextMoves = possibleMoveService.getNextMoves(gameId);
    return ok(nextMoves);
  }

  private void validateMoveRequest(UUID gameId, MoveRequest moveRequest) {
    if (!validationService.existsAndActive(gameId)) {
      throw new GameNotFoundException(String.format("No active game found: %s", gameId));
    }
    if (!validationService.isGameInProgressConsistent(gameId, moveRequest)) {
      throw new IllegalArgumentException("Request is not consistent with the game.");
    }
    if (!moveRequest.getMove().matches("\\d{1,2}[\\-x]\\d{1,2}")) {
      throw new IllegalArgumentException("");
    }
    // todo check move, player, state etc. Add validators instead?
  }
}
