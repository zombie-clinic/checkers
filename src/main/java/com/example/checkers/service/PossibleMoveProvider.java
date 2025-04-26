package com.example.checkers.service;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.Piece;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.Side;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PossibleMoveProvider {

    public Map<Integer, List<PossibleMove>> getPossibleMovesForPiece(Piece piece, Checkerboard state) {
        return Map.of(piece.position(), getPossibleMovesForPieceInternal(piece, state));
    }

    public Map<Integer, List<PossibleMove>> getPossibleMovesForSide(Side side, Checkerboard state) {
        var map = new HashMap<Integer, List<PossibleMove>>();
        for (int i : state.getSide(side)) {
            var possibleMoves = getPossibleMovesForPieceInternal(Piece.of(i, side), state);
            if (!possibleMoves.isEmpty()) {
                map.put(i, possibleMoves);
            }
        }
        return map;
    }

    List<PossibleMove> getPossibleMovesForPieceInternal(Piece piece,
                                                        Checkerboard state,
                                                        boolean isChainCaptureCheck) {

        var moves = new ArrayList<PossibleMove>();
        for (LinkedList<Integer> diagonal : Checkerboard.getDiagonals()) {
            moves.addAll(getMoves(state, piece, diagonal));
        }

        if (moves.stream().anyMatch(PossibleMove::isCapture)) {
            return captureMovesVerifiedForTerminality(
                    state,
                    piece.side(),
                    moves.stream().filter(PossibleMove::isCapture).toList(),
                    isChainCaptureCheck
            );
        }

        return moves.stream()
                .filter(i -> isDestinationAvailable(state, i))
                .toList();
    }

    List<PossibleMove> getPossibleMovesForPieceInternal(Piece piece, Checkerboard state) {
        return getPossibleMovesForPieceInternal(piece, state, false);
    }

    private List<PossibleMove> captureMovesVerifiedForTerminality(Checkerboard state,
                                                                  Side side,
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

    private List<PossibleMove> getMoves(Checkerboard state, Piece piece,
                                        LinkedList<Integer> diagonalSource) {


        LinkedList<Integer> diagonal = piece.isLight() ?
                new LinkedList<>(diagonalSource.reversed()) : new LinkedList<>(diagonalSource);

        var res = new ArrayList<PossibleMove>();

        if (diagonal.contains(piece.position())) {
            if (piece.position() != diagonal.peekLast()) {

                var nextAfterNumIdx = diagonal.indexOf(piece.position()) + 1;
                // TODO Here is functionality missing for capturing backwards
                if (state.getSide(piece.oppositeSide()).contains(diagonal.get(nextAfterNumIdx))) {
                    determineCaptureMove(nextAfterNumIdx + 1, diagonal, state, piece).ifPresent(
                            res::add
                    );

                } else {

                    // TODO before this a check against state needs to be done
                    res.add(
                            new PossibleMove(piece,
                                    diagonal.get(diagonal.lastIndexOf(piece.position()) + 1), false,
                                    true));

                }
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
                if (state.getSide(piece.oppositeSide()).contains(dr.get(nextAfterNumIdx))) {
                    determineCaptureMove(nextAfterNumIdx + 1, dr, state, piece).ifPresent(
                            res::add
                    );

                }
            }
        }


        return res;

    }

    private Optional<PossibleMove> determineCaptureMove(int nextNextIdx,
                                                        LinkedList<Integer> list,
                                                        Checkerboard state,
                                                        Piece piece
    ) {
        if (nextNextIdx > list.size() - 1) {
            return Optional.empty();
        }
        if (state.isEmptyCell(list.get(nextNextIdx))) {
            // TODO Mind isTerminal is true just for now
            return Optional.of(new PossibleMove(piece, list.get(nextNextIdx), true, true));
        }
        return Optional.empty();
    }

    private boolean isDestinationAvailable(Checkerboard state, PossibleMove i) {
        return !(state.getSide(Side.DARK).contains(i.destination()) || state.getSide(Side.LIGHT).contains(i.destination()));
    }
}
