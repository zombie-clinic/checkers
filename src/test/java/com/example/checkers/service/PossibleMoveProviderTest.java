package com.example.checkers.service;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.Side;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.example.checkers.domain.Checkerboard.state;
import static com.example.checkers.domain.Side.DARK;
import static com.example.checkers.domain.Side.LIGHT;

public class PossibleMoveProviderTest {

    private static Stream<Arguments> getInputsForEmptyBoard() {
        return Stream.of(
                Arguments.of(1, DARK, List.of(5, 6)),
                Arguments.of(6, DARK, List.of(9, 10)),
                Arguments.of(12, DARK, List.of(16)),
                Arguments.of(10, DARK, List.of(14, 15)),
                Arguments.of(25, LIGHT, List.of(21, 22)),
                Arguments.of(22, LIGHT, List.of(17, 18)),
                Arguments.of(27, LIGHT, List.of(23, 24)),
                Arguments.of(20, LIGHT, List.of(16))
        );
    }

    private static Stream<Arguments> getInputsForStartingGame() {
        return Stream.of(
                Arguments.of(1, DARK, List.of()),
                Arguments.of(6, DARK, List.of()),
                Arguments.of(12, DARK, List.of(16)),
                Arguments.of(10, DARK, List.of(14, 15)),
                Arguments.of(25, LIGHT, List.of()),
                Arguments.of(22, LIGHT, List.of(17, 18)),
                Arguments.of(27, LIGHT, List.of()),
                Arguments.of(20, LIGHT, List.of(16))
        );
    }

    public static Stream<Arguments> getStatesWithCaptures() {
        // TODO Consider introducing a type - TerminalMove, CaptureMove, CaptureTerminalMove etc
        return Stream.of(
                Arguments.of(state(List.of(1), List.of(6, 15)), 1, DARK, List.of(10), true, false),
                Arguments.of(state(List.of(10), List.of(15)), 10, DARK, List.of(19), true, true),
                Arguments.of(state(List.of(14, 7), List.of(17)), 17, LIGHT, List.of(10), true, false),
                Arguments.of(state(List.of(7), List.of(10)), 10, LIGHT, List.of(3), true, true),
                Arguments.of(state(List.of(2), List.of(7)), 2, DARK, List.of(11), true, true),
                Arguments.of(state(List.of(26), List.of(31)), 31, LIGHT, List.of(22), true, true)
        );
    }

    public static Stream<Arguments> getNoneCaptureStates() {
        return Stream.of(
                Arguments.of(new Checkerboard(List.of(26), List.of(31)), 26, DARK, List.of(30)),
                Arguments.of(new Checkerboard(List.of(2), List.of(7)), 7, LIGHT, List.of(3))
        );
    }

    public static Stream<Arguments> getStatesWithCurvedCaptures() {
        // TODO Consider introducing a type - TerminalMove, CaptureMove, CaptureTerminalMove etc
        return Stream.of(
                Arguments.of(state(List.of(1), List.of(6, 14)), 1, DARK, List.of(10), true, false),
                Arguments.of(state(List.of(22), List.of(18)), 22, DARK, List.of(15), true, true),
                Arguments.of(state(List.of(10), List.of(14)), 10, DARK, List.of(17), true, true),
                Arguments.of(state(List.of(14, 15), List.of(17)), 17, LIGHT, List.of(10), true, false),
                Arguments.of(state(List.of(15), List.of(10)), 10, LIGHT, List.of(19), true, true)
        );
    }

    @ParameterizedTest
    @MethodSource("getInputsForEmptyBoard")
    void givenEmptyBoard_shouldProvideValidMoves(int num, Side side, List<Integer> expected) {
        Checkerboard state = new Checkerboard(
                side == DARK ? List.of(num) : List.of(),
                side == LIGHT ? List.of(num) : List.of());
        var actual = new PossibleMoveProvider().getPossibleMoves(num, side, state);
        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected.stream()
                .map(i -> new PossibleMove(side, num, i, false, true)).toList());
    }

    @ParameterizedTest
    @MethodSource({"getInputsForStartingGame"})
    void givenStartingGame_shouldProvideValidMoves(int num, Side side,
                                                   List<Integer> expected) {
        // TODO Make starting state constructor
        var state = new Checkerboard(
                Checkerboard.getStartingState().getDark(),
                Checkerboard.getStartingState().getLight());
        var actual = new PossibleMoveProvider().getPossibleMoves(num, side, state);
        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected.stream()
                .map(i -> new PossibleMove(side, num, i, false, true)).toList());
    }

    @ParameterizedTest
    @MethodSource({"getStatesWithCaptures"})
    void givenStatesWithCaptures_shouldProvideValidMoves(Checkerboard state, int num,
                                                         Side side,
                                                         List<Integer> expected,
                                                         boolean isCapture, boolean isTerminal) {
        var actual = new PossibleMoveProvider().getPossibleMoves(num, side, state);
        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected.stream()
                .map(i -> new PossibleMove(side, num, i, isCapture, isTerminal)).toList());
    }

    @ParameterizedTest
    @MethodSource({"getNoneCaptureStates"})
    void givenNonCaptureStates_shouldProvideValidMoves(Checkerboard state, int num, Side side,
                                                       List<Integer> expected) {
        var actual = new PossibleMoveProvider().getPossibleMoves(num, side, state);
        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected.stream()
                .map(i -> new PossibleMove(side, num, i, false, true)).toList());
    }

    @ParameterizedTest
    @MethodSource({"getStatesWithCurvedCaptures"})
    void givenStatesWithCurvedCaptures_shouldProvideValidMoves(Checkerboard state, int num,
                                                               Side side,
                                                               List<Integer> expected,
                                                               boolean isCapture,
                                                               boolean isTerminal) {
        var actual = new PossibleMoveProvider().getPossibleMoves(num, side, state);
        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected.stream()
                .map(i -> new PossibleMove(side, num, i, isCapture, isTerminal)).toList());
    }
}
