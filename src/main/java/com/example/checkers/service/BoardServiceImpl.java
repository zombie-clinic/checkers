package com.example.checkers.service;

import com.example.checkers.domain.*;
import com.example.checkers.model.State;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BoardServiceImpl implements BoardService {

    @Override
    public Map<Integer, List<PossibleMove>> getPossibleMoves(Side side, State state) {

        if (state == null) {
            state = Checkerboard.getStartingState();
        }

        Checkerboard checkerboard = new Checkerboard(state.getDark(), state.getLight());

        return switch (side) {
            case DARK -> getPossibleMoves(Side.DARK, checkerboard);
            case LIGHT -> getPossibleMoves(Side.LIGHT, checkerboard);
        };
    }

    private Map<Integer, List<PossibleMove>> getPossibleMoves(Side side, Checkerboard checkerboard) {

        var moves = new HashMap<Integer, List<PossibleMove>>();
        var maybeMoves = new HashMap<Integer, List<PossibleMove>>();

        for (Integer start : checkerboard.getSide(side)) {

            Square square1 = checkerboard.getSquareMap().get(start);
            Square square = new Square(
                    start, side == Side.DARK ? PieceType.DARK : PieceType.LIGHT,
                    square1.neighborSquares().stream()
                            .filter(el -> side == Side.DARK ? el > start : el < start)
                            .toList()
            );

            // empty neighbors
            square.neighborSquares().stream()
                    .map(dest -> checkerboard.getSquareMap().get(dest))
                    .filter(dest -> dest.pieceType().equals(PieceType.EMPTY))
                    .forEach(dest -> moves
                            .computeIfAbsent(start, v -> new ArrayList<>())
                            // FIXME isCapture, isTerminal
                            .add(new PossibleMove(side, start, dest.number(), false, true)
                            ));

            // busy of same color - skip
            // busy of opponent color - maybe

            PieceType opponent;
            if (checkerboard.getSquareMap().get(start).pieceType().equals(PieceType.DARK)) {
                opponent = PieceType.LIGHT;
            } else {
                opponent = PieceType.DARK;
            }

            square.neighborSquares().stream()
                    .map(dest -> checkerboard.getSquareMap().get(dest))
                    .filter(dest -> dest.pieceType().equals(opponent))
                    .forEach(dest -> maybeMoves
                            .computeIfAbsent(start, v -> new ArrayList<>())
                            .add(new PossibleMove(side, start, dest.number(), null, null)
                            ));
        }

        for (var entry : maybeMoves.entrySet()) {



            for (PossibleMove possibleMove : entry.getValue()) {

                Square square = checkerboard.getSquareMap().get(possibleMove.destination());

                // empty neighbors
                square.neighborSquares().stream()
                        .map(captureDest -> checkerboard.getSquareMap().get(captureDest))
                        .filter(captureDest -> captureDest.pieceType().equals(PieceType.EMPTY))
                        .filter(captureDest -> Math.abs(captureDest.number() - possibleMove.position()) != 1)
                        .filter(captureDest -> Math.abs(captureDest.number() - possibleMove.position()) != 8)
                        .forEach(captureDest -> moves
                                .computeIfAbsent(possibleMove.position(), v -> new ArrayList<>())
                                .add(new PossibleMove(side, possibleMove.position(), captureDest.number(), true, null)
                                ));
            }
        }

        return moves;
    }

    public record Position(int i, int j) {

    }
}
