package com.example.checkers.domain;

import com.example.checkers.model.State;
import com.example.checkers.service.CaptureService;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Checkerboard {

    private final List<Integer> darkPieces;

    private final List<Integer> lightPieces;

    @Getter
    Map<Integer, Square> squareMap;

    public Checkerboard(List<Integer> darkPieces, List<Integer> lightPieces) {
        this.darkPieces = darkPieces;
        this.lightPieces = lightPieces;


        squareMap = getPlayableSquaresList().stream()
                .collect(Collectors.toMap(Function.identity(), (Integer v) -> {
                    if (darkPieces.contains(v)) {
                        return new Square(v, PieceType.DARK,
                                CaptureService.getAdjacentSquaresNumbers(v));
                    } else if (lightPieces.contains(v)) {
                        return new Square(v, PieceType.LIGHT,
                                CaptureService.getAdjacentSquaresNumbers(v));
                    } else {
                        return new Square(v, PieceType.EMPTY,
                                CaptureService.getAdjacentSquaresNumbers(v));
                    }
                }));
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

    public static List<Integer> getPlayableSquaresList() {
        return new ArrayList<>(IntStream.rangeClosed(1, 32).boxed().toList());
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

    public boolean isDark(Integer key) {
        return squareMap.get(key).pieceType().equals(PieceType.DARK);
    }

    public boolean isLight(Integer key) {
        return squareMap.get(key).pieceType().equals(PieceType.LIGHT);
    }
    
    public boolean isEmptyCell(int num) {
        return !darkPieces.contains(num) && !lightPieces.contains(num);
    }
}
