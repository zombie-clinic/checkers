package com.example.checkers.service;

import static com.example.checkers.core.Side.DARK;
import static com.example.checkers.core.Side.LIGHT;

import com.example.checkers.core.Checkerboard;
import com.example.checkers.core.Piece;
import com.example.checkers.core.PossibleMove;
import com.example.checkers.core.State;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PossibleMoveProviderTest {

  public static Stream<Arguments> getStatesWithCaptures() {
    return Stream.of(
        Arguments.of(new State(Set.of(1), Set.of(6, 15), Set.of()), Piece.of(1, DARK), Set.of(10), true),
        Arguments.of(new State(Set.of(10), Set.of(15), Set.of()), Piece.of(10, DARK), Set.of(19), true),
        Arguments.of(new State(Set.of(14, 7), Set.of(17), Set.of()), Piece.of(17, LIGHT), Set.of(10), true),
        Arguments.of(new State(Set.of(7), Set.of(10), Set.of()), Piece.of(10, LIGHT), Set.of(3), true),
        Arguments.of(new State(Set.of(2), Set.of(7), Set.of()), Piece.of(2, DARK), Set.of(11), true),
        Arguments.of(new State(Set.of(26), Set.of(31), Set.of()), Piece.of(31, LIGHT), Set.of(22), true)
    );
  }

  @ParameterizedTest
  @MethodSource("getInputsForEmptyBoard")
  void givenEmptyBoard_shouldProvideValidMoves(Piece piece, Set<Integer> expected) {
    State state = new State(
        piece.isDark() ? Set.of(piece.position()) : Set.of(),
        piece.isLight() ? Set.of(piece.position()) : Set.of(),
        Set.of());
    var actual = new PossibleMoveProviderImpl().getPossibleMovesForPieceInternal(piece, state);
    Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected.stream()
        .map(i -> new PossibleMove(piece, i, false)).collect(Collectors.toSet()));
  }

  @ParameterizedTest
  @MethodSource({"getInputsForStartingGame"})
  void givenStartingGame_shouldProvideValidMoves(Piece piece, Set<Integer> expected) {
    // TODO Make starting state constructor
    var state = new State(
        Checkerboard.getStartingState().getDark(),
        Checkerboard.getStartingState().getLight(),
        Set.of());
    var actual = new PossibleMoveProviderImpl().getPossibleMovesForPieceInternal(piece, state);
    Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected.stream()
        .map(i -> new PossibleMove(piece, i, false)).toList());
  }

  @ParameterizedTest
  @MethodSource({"getStatesWithCaptures"})
  void givenStatesWithCaptures_shouldProvideValidMoves(State state, Piece piece,
                                                       Set<Integer> expected,
                                                       boolean isCapture) {
    var actual = new PossibleMoveProviderImpl().getPossibleMovesForPieceInternal(piece, state);
    Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected.stream()
        .map(i -> new PossibleMove(piece, i, isCapture)).toList());
  }

  @ParameterizedTest
  @MethodSource({"getNoneCaptureStates"})
  void givenNonCaptureStates_shouldProvideValidMoves(State state, Piece piece,
                                                     Set<Integer> expected) {
    var actual = new PossibleMoveProviderImpl().getPossibleMovesForPieceInternal(piece, state);
    Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected.stream()
        .map(i -> new PossibleMove(piece, i, false)).toList());
  }

  @ParameterizedTest
  @MethodSource({"getStatesWithCurvedCaptures"})
  void givenStatesWithCurvedCaptures_shouldProvideValidMoves(State state, Piece piece,
                                                             Set<Integer> expected,
                                                             boolean isCapture) {
    var actual = new PossibleMoveProviderImpl().getPossibleMovesForPieceInternal(piece, state);
    Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected.stream()
        .map(i -> new PossibleMove(piece, i, isCapture)).toList());
  }

  private static Stream<Arguments> getInputsForEmptyBoard() {
    return Stream.of(
        Arguments.of(Piece.of(1, DARK), Set.of(5, 6)),
        Arguments.of(Piece.of(6, DARK), Set.of(9, 10)),
        Arguments.of(Piece.of(12, DARK), Set.of(16)),
        Arguments.of(Piece.of(10, DARK), Set.of(14, 15)),
        Arguments.of(Piece.of(25, LIGHT), Set.of(21, 22)),
        Arguments.of(Piece.of(22, LIGHT), Set.of(17, 18)),
        Arguments.of(Piece.of(27, LIGHT), Set.of(23, 24)),
        Arguments.of(Piece.of(20, LIGHT), Set.of(16))
    );
  }

  private static Stream<Arguments> getInputsForStartingGame() {
    return Stream.of(
        Arguments.of(Piece.of(1, DARK), Set.of()),
        Arguments.of(Piece.of(6, DARK), Set.of()),
        Arguments.of(Piece.of(12, DARK), Set.of(16)),
        Arguments.of(Piece.of(10, DARK), Set.of(14, 15)),
        Arguments.of(Piece.of(25, LIGHT), Set.of()),
        Arguments.of(Piece.of(22, LIGHT), Set.of(17, 18)),
        Arguments.of(Piece.of(27, LIGHT), Set.of()),
        Arguments.of(Piece.of(20, LIGHT), Set.of(16))
    );
  }

  private static Stream<Arguments> getNoneCaptureStates() {
    return Stream.of(
        Arguments.of(new State(Set.of(26), Set.of(31), Set.of()), Piece.of(26, DARK), Set.of(30)),
        Arguments.of(new State(Set.of(2), Set.of(7), Set.of()), Piece.of(7, LIGHT), Set.of(3))
    );
  }

  private static Stream<Arguments> getStatesWithCurvedCaptures() {
    return Stream.of(
        Arguments.of(new State(Set.of(1), Set.of(6, 14), Set.of()), Piece.of(1, DARK), Set.of(10), true),
        Arguments.of(new State(Set.of(22), Set.of(18), Set.of()), Piece.of(22, DARK), Set.of(15), true),
        Arguments.of(new State(Set.of(10), Set.of(14), Set.of()), Piece.of(10, DARK), Set.of(17), true),
        Arguments.of(new State(Set.of(14, 15), Set.of(17), Set.of()), Piece.of(17, LIGHT), Set.of(10), true),
        Arguments.of(new State(Set.of(15), Set.of(10), Set.of()), Piece.of(10, LIGHT), Set.of(19), true)
    );
  }
}
