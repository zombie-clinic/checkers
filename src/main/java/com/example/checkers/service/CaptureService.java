package com.example.checkers.service;

import com.example.checkers.domain.Board;
import com.example.checkers.domain.Side;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.State;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class CaptureService {

    private final BoardService boardService;

    public State generateAfterCaptureState(MoveRequest moveRequest) {
        List<Integer> black = moveRequest.getState().getBlack();
        List<Integer> white = moveRequest.getState().getWhite();

        Side side = Side.valueOf(moveRequest.getSide());
        MoveRecord moveRecord = MoveRecord.createMoveRecord(moveRequest.getMove());

        return switch (side) {
            case BLACK -> {
                AfterCaptureState state = process(black, white, moveRecord);
                yield new State(state.attacker, state.defender);
            }
            case WHITE -> {
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
        Set<BoardServiceImpl.Position> startNeighbors = new HashSet<>(boardService.getValidNeighborsForPosition(start));
        Set<BoardServiceImpl.Position> destNeighbors = new HashSet<>(boardService.getValidNeighborsForPosition(dest));

        Sets.SetView<BoardServiceImpl.Position> intersection = Sets.intersection(startNeighbors, destNeighbors);
        if (intersection.size() != 1) {
            throw new IllegalStateException(STR. "Failed to determine capture: \{ start }x\{ dest }" );
        }

        BoardServiceImpl.Position capturePos = intersection.stream().findFirst().get();
        return Board.getBoardArray()[capturePos.i()][capturePos.j()];
    }

    record MoveRecord(Integer start, Integer dest) {
        static MoveRecord createMoveRecord(String captureString) {
            String[] pos = captureString.split("x");
            return new MoveRecord(Integer.valueOf(pos[0]), Integer.valueOf(pos[1]));
        }
    }
}
