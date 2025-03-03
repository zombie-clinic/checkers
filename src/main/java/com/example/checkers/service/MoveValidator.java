package com.example.checkers.service;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.Side;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.persistence.MoveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MoveValidator implements Validator {

    private final MoveRepository moveRepository;

    private PossibleMoveProvider possibleMoveProvider;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(MoveRequest.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MoveRequest moveRequest = (MoveRequest) target;
        var moves = moveRepository.findAllByGameId(moveRequest.getGameId());
        var lastMove = moves.getLast();
        var possibleMoves = possibleMoveProvider.getPossibleMovesMap(
                Side.valueOf(lastMove.getSide()), Checkerboard.state(
                        Arrays.stream(lastMove.getDark().split(",")).map(Integer::valueOf).toList(),
                        Arrays.stream(lastMove.getLight().split(",")).map(Integer::valueOf).toList()
                ));
        String start = moveRequest.getMove().split("x\\-")[0];
        String end = moveRequest.getMove().split("x\\-")[1];
        List<PossibleMove> movesByStart = possibleMoves.get(Integer.valueOf(start)).stream()
                .filter(m -> m.destination() == Integer.parseInt(end))
                .toList();

        Set<String> possibleMovesSet = movesByStart.stream()
                .map(m -> start + (m.isCapture() ? "x" : "-") + m.destination())
                .collect(Collectors.toSet());


        if (!possibleMovesSet.contains(moveRequest.getMove())) {
            ValidationUtils.rejectIfEmpty(errors, "move", "impossible.move");
        }
    }
}
