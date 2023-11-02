package com.example.checkers.service;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.Side;
import com.example.checkers.model.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface BoardService {

    Map<Integer, List<PossibleMove>> getPossibleMoves(Side side, State state);

    static List<BoardServiceImpl.Position> getAdjacentSquares(Integer square) {
        Integer[] ij = getIJ(square);
        Integer i = ij[0];
        Integer j = ij[1];

        List<BoardServiceImpl.Position> positions = new ArrayList<>();
        positions.add(new BoardServiceImpl.Position(i - 1, j - 1));
        positions.add(new BoardServiceImpl.Position(i + 1, j - 1));
        positions.add(new BoardServiceImpl.Position(i - 1, j + 1));
        positions.add(new BoardServiceImpl.Position(i + 1, j + 1));

        return positions.stream().filter(BoardService::isWithinBoard).toList();
    }


    static List<Integer> getAdjacentSquaresNumbers(Integer square) {
        Integer[] ij = getIJ(square);
        Integer i = ij[0];
        Integer j = ij[1];

        List<BoardServiceImpl.Position> positions = new ArrayList<>();
        positions.add(new BoardServiceImpl.Position(i - 1, j - 1));
        positions.add(new BoardServiceImpl.Position(i + 1, j - 1));
        positions.add(new BoardServiceImpl.Position(i - 1, j + 1));
        positions.add(new BoardServiceImpl.Position(i + 1, j + 1));

        return positions.stream()
                .filter(BoardService::isWithinBoard)
                .toList().stream()
                .map(p -> Checkerboard.getAllSquaresArray()[p.i()][p.j()])
                .toList();
    }


    static Integer[] getIJ(Integer cell) {
        for (int i = 0; i < Checkerboard.getAllSquaresArray().length; i++) {
            int j = Arrays.asList(Checkerboard.getAllSquaresArray()[i]).indexOf(cell);
            if (j != -1) {
                return new Integer[]{i, j};
            }
        }
        throw new IllegalArgumentException("Can't find provided cell: " + cell);
    }

    static boolean isWithinBoard(BoardServiceImpl.Position position) {
        try {
            Integer i = Checkerboard.getAllSquaresArray()[position.i()][position.j()];
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }
}
