package com.example.checkers.service;

import com.example.checkers.domain.Board;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.Side;
import com.example.checkers.model.State;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

import static com.example.checkers.domain.Side.BLACK;
import static com.example.checkers.domain.Side.WHITE;
import static java.util.stream.Collectors.toMap;

@Service
public class BoardServiceImpl implements BoardService {

    @Override
    public Map<Integer, List<PossibleMove>> getPossibleMoves(Side side, State state) {

        if (state == null) {
            state = Board.getInitialState();
        }

        return switch (side) {
            case BLACK -> getPossibleMovesForBlack(state);
            case WHITE -> getPossibleMovesForWhite(state);
        };
    }

    private Map<Integer, List<PossibleMove>> getPossibleMovesForBlack(State state) {
        Map<Integer, List<Position>> blackPositionsAndNeighbors = state.getBlack().stream().collect(toMap(Function.identity(), this::getIJ)).entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> getValidNeighborsForPosition(entry.getValue())));

        List<Integer> freeCells = Board.getValidCells();
        freeCells.removeAll(state.getBlack());
        freeCells.removeAll(state.getWhite());

        List<Position> freeCellsPositions = freeCells.stream().map(this::getIJ).map(fc -> new Position(fc[0], fc[1])).toList();

        Map<Integer, List<PossibleMove>> possibleMoves = new HashMap<>();

        for (Map.Entry<Integer, List<Position>> pieceNeighbors : blackPositionsAndNeighbors.entrySet()) {
            Integer piece = pieceNeighbors.getKey();
            List<Position> neighbors = pieceNeighbors.getValue();

            // TODO Check if there are white neighbors
            // if (neighbors.stream().anyMatch(n -> state.getWhite().contains(n))) {
            // check white for empty neighbors for capture opportunity
            // also remember the direction (vector?)
            // logic for capture, different service/class?
            // }


            for (Position free : freeCellsPositions) {
                if (neighbors.contains(free)) {
                    possibleMoves.computeIfAbsent(piece, k -> new ArrayList<>()).add(new PossibleMove(BLACK, piece, getBoard()[free.i][free.j], false, true));
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

    private Map<Integer, List<PossibleMove>> getPossibleMovesForWhite(State state) {
        Map<Integer, List<Position>> blackPositionsAndNeighbors = state.getWhite().stream().collect(toMap(Function.identity(), this::getIJ)).entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> getValidNeighborsForPosition(entry.getValue())));


        List<Integer> freeCells = Board.getValidCells();
        freeCells.removeAll(state.getBlack());
        freeCells.removeAll(state.getWhite());

        List<Position> freeCellsPositions = freeCells.stream().map(this::getIJ).map(fc -> new Position(fc[0], fc[1])).toList();

        Map<Integer, List<PossibleMove>> possibleMoves = new HashMap<>();

        for (Map.Entry<Integer, List<Position>> pieceNeighbors : blackPositionsAndNeighbors.entrySet()) {
            Integer piece = pieceNeighbors.getKey();
            List<Position> neighbors = pieceNeighbors.getValue();

            // TODO Check if there are white neighbors
            // if (neighbors.stream().anyMatch(n -> state.getWhite().contains(n))) {
            // check white for empty neighbors for capture opportunity
            // also remember the direction (vector?)
            // logic for capture, different service/class?
            // }


            for (Position free : freeCellsPositions) {
                if (neighbors.contains(free)) {
                    possibleMoves.computeIfAbsent(piece, k -> new ArrayList<>()).add(new PossibleMove(WHITE, piece, getBoard()[free.i][free.j], false, true));
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
        return Board.getBoardArray();
    }

    record Position(int i, int j) {

    }

    private List<Position> getValidNeighborsForPosition(Integer[] cell) {
        Integer i = cell[0];
        Integer j = cell[1];

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
