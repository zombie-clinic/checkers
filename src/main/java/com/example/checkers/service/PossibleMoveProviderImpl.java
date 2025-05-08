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
                                                      State state) {

    var moves = new ArrayList<PossibleMove>();
    for (LinkedList<Integer> diagonal : Checkerboard.getDiagonals()) {
      moves.addAll(getMoves(state, piece, diagonal));
    }

    if (moves.stream().anyMatch(PossibleMove::isCapture)) {
      return
          moves.stream().filter(PossibleMove::isCapture).toList();
    }

    return moves.stream()
        .filter(move -> StateUtils.isEmptyCell(move.destination(), state))
        .toList();
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

    if (diagonal.contains(piece.position())) {
      collectForwardMoves(state, piece, diagonal, res, false);
      collectBackwardCaptures(state, piece, diagonal, res);
      if (isKing) {
        collectKingMoves(state, piece, diagonal, res);
        collectBackwardKingMoves(state, piece, diagonal, res);
      }
    }

    return res;
  }

  private void collectForwardMoves(State state,
                                   Piece piece,
                                   LinkedList<Integer> diagonal, ArrayList<PossibleMove> res,
                                   boolean isBackwardsCaptureCheck) {
    int square = piece.position();
    var nextSquareIndex = diagonal.indexOf(square) + 1;
    Integer nextSquare = nextSquareIndex >= diagonal.size() ? null : diagonal.get(nextSquareIndex);
    if (nextSquare == null) {
      return;
    }
    if (nextSquareIndex > diagonal.size() - 1) {
      return;
    }
    if (isSquareFree(state, nextSquare) && !isBackwardsCaptureCheck) {
      res.add(new PossibleMove(piece, nextSquare, false));
    }
    if (isSquareOccupiedByWhatSide(state, piece.oppositeSide(), nextSquare)) {
      checkIfCaptureIsPossible(nextSquareIndex + 1, diagonal, state, piece).ifPresent(res::add);
    }
  }

  private void collectBackwardCaptures(State state, Piece piece, LinkedList<Integer> diagonal, ArrayList<PossibleMove> res) {
    collectForwardMoves(state, piece, diagonal.reversed(), res, true);
  }

  private void collectKingMoves(State state,
                                Piece piece,
                                LinkedList<Integer> diagonal,
                                ArrayList<PossibleMove> res) {

    boolean captureInProgress = false;
    boolean captureDone = false;

    if (diagonal.indexOf(piece.position()) == diagonal.size() - 1) {
      return;
    }

    List<Integer> moveDirection = diagonal.subList(diagonal.indexOf(piece.position()) + 1, diagonal.size());
    for (int destSquare : moveDirection) {
      if (isSquareOccupiedByWhatSide(state, piece.side(), destSquare)) {
        return;
      }
      if (isSquareOccupiedByWhatSide(state, piece.oppositeSide(), destSquare)) {
        if (captureDone) {
          return;
        } else {
          captureInProgress = true;
        }
      }
      if (isSquareFree(state, destSquare)) {
        if (captureInProgress) {
          res.add(new PossibleMove(piece, destSquare, true));
          captureDone = true;
        } else {
          res.add(new PossibleMove(piece, destSquare, false));
        }
      }
    }
  }

  private void collectBackwardKingMoves(State state, Piece piece, LinkedList<Integer> diagonal, ArrayList<PossibleMove> res) {
    collectKingMoves(state, piece, diagonal.reversed(), res);
  }

  private Optional<PossibleMove> checkIfCaptureIsPossible(int nextNextIdx,
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
      return Optional.of(new PossibleMove(piece, diagonal.get(nextNextIdx), true));
    }

    return Optional.empty();
  }

  private static boolean isSquareOccupied(State state, int destSquare) {
    return StateUtils.getSide(Side.LIGHT, state).contains(destSquare)
        || StateUtils.getSide(Side.DARK, state).contains(destSquare);
  }

  private static boolean isSquareFree(State state, int destSquare) {
    return !isSquareOccupied(state, destSquare);
  }

  private static boolean isSquareOccupiedByWhatSide(State state, Side side, int destSquare) {
    return StateUtils.getSide(side, state).contains(destSquare);
  }
}
