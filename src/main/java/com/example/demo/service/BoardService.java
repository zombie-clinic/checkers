package com.example.demo.service;

import com.example.demo.domain.PossibleMove;
import com.example.demo.domain.Side;
import com.example.demo.model.State;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface BoardService {

    default Integer[][] getBoard() {
        return new Integer[][]{
                {0, 1, 0, 2, 0, 3, 0, 4},
                {5, 0, 6, 0, 2, 0, 8, 0},
                {0, 9, 0, 10, 0, 11, 0, 12},
                {13, 0, 14, 0, 15, 0, 16, 0},
                {0, 17, 0, 18, 0, 19, 0, 20},
                {21, 0, 22, 0, 23, 0, 24, 0},
                {0, 25, 0, 26, 0, 27, 0, 28},
                {29, 0, 30, 0, 31, 0, 32, 0},
        };
    }

    default List<Integer> getValidCells() {
        return IntStream.rangeClosed(1, 32).boxed().toList();
    }

    default List<Integer> getAdjacentCells(int cell) {
        return Stream.of(cell + 3, cell + 4, cell - 3, cell - 4)
                .filter(v -> getValidCells().contains(v))
                .toList();
    }

    Map<Integer, List<PossibleMove>> getPossibleMoves(Side side, State state);
}
