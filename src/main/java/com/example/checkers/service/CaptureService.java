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
        State state = moveRequest.getState();
        Side side = Side.valueOf(moveRequest.getSide());
        String[] pos = moveRequest.getMove().split("x");
        MoveRecord moveRecord = new MoveRecord(Integer.valueOf(pos[0]), Integer.valueOf(pos[1]));

        return switch (side) {
            case BLACK -> {
                NewState newState = calculateState(state.getBlack(), state.getWhite(), moveRecord);
                yield new State(newState.attacker, newState.defender);
            }
            case WHITE -> {
                NewState newState = calculateState(state.getWhite(), state.getBlack(), moveRecord);
                yield new State(newState.defender, newState.attacker);            }
        };
    }

    private NewState calculateState(List<Integer> attacker, List<Integer> defender, MoveRecord moveRecord) {
        Integer start = moveRecord.start;
        Integer dest = moveRecord.dest;

        attacker.remove(start);
        attacker.add(dest);

        Integer capture = getCapture(start, dest);

        defender.remove(capture);

        return new NewState(
                attacker, defender
        );
    }

    record NewState(List<Integer> attacker, List<Integer> defender) {

    }

    private Integer getCapture(Integer start, Integer dest) {
        Set<BoardServiceImpl.Position> val1 = new HashSet<>(boardService.getValidNeighborsForPosition(start));
        Set<BoardServiceImpl.Position> val2 = new HashSet<>(boardService.getValidNeighborsForPosition(dest));

        Sets.SetView<BoardServiceImpl.Position> intersection = Sets.intersection(val1, val2);
        if (intersection.size() != 1) {
            throw new IllegalStateException(STR."Failed to determine capture: \{ start }x\{ dest }");
        }

        BoardServiceImpl.Position capturePos = intersection.stream().findFirst().get();
        return Board.getBoardArray()[capturePos.i()][capturePos.j()];
    }

    record MoveRecord (Integer start, Integer dest) {

    }
}
