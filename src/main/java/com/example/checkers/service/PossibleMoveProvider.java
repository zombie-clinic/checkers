package com.example.checkers.service;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.Side;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PossibleMoveProvider {

    Map<Integer, List<PossibleMove>> getPossibleMovesMap(Side side, Checkerboard state) {
        var map = new HashMap<Integer, List<PossibleMove>>();
        // TODO refactor possible moves representation
        for (int i : state.getSide(side)) {
            List<PossibleMove> possibleMoves = getPossibleMoves(i, side, state);
            if (!possibleMoves.isEmpty()) {
                map.put(i, possibleMoves);
            }
        }
        return map;
    }


    public List<PossibleMove> getPossibleMoves(int num, Side side, Checkerboard state) {
        return getPossibleMoves(num, side, state, false);
    }

    // region private methods
    public List<PossibleMove> getPossibleMoves(int num, Side side, Checkerboard state,
                                               boolean isCaptureTerminalityCheck) {

        List<LinkedList<Integer>> board = Checkerboard.getDiagonals();

        List<PossibleMove> res = new ArrayList<>();
        for (LinkedList<Integer> diagonal : board) {
            if (side == Side.LIGHT) {
                // TODO check if creating a defensive copy needed?
                res.addAll(getMoves(state, Side.LIGHT, num, diagonal.reversed(),
                        isCaptureTerminalityCheck));
            } else {
                res.addAll(getMoves(state, Side.DARK, num, diagonal, isCaptureTerminalityCheck));
            }
        }

        if (res.stream().anyMatch(PossibleMove::isCapture)) {
            return onlyMovesWithCaptures(side, state, isCaptureTerminalityCheck, res);
        }

        return res.stream()
                .filter(i -> !(state.getSide(Side.DARK).contains(i.destination()) || state.getSide(Side.LIGHT).contains(i.destination())))
                .toList();
    }

    private List<PossibleMove> onlyMovesWithCaptures(Side side, Checkerboard state,
                                                     boolean isCaptureTerminalityCheck,
                                                     List<PossibleMove> res) {
        List<PossibleMove> captureMoves = res.stream()
                .filter(PossibleMove::isCapture)
                .toList();
        if (isCaptureTerminalityCheck) {
            // return after check to avoid recursion
            return captureMoves;
        }
        return captureMovesVerifiedForTerminality(state, side, captureMoves);
    }

    private List<PossibleMove> captureMovesVerifiedForTerminality(Checkerboard state,
                                                                  Side side,
                                                                  List<PossibleMove> captureMoves) {

        List<PossibleMove> res = new ArrayList<>();
        for (PossibleMove c : captureMoves) {
            var moves = getPossibleMoves(c.destination(), side, state, true);
            if (moves.stream().anyMatch(PossibleMove::isCapture)) {
                res.add(new PossibleMove(c.side(), c.position(), c.destination(), c.isCapture(),
                        false));
            } else {
                res.add(new PossibleMove(c.side(), c.position(), c.destination(), c.isCapture(),
                        true));
            }
        }

        return res;
    }

    private List<PossibleMove> getMoves(Checkerboard state, Side side, int pieceNum,
                                        LinkedList<Integer> diagonal,
                                        boolean isCaptureTerminalityCheck) {

        var res = new ArrayList<PossibleMove>();

        if (diagonal.contains(pieceNum)) {
            if (pieceNum != diagonal.peekLast()) {

                var nextAfterNumIdx = diagonal.indexOf(pieceNum) + 1;
                var oppositeSide = side == Side.DARK ? Side.LIGHT : Side.DARK;
                // TODO Here is functionality missing for capturing backwards
                if (state.getSide(oppositeSide).contains(diagonal.get(nextAfterNumIdx))) {
                    determineCaptureMove(nextAfterNumIdx + 1, diagonal, state, side, pieceNum).ifPresent(
                            res::add
                    );

                } else {

                    // TODO before this a check against state needs to be done
                    res.add(
                            new PossibleMove(side, pieceNum,
                                    diagonal.get(diagonal.lastIndexOf(pieceNum) + 1), false,
                                    true));

                }
            }
        }


        // TODO Refactor duplication
        if (diagonal.contains(pieceNum)) {
            // for backwards moves only capture check is performed
            var dr = diagonal.reversed();
            if (pieceNum != dr.peekLast()) {

                var nextAfterNumIdx = dr.indexOf(pieceNum) + 1;
                var oppositeSide = side == Side.DARK ? Side.LIGHT : Side.DARK;
                // TODO Here is functionality missing for capturing backwards
                if (state.getSide(oppositeSide).contains(dr.get(nextAfterNumIdx))) {
                    determineCaptureMove(nextAfterNumIdx + 1, dr, state, side, pieceNum).ifPresent(
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
                                                        Side side,
                                                        Integer num) {
        if (nextNextIdx > list.size() - 1) {
            return Optional.empty();
        }
        if (state.isEmptyCell(list.get(nextNextIdx))) {
            // TODO Mind isTerminal is true just for now
            return Optional.of(new PossibleMove(side, num, list.get(nextNextIdx), true, true));
        }
        return Optional.empty();
    }
    //endregion
}
