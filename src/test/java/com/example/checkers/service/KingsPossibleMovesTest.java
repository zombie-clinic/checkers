package com.example.checkers.service;

import static com.example.checkers.domain.Side.LIGHT;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.checkers.domain.Piece;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.model.State;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class KingsPossibleMovesTest {

  @ParameterizedTest
  @MethodSource({"getKingsPositions"})
  void givenStatesWithCurvedCaptures_shouldProvideValidMoves(State state, Piece piece,
                                                             List<Integer> destList,
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
        Arguments.of(getTestState(List.of(10, 19), List.of(1), List.of(1)),
            Piece.of(1, LIGHT), List.of(15), true),
        Arguments.of(getTestState(List.of(19), List.of(15), List.of(15)),
            Piece.of(15, LIGHT), List.of(24, 28), true)
    );
  }

  static State getTestState(List<Integer> black, List<Integer> light, List<Integer> kings) {
    var state = new State(black, light);
    state.setKings(kings);
    return state;
  }
}
