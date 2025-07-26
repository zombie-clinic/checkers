package com.example.checkers.service;

import static com.example.checkers.service.StateUtils.getStateFromMoveList;

import com.example.checkers.core.MoveRecord;
import com.example.checkers.core.PossibleMove;
import com.example.checkers.core.PossibleMoveSimplified;
import com.example.checkers.core.Side;
import com.example.checkers.core.State;
import com.example.checkers.model.MoveResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PossibleMoveServiceImpl implements PossibleMoveService {

  private final MovesReaderService movesReaderService;

  private final TurnService turnService;

  private final StartingStateLookupService startingStateLookupService;

  private final PossibleMoveProvider possibleMoveProvider;

  // TODO do we need side? Depends on if we are fetching from db
  @Override
  @Transactional
  public MoveResponse getNextMoves(UUID gameId) {
    var moveList = movesReaderService.getMovesFor(gameId.toString());

    var nextToMoveSide = turnService.getWhichSideToMove(gameId.toString());
    if (nextToMoveSide == null) {
      // meaning last player to capture a piece wins
      // we return empty possible moves and null next turn
      // in order front end to recognize end time
      return new MoveResponse(
          gameId.toString(),
          State.toServerState(getStateFromMoveList(moveList.stream().toList())),
          null,
          Map.of()
      );
    }
    // log.info("Next move: {}", nextToMoveSide);
    return generateMoveResponse(gameId.toString(), nextToMoveSide);
  }


  // TODO Add a distinction between Player and User
  // TODO User becomes Player when a game starts, Player has a user id and a side
  private MoveResponse generateMoveResponse(String gameId, Side nextToMoveSide) {
    var moveList = movesReaderService.getMovesFor(gameId);

    State initialState;

    if (moveList.isEmpty()) {
      initialState =
          startingStateLookupService.getStateFromStartingStateString(UUID.fromString(gameId));
      return calculateMoveResponse(gameId, nextToMoveSide, initialState);
    } else {
      initialState = getStateFromMoveList(moveList);
    }

    State resultState;

    MoveRecord last = moveList.getLast();
    if (last == null) {
      resultState = new State(initialState.dark(), initialState.light(), Set.of());
    } else {
      if (last.kings().isEmpty()) {
        resultState = new State(initialState.dark(), initialState.light(), Set.of());
      } else {
        resultState =
            new State(initialState.dark(), initialState.light(), new HashSet<>(last.kings()));
      }
    }

    return calculateMoveResponse(gameId, nextToMoveSide, resultState);
  }

  private MoveResponse calculateMoveResponse(String gameId, Side nextToMoveSide, State state) {
    Map<Integer, List<PossibleMove>> possibleMoves = possibleMoveProvider.getPossibleMovesForSide(
        nextToMoveSide, state);
    Map<Integer, List<PossibleMoveSimplified>> simplifiedPossibleMoves =
        getSimplifiedPossibleMoves(possibleMoves);

    Map<Integer, List<PossibleMoveSimplified>> res = new HashMap<>();

    for (Map.Entry<Integer, List<PossibleMoveSimplified>> e :
        simplifiedPossibleMoves.entrySet()) {
      if (e.getValue().stream().anyMatch(PossibleMoveSimplified::isCapture)) {
        res.put(e.getKey(), e.getValue());
      }
    }

    return new MoveResponse(gameId, State.toServerState(state), nextToMoveSide.name(),
        simplifiedPossibleMoves);
  }

  private Map<Integer, List<PossibleMoveSimplified>> getSimplifiedPossibleMoves(Map<Integer,
      List<PossibleMove>> moves) {
    Map<Integer, List<PossibleMoveSimplified>> map = new HashMap<>();
    for (Map.Entry<Integer, List<PossibleMove>> entry : moves.entrySet()) {
      map.put(entry.getKey(), entry.getValue().stream()
          .map(PossibleMoveSimplified::fromMove).toList());
    }
    return map;
  }
}
