package com.example.checkers.service;

import static com.example.checkers.domain.Side.LIGHT;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.checkers.domain.Piece;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.Side;
import com.example.checkers.domain.State;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KingsPossibleMovesTest {

  // FIXME State change doesn't make sense in a stateless class (PossibleMoveProvider)
  @Test
  void givenTwoPiecesToCapture_shouldPerformChainCapture() {
    var moveProvider = new PossibleMoveProviderImpl();

    State state1 = getTestState(Set.of(25), Set.of(18, 11), Set.of(25));
    var actual1 = moveProvider.getPossibleMovesForPieceInternal(Piece.of(25, Side.DARK), state1);
    List<PossibleMove> expected1 = Stream.of(15)
        .map(i -> new PossibleMove(Piece.of(25, Side.DARK), i, true))
        .toList();
    assertThat(actual1).containsExactlyInAnyOrderElementsOf(expected1);

    State state2 = getTestState(Set.of(15), Set.of(11), Set.of(15));
    var actual2 = moveProvider.getPossibleMovesForPieceInternal(Piece.of(15, Side.DARK), state2);
    List<PossibleMove> expected2 = Stream.of(8, 4)
        .map(i -> new PossibleMove(Piece.of(15, Side.DARK), i, true))
        .toList();
    assertThat(actual2).containsExactlyInAnyOrderElementsOf(expected2);
  }

  @ParameterizedTest
  @MethodSource({"getKingsPositions"})
  void givenStatesWithCurvedCaptures_shouldProvideValidMoves(State state, Piece piece,
                                                             Set<Integer> destList,
                                                             boolean isCapture) {
    var moveProvider = new PossibleMoveProviderImpl();
    var actual = moveProvider.getPossibleMovesForPieceInternal(piece, state);
    List<PossibleMove> pmList = destList.stream()
        .map(i -> new PossibleMove(piece, i, isCapture))
        .toList();
    assertThat(actual).containsExactlyInAnyOrderElementsOf(pmList);
  }


  private static Stream<Arguments> getKingsPositions() {
    return Stream.of(
        Arguments.of(getTestState(Set.of(10, 19), Set.of(1), Set.of(1)), Piece.of(1, LIGHT), Set.of(15), true),
        Arguments.of(getTestState(Set.of(19), Set.of(15), Set.of(15)), Piece.of(15, LIGHT), Set.of(24, 28), true)
    );
  }

  static State getTestState(Set<Integer> black, Set<Integer> light, Set<Integer> kings) {
    return new State(black, light, kings);
  }
}
