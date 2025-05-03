package com.example.checkers.service;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.Piece;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.Side;
import com.example.checkers.model.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PossibleMoveProviderImpl implements PossibleMoveProvider {

  @Override
  public Map<Integer, List<PossibleMove>> getPossibleMovesForPiece(Piece piece, State state) {
    return Map.of(piece.position(), getPossibleMovesForPieceInternal(piece, state));
  }

  @Override
  public Map<Integer, List<PossibleMove>> getPossibleMovesForSide(Side side, State state) {
    var map = new HashMap<Integer, List<PossibleMove>>();
    for (int i : StateUtils.getSide(side, state)) {
      var possibleMoves = getPossibleMovesForPieceInternal(Piece.of(i, side), state);
      if (!possibleMoves.isEmpty()) {
        map.put(i, possibleMoves);
      }
    }
    return map;
  }

  List<PossibleMove> getPossibleMovesForPieceInternal(Piece piece,
                                                      State state,
                                                      boolean isChainCaptureCheck) {

    var moves = new ArrayList<PossibleMove>();
    for (LinkedList<Integer> diagonal : Checkerboard.getDiagonals()) {
      moves.addAll(getMoves(state, piece, diagonal));
    }

    if (moves.stream().anyMatch(PossibleMove::isCapture)) {
      return captureMovesVerifiedForTerminality(
          state,
          moves.stream().filter(PossibleMove::isCapture).toList(),
          isChainCaptureCheck
      );
    }

    return moves.stream()
        .filter(move -> StateUtils.isEmptyCell(move.destination(), state))
        .toList();
  }

  List<PossibleMove> getPossibleMovesForPieceInternal(Piece piece, State state) {
    return getPossibleMovesForPieceInternal(piece, state, false);
  }

  private List<PossibleMove> captureMovesVerifiedForTerminality(State state,
                                                                List<PossibleMove> captureMoves,
                                                                boolean isCaptureTerminalityCheck) {

    if (isCaptureTerminalityCheck) {
      return captureMoves;
    }

    List<PossibleMove> res = new ArrayList<>();
    for (PossibleMove c : captureMoves) {
      var moves = getPossibleMovesForPieceInternal(new Piece(c.destination(),
              c.piece().side()),
          state, true);
      if (moves.stream().anyMatch(PossibleMove::isCapture)) {
        res.add(new PossibleMove(c.piece(), c.destination(), c.isCapture(),
            false));
      } else {
        res.add(new PossibleMove(c.piece(), c.destination(), c.isCapture(),
            true));
      }
    }

    return res;
  }

  private List<PossibleMove> getMoves(State state, Piece piece,
                                      LinkedList<Integer> diagonalSource) {


    LinkedList<Integer> ordered = new LinkedList<>(diagonalSource);
    LinkedList<Integer> reversed = new LinkedList<>(diagonalSource.reversed());
    var diagonal = piece.isLight() ? reversed : ordered;

    Side side = piece.side();
    // FIXME Domain object should separate from api request object
    List<Integer> kings = state.getKings() == null ? List.of() : state.getKings();
    boolean isKing = side == Side.DARK ?
        (state.getDark().contains(piece.position()) && kings.contains(piece.position()))
        : (state.getLight().contains(piece.position()) && kings.contains(piece.position()));

    var res = new ArrayList<PossibleMove>();

    collectMoves(state, piece, diagonal, res, isKing);

    return res;
  }

  private void collectMoves(State state, Piece piece, LinkedList<Integer> diagonal, ArrayList<PossibleMove> res,
                            boolean isKing) {
    if (diagonal.contains(piece.position())) {

      if (!isKing) {
        if (piece.position() != diagonal.peekLast()) {

          var nextAfterNumIdx = diagonal.indexOf(piece.position()) + 1;

          // TODO Here is functionality missing for capturing backwards
          if (StateUtils.getSide(piece.oppositeSide(), state).contains(diagonal.get(nextAfterNumIdx))) {
            determineCaptureMove(nextAfterNumIdx + 1, diagonal, state, piece).ifPresent(
                res::add
            );

          } else {
            // TODO before this a check against state needs to be done
            res.add(new PossibleMove(piece, diagonal.get(nextAfterNumIdx), false, true));
          }
        }
      } else {
        // isKing == true
        var nextAfterNumIdx = diagonal.indexOf(piece.position()) - 1;
        res.add(new PossibleMove(piece, diagonal.get(nextAfterNumIdx), false, true));
      }
    }


    int pieceNum = piece.position();
    // TODO Refactor duplication
    if (diagonal.contains(pieceNum)) {
      // for backwards moves only capture check is performed
      var dr = diagonal.reversed();
      if (pieceNum != dr.peekLast()) {

        var nextAfterNumIdx = dr.indexOf(pieceNum) + 1;
        // TODO Here is functionality missing for capturing backwards
        if (StateUtils.getSide(piece.oppositeSide(), state).contains(dr.get(nextAfterNumIdx))) {
          determineCaptureMove(nextAfterNumIdx + 1, dr, state, piece).ifPresent(
              res::add
          );
        }
      }
    }
  }

  private Optional<PossibleMove> determineCaptureMove(int nextNextIdx,
                                                      LinkedList<Integer> diagonal,
                                                      State state,
                                                      Piece piece
  ) {
    if (nextNextIdx > diagonal.size() - 1) {
      // can't capture the opponent, if they take the last diagonal position
      return Optional.empty();
    }


    boolean isLandingCellEmpty = StateUtils.isEmptyCell(diagonal.get(nextNextIdx), state);
    if (isLandingCellEmpty) {
      // TODO Mind isTerminal is true just for now
      return Optional.of(new PossibleMove(piece, diagonal.get(nextNextIdx), true, true));
    }

    return Optional.empty();
  }
}
