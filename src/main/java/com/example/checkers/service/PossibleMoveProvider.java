package com.example.checkers.service;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.Side;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class PossibleMoveProvider {

    List <PossibleMove> getPossibleMoves(int num, Side side, Checkerboard state, boolean isCaptureTerminalityCheck) {
        LinkedList<Integer> left1 = new LinkedList<>(List.of(1, 5));
        LinkedList<Integer> left2 = new LinkedList<>(List.of(2, 6, 9, 13));
        LinkedList<Integer> left3 = new LinkedList<>(List.of(3, 7, 10, 14, 17, 21));
        LinkedList<Integer> left4 = new LinkedList<>(List.of(4, 8, 11, 15, 18, 22, 25, 29));
        LinkedList<Integer> left5 = new LinkedList<>(List.of(12, 16, 19, 23, 26, 30));
        LinkedList<Integer> left6 = new LinkedList<>(List.of(20, 24, 27, 31));
        LinkedList<Integer> left7 = new LinkedList<>(List.of(28, 32));

        LinkedList<Integer> right1 = new LinkedList<>(List.of(4));
        LinkedList<Integer> right2 = new LinkedList<>(List.of(3, 8, 12));
        LinkedList<Integer> right3 = new LinkedList<>(List.of(2, 7, 11, 16, 20));
        LinkedList<Integer> right4 = new LinkedList<>(List.of(1, 6, 10, 15, 19, 24, 28));
        LinkedList<Integer> right5 = new LinkedList<>(List.of(5, 9, 14, 18, 23, 27, 32));
        LinkedList<Integer> right6 = new LinkedList<>(List.of(13, 17, 22, 26, 31));
        LinkedList<Integer> right7 = new LinkedList<>(List.of(21, 25, 30));
        LinkedList<Integer> right8 = new LinkedList<>(List.of(29));

        List<LinkedList<Integer>> board = List.of(
                left1, left2, left3, left4, left5, left6, left7,
                right1, right2, right3, right4, right5, right6, right7, right8
        );

        List<PossibleMove> res = new ArrayList<>();
        for (LinkedList<Integer> l : board) {
            if (side == Side.LIGHT) {
                // TODO check if creating a defensive copy needed?
                getMoves(state, Side.LIGHT, num, l.reversed()).ifPresent(res::add);
                if (isCaptureTerminalityCheck) {
                    getMoves(state, Side.LIGHT, num, l).ifPresent(res::add);
                }
            } else {
                getMoves(state, Side.DARK, num, l).ifPresent(res::add);
                if (isCaptureTerminalityCheck) {
                    getMoves(state, Side.DARK, num, l.reversed()).ifPresent(res::add);
                }
            }
        }

        if (res.stream().anyMatch(PossibleMove::isCapture)) {
            List<PossibleMove> captureMoves = res.stream()
                    .filter(PossibleMove::isCapture)
                    .toList();
            if (isCaptureTerminalityCheck) {
                return captureMoves;
            }
            return captureMovesVerifiedForTerminality(state, side, captureMoves);
        }

        return res.stream()
                .filter(i -> !(state.getSide(Side.DARK).contains(i.destination()) || state.getSide(Side.LIGHT).contains(i.destination())))
                .toList();
    }


    List<PossibleMove> getPossibleMoves(int num, Side side, Checkerboard state) {
        return getPossibleMoves(num, side, state, false);
    }

    private List<PossibleMove> captureMovesVerifiedForTerminality(Checkerboard state,
                                                                  Side side,
                                                                  List<PossibleMove> captureMoves) {

        List<PossibleMove> res = new ArrayList<>();
        for (PossibleMove c : captureMoves) {
            var moves = getPossibleMoves(c.destination(), side, state, true);
            if (moves.stream().anyMatch(PossibleMove::isCapture)) {
                res.add(new PossibleMove(c.side(), c.position(), c.destination(), c.isCapture(), false));
            } else {
                res.add(new PossibleMove(c.side(), c.position(), c.destination(), c.isCapture(), true));
            }
        }

        return res;
    }

    private Optional<PossibleMove> getMoves(Checkerboard state, Side side, int num,
                                            LinkedList<Integer> list) {
        if (list.contains(num)) {
            if (num != list.peekLast()) {

                var nextAfterNumIdx = list.indexOf(num) + 1;
                var oppositeSide = side == Side.DARK ? Side.LIGHT : Side.DARK;
                // TODO Here is functionality missing for capturing backwards
                if (state.getSide(oppositeSide).contains(list.get(nextAfterNumIdx))) {
                    return determineCaptureMove(nextAfterNumIdx + 1, list, state, side, num);
                }

                // TODO before this a check against state needs to be done
                return Optional.of(
                        new PossibleMove(side, num, list.get(list.lastIndexOf(num) + 1), false,
                                true));
            }
        }

        return Optional.empty();
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
}
