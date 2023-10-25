package com.example.demo.service;

import com.example.demo.domain.Board;
import com.example.demo.domain.PossibleMove;
import com.example.demo.domain.Side;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardServiceTest {

    @Test
    void givenInitialState_whenFirstMove_thenGetPossibleMovesForWhite() {
        BoardService boardService = new BoardServiceImpl();
        Map<Integer, List<PossibleMove>> moves = boardService.getPossibleMoves(Side.WHITE, Board.getInitialState());

        assertEquals(4, moves.size()); // 4 pieces are able to perform moves

//        // 21-17
//        Assertions.assertThat(
//                moves.get(21).size()
//                ).isEqualTo(21).
//                .isEqualTo(21))
//
//        containsOneMove(moves.get(21)).toDestination(17);
//
//        assertEquals(1, moves.get(21).size());
//        assertEquals(17, moves.get(21).getFirst().destination());
//        assertEquals(1, moves.get(21).size());
//
//        // 22-17, 22-18
//        assertEquals(2, moves.get(22).size());
//        List<Integer> destinations = moves.get(22).stream()
//                .map(PossibleMove::destination)
//                .toList();
//        assertTrue(List.of(17, 18).containsAll(destinations));
//        assertEquals(2, moves.get(22).size());
//
//        // 23-18, 23-19
//        assertEquals(2, moves.get(22).size());
//        List<Integer> destinations = moves.get(22).stream()
//                .map(PossibleMove::destination)
//                .toList();
//        assertTrue(List.of(17, 18).containsAll(destinations));
//        assertEquals(2, moves.get(22).size());
//
//        // 22-17, 22-18
//        assertEquals(2, moves.get(22).size());
//        List<Integer> destinations = moves.get(22).stream()
//                .map(PossibleMove::destination)
//                .toList();
//        assertTrue(List.of(17, 18).containsAll(destinations));
//        assertEquals(2, moves.get(22).size());

    }

//    private DestinationRecord containsOneMove(List<PossibleMove> move) {
//        assertEquals(1, move.size());
//        return new DestinationRecord(
//
//        );
//    }

    public record DestinationRecord(Predicate<Integer> toDestination) {

    }
}