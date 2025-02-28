package com.example.checkers.domain;

import com.example.checkers.model.State;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class Checkerboard {

    private final List<Integer> darkPieces;

    private final List<Integer> lightPieces;

    @Getter
    Map<Integer, Square> squareMap;

    public Checkerboard(List<Integer> darkPieces, List<Integer> lightPieces) {
        this.darkPieces = darkPieces;
        this.lightPieces = lightPieces;
    }

    public static State getStartingState() {
        return new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                List.of(21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32));
    }

    public static Integer[][] getAllSquaresArray() {
        return new Integer[][]{
                {0, 1, 0, 2, 0, 3, 0, 4},
                {5, 0, 6, 0, 7, 0, 8, 0},
                {0, 9, 0, 10, 0, 11, 0, 12},
                {13, 0, 14, 0, 15, 0, 16, 0},
                {0, 17, 0, 18, 0, 19, 0, 20},
                {21, 0, 22, 0, 23, 0, 24, 0},
                {0, 25, 0, 26, 0, 27, 0, 28},
                {29, 0, 30, 0, 31, 0, 32, 0}
        };
    }

    public static Checkerboard state(List<Integer> darkPieces, List<Integer> lightPieces) {
        return new Checkerboard(darkPieces, lightPieces);
    }

    public List<Integer> getSide(Side side) {
        return switch (side) {
            case DARK -> darkPieces;
            case LIGHT -> lightPieces;
        };
    }
    
    public boolean isEmptyCell(int num) {
        return !darkPieces.contains(num) && !lightPieces.contains(num);
    }
}
