package com.example.checkers.service;

import static com.example.checkers.service.StateUtils.getStateFromMoveList;

import com.example.checkers.core.MoveRecord;
import com.example.checkers.core.Piece;
import com.example.checkers.core.PossibleMove;
import com.example.checkers.core.Side;
import com.example.checkers.core.State;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * This component defines which side is to move next at any given time of the game.
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class TurnService {

  private final MovesReaderService movesReaderService;

  private final PossibleMoveProvider provider;

  /**
   * The implementation logic goes as follows.
   *
   * <p>If there are no moves -> LIGHT to move first.
   *
   * <p>If there are moves and the last is capture -> infer based on capture result.
   *
   * <p>If there moves and the last one is no capture -> pass turn to opposite side.
   *
   * @param gameId UUID of an ongoing game
   * @return LIGHT or DARK
   */
  public Side getWhichSideToMove(String gameId) {
    List<MoveRecord> moves = movesReaderService.getMovesFor(gameId);
    Optional<Long> lastMoveId = moves.stream()
        .map(MoveRecord::moveId)
        .max(Comparator.naturalOrder());
    if (lastMoveId.isEmpty()) {
      return Side.LIGHT;
    }

    MoveRecord lastMove = moves.getLast();

    // TODO add isCapture method
    if (lastMove.move().contains("x")) {
      // Let's check if it's chaining capturing move
      // It is only when the next move with the same piece leads to capture
      // this means we need to check only for that piece moves
      State currentState = getStateFromMoveList(moves);

      if (currentState.getDark().isEmpty() || currentState.getLight().isEmpty()) {
        return null;
      }

      var piece = Piece.of(Integer.parseInt(lastMove.move().split("x")[1]), lastMove.side());
      var possibleMovesMap = provider.getPossibleMovesForPiece(piece, currentState);

      List<PossibleMove> filtered = possibleMovesMap.entrySet().stream()
          .flatMap(p -> p.getValue().stream())
          .filter(PossibleMove::isCapture)
          .toList();

      if (!filtered.isEmpty()) {
        return lastMove.side();
      }
    }

    return lastMove.side() == Side.LIGHT ? Side.DARK : Side.LIGHT;
  }
}
