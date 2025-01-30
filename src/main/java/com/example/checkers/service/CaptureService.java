package com.example.checkers.service;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.Side;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.State;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CaptureService {

    private final BoardService boardService;

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

    private AfterCaptureState process(List<Integer> attacker, List<Integer> defender, MoveRecord moveRecord) {
        Integer start = moveRecord.start;
        Integer dest = moveRecord.dest;

        attacker.remove(start);
        attacker.add(dest);

        Integer capture = getSingleCapture(start, dest);
        defender.remove(capture);

        return new AfterCaptureState(attacker, defender);
    }

    record AfterCaptureState(List<Integer> attacker, List<Integer> defender) {

    }

    private Integer getSingleCapture(Integer start, Integer dest) {
        Set<BoardServiceImpl.Position> startNeighbors = new HashSet<>(BoardService.getAdjacentSquares(start));
        Set<BoardServiceImpl.Position> destNeighbors = new HashSet<>(BoardService.getAdjacentSquares(dest));

        Sets.SetView<BoardServiceImpl.Position> intersection = Sets.intersection(startNeighbors, destNeighbors);
        if (intersection.size() != 1) {
            throw new IllegalStateException("Failed to determine capture: %sx%s".formatted(start, dest) );
        }

        BoardServiceImpl.Position capturePos = intersection.stream().findFirst().get();
        return Checkerboard.getAllSquaresArray()[capturePos.i()][capturePos.j()];
    }

    record MoveRecord(Integer start, Integer dest) {
        static MoveRecord createMoveRecord(String captureString) {
            String[] pos = captureString.split("x");
            return new MoveRecord(Integer.valueOf(pos[0]), Integer.valueOf(pos[1]));
        }
    }
}
