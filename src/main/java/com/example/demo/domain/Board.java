package com.example.demo.domain;

import com.example.demo.model.State;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Board {

    public static State getInitialState() {
        return new State(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), List.of(21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32));
    }

    public static Integer[][] getBoardArray() {
        return new Integer[][]{{0, 1, 0, 2, 0, 3, 0, 4}, {5, 0, 6, 0, 7, 0, 8, 0}, {0, 9, 0, 10, 0, 11, 0, 12}, {13, 0, 14, 0, 15, 0, 16, 0}, {0, 17, 0, 18, 0, 19, 0, 20}, {21, 0, 22, 0, 23, 0, 24, 0}, {0, 25, 0, 26, 0, 27, 0, 28}, {29, 0, 30, 0, 31, 0, 32, 0},};
    }

    public static List<Integer> getValidCells() {
        return new ArrayList<>(IntStream.rangeClosed(1, 32).boxed().toList());
    }
}
