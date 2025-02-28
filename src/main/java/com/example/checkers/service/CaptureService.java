package com.example.checkers.service;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.Side;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.State;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class CaptureService {

    public State generateAfterCaptureState(MoveRequest moveRequest) {
        List<Integer> black = moveRequest.getState().getDark();
        List<Integer> white = moveRequest.getState().getLight();

        Side side = Side.valueOf(moveRequest.getSide());
        MoveRecord moveRecord = MoveRecord.createMoveRecord(moveRequest.getMove());

        return switch (side) {
            case DARK -> {
                AfterCaptureState state = process(black, white, moveRecord);
                yield new State(state.attacker, state.defender);
            }
            case LIGHT -> {
                AfterCaptureState state = process(white, black, moveRecord);
                yield new State(state.defender, state.attacker);
            }
        };
    }


    // Private methods

    private AfterCaptureState process(List<Integer> attacker, List<Integer> defender,
                                      MoveRecord moveRecord) {
        Integer start = moveRecord.start;
        Integer dest = moveRecord.dest;

        attacker.remove(start);
        attacker.add(dest);

        Integer capture = getSingleCapture(start, dest);
        defender.remove(capture);

        return new AfterCaptureState(attacker, defender);
    }

    private Integer getSingleCapture(Integer start, Integer dest) {
        Set<Position> startNeighbors = new HashSet<>(getAdjacentSquares(start));
        Set<Position> destNeighbors = new HashSet<>(getAdjacentSquares(dest));

        Sets.SetView<Position> intersection = Sets.intersection(startNeighbors, destNeighbors);
        if (intersection.size() != 1) {
            throw new IllegalStateException("Failed to determine capture: %sx%s".formatted(start,
                    dest));
        }

        Position capturePos = intersection.stream().findFirst().get();
        return Checkerboard.getAllSquaresArray()[capturePos.i()][capturePos.j()];
    }

    private record AfterCaptureState(List<Integer> attacker, List<Integer> defender) {

    }
   private record MoveRecord(Integer start, Integer dest) {
        static MoveRecord createMoveRecord(String captureString) {
            String[] pos = captureString.split("x");
            return new MoveRecord(Integer.valueOf(pos[0]), Integer.valueOf(pos[1]));
        }


    }
    private record Position(int i, int j) {

    }
    private static boolean isWithinBoard(Position position) {
        try {
            Integer i = Checkerboard.getAllSquaresArray()[position.i()][position.j()];
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }
    private static Integer[] getIJ(Integer cell) {
        for (int i = 0; i < Checkerboard.getAllSquaresArray().length; i++) {
            int j = Arrays.asList(Checkerboard.getAllSquaresArray()[i]).indexOf(cell);
            if (j != -1) {
                return new Integer[]{i, j};
            }
        }
        throw new IllegalArgumentException("Can't find provided cell: " + cell);
    }

    private static List<CaptureService.Position> getAdjacentSquares(Integer square) {
        Integer[] ij = getIJ(square);
        Integer i = ij[0];
        Integer j = ij[1];

        List<CaptureService.Position> positions = new ArrayList<>();
        positions.add(new Position(i - 1, j - 1));
        positions.add(new Position(i + 1, j - 1));
        positions.add(new Position(i - 1, j + 1));
        positions.add(new Position(i + 1, j + 1));

        return positions.stream().filter(CaptureService::isWithinBoard).toList();
    }
}
