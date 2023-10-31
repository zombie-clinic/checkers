package com.example.checkers.service;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.Side;
import com.example.checkers.model.State;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

import static com.example.checkers.domain.Side.DARK;
import static com.example.checkers.domain.Side.LIGHT;
import static java.util.stream.Collectors.toMap;

@Service
public class BoardServiceImpl implements BoardService {

    @Override
    public Map<Integer, List<PossibleMove>> getPossibleMoves(Side side, State state) {

        if (state == null) {
            state = Checkerboard.getStartingState();
        }

        return switch (side) {
            case DARK -> getPossibleMovesForDark(state);
            case LIGHT -> getPossibleMovesForLight(state);
        };
    }

    private Map<Integer, List<PossibleMove>> getPossibleMovesForDark(State state) {
        Map<Integer, List<Position>> blackPositionsAndNeighbors = state.getDark().stream().collect(toMap(Function.identity(), this::getAdjacentSquares));

        List<Integer> freeCells = Checkerboard.getPlayableSquaresList();
        freeCells.removeAll(state.getDark());
        freeCells.removeAll(state.getLight());

        List<Position> freeCellsPositions = freeCells.stream().map(this::getIJ).map(fc -> new Position(fc[0], fc[1])).toList();

        Map<Integer, List<PossibleMove>> possibleMoves = new HashMap<>();

        for (Map.Entry<Integer, List<Position>> pieceNeighbors : blackPositionsAndNeighbors.entrySet()) {
            Integer piece = pieceNeighbors.getKey();
            List<Position> neighbors = pieceNeighbors.getValue();

            // TODO Check if there are white neighbors
            // if (neighbors.stream().anyMatch(n -> state().getLight().contains(n))) {
            // check white for empty neighbors for capture opportunity
            // also remember the direction (vector?)
            // logic for capture, different service/class?
            // }


            for (Position free : freeCellsPositions) {
                if (neighbors.contains(free)) {
                    possibleMoves.computeIfAbsent(piece, k -> new ArrayList<>()).add(new PossibleMove(DARK, piece, getBoard()[free.i][free.j], false, false));
                }
            }

            if (neighbors.stream().anyMatch(freeCellsPositions::contains)) {
                // then it's an opportunity to make a move - but check for numbers outside the board
            }

            // check move is terminal, possbile run the same logic as here, but for one piece
            // so it makes sense to extract one piece logic

        }


        return possibleMoves;
    }

    private Map<Integer, List<PossibleMove>> getPossibleMovesForLight(State state) {
        Map<Integer, List<Position>> blackPositionsAndNeighbors = state.getLight().stream().collect(toMap(Function.identity(), this::getAdjacentSquares));


        List<Integer> freeCells = Checkerboard.getPlayableSquaresList();
        freeCells.removeAll(state.getDark());
        freeCells.removeAll(state.getLight());

        List<Position> freeCellsPositions = freeCells.stream().map(this::getIJ).map(fc -> new Position(fc[0], fc[1])).toList();

        Map<Integer, List<PossibleMove>> possibleMoves = new HashMap<>();

        for (Map.Entry<Integer, List<Position>> pieceNeighbors : blackPositionsAndNeighbors.entrySet()) {
            Integer piece = pieceNeighbors.getKey();
            List<Position> neighbors = pieceNeighbors.getValue();

            // TODO Check if there are white neighbors
            // if (neighbors.stream().anyMatch(n -> state().getLight().contains(n))) {
            // check white for empty neighbors for capture opportunity
            // also remember the direction (vector?)
            // logic for capture, different service/class?
            // }


            for (Position free : freeCellsPositions) {
                if (neighbors.contains(free)) {
                    possibleMoves.computeIfAbsent(piece, k -> new ArrayList<>()).add(new PossibleMove(LIGHT, piece, getBoard()[free.i][free.j], false, false));
                }
            }

            if (neighbors.stream().anyMatch(freeCellsPositions::contains)) {
                // then it's an opportunity to make a move - but check for numbers outside the board
            }

            // check move is terminal, possbile run the same logic as here, but for one piece
            // so it makes sense to extract one piece logic

        }


        return possibleMoves;
    }

    private Integer[][] getBoard() {
        return Checkerboard.getAllSquaresArray();
    }

    public record Position(int i, int j) {

    }

    public List<Position> getAdjacentSquares(Integer square) {
        Integer[] ij = getIJ(square);
        Integer i = ij[0];
        Integer j = ij[1];

        List<Position> positions = new ArrayList<>();
        positions.add(new Position(i - 1, j - 1));
        positions.add(new Position(i + 1, j - 1));
        positions.add(new Position(i - 1, j + 1));
        positions.add(new Position(i + 1, j + 1));

        return positions.stream().filter(this::isWithinBoard).toList();
    }

    private boolean isWithinBoard(Position position) {
        try {
            Integer i = getBoard()[position.i][position.j];
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private Integer[] getIJ(Integer cell) {
        for (int i = 0; i < getBoard().length; i++) {
            int j = Arrays.asList(getBoard()[i]).indexOf(cell);
            if (j != -1) {
                return new Integer[]{i, j};
            }
        }
        throw new IllegalArgumentException("Can't find provided cell: " + cell);
    }
}
