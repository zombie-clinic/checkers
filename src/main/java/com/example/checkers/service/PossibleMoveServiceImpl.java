package com.example.checkers.service;

import static com.example.checkers.service.StateUtils.getStateFromMoveList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.MoveRecord;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.PossibleMoveSimplified;
import com.example.checkers.domain.Side;
import com.example.checkers.model.MoveResponse;
import com.example.checkers.model.State;
import lombok.RequiredArgsConstructor;


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
      return new MoveResponse(gameId.toString(),
          getStateFromMoveList(moveList.stream().toList()),
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

    State state;
    if (moveList.isEmpty()) {
      // fixme seems to be unreachable code
      state = startingStateLookupService.getStateFromStartingStateString(UUID.fromString(gameId));
      state.setKings(List.of());
    } else {
      state = getStateFromMoveList(moveList);
      MoveRecord last = moveList.getLast();
      if (last == null) {
        state.setKings(List.of());
      } else {
        if (last.kings().isEmpty()) {
          state.setKings(List.of());
        } else {
          state.setKings(new ArrayList<>(last.kings()));
        }
      }
    }
    // regular move, game in progress
    Map<Integer, List<PossibleMove>> possibleMoves = possibleMoveProvider.getPossibleMovesForSide(
        nextToMoveSide, Checkerboard.state(state.getDark(), state.getLight())
    );
    Map<Integer, List<PossibleMoveSimplified>> simplifiedPossibleMoves =
        getSimplifiedPossibleMoves(possibleMoves);

    Map<Integer, List<PossibleMoveSimplified>> res = new HashMap<>();

    for (Map.Entry<Integer, List<PossibleMoveSimplified>> e :
        simplifiedPossibleMoves.entrySet()) {
      if (e.getValue().stream().anyMatch(PossibleMoveSimplified::isCapture)) {
        res.put(e.getKey(), e.getValue());
      }
    }

    if (res.isEmpty()) {
      // no captures, regular move
      return new MoveResponse(gameId, state, nextToMoveSide.name(),
          simplifiedPossibleMoves);
    }

    // Only captures below
    Side currentSide = Side.valueOf(nextToMoveSide.name());
    Side lastMoveSide = moveList.getLast().side();

    if (currentSide == lastMoveSide) {
      Integer lastMoveCellDest = Integer.valueOf(moveList.getLast().move().split("x")[1]);
      Map<Integer, List<PossibleMoveSimplified>> resFilteredForChainCaptures =
          new HashMap<>();
      resFilteredForChainCaptures.put(lastMoveCellDest, res.get(lastMoveCellDest));

      return new MoveResponse(gameId, state, currentSide.name(),

          resFilteredForChainCaptures.entrySet().stream()
              .filter(e -> {
                var list = e.getValue();
                return list.stream().anyMatch(
                    PossibleMoveSimplified::isCapture);
              }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    return new MoveResponse(gameId, state, currentSide.name(), res);
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
