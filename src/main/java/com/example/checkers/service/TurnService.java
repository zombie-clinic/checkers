package com.example.checkers.service;

import static com.example.checkers.service.StateUtils.getStateFromMoveList;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.MoveRecord;
import com.example.checkers.domain.Piece;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.Side;
import com.example.checkers.model.State;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TurnService {

  private final MovesReaderService movesReaderService;

  private final PossibleMoveProvider provider;

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
