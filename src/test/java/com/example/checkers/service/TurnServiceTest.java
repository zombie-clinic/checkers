package com.example.checkers.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.MoveRecord;
import com.example.checkers.domain.Side;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TurnServiceTest {

  @Mock
  private MovesReaderService movemovesReaderService;

  @Mock
  private PossibleMoveProviderImpl possibleMoveProvider;

  @InjectMocks
  private TurnService turnService;

  @ParameterizedTest
  @MethodSource("getGames")
  void getNextToMoveSide(String gameId, List<MoveRecord> moveList, Side expectedSide) {
    BDDMockito.given(movemovesReaderService.getMovesFor(gameId)).willReturn(moveList);
    Side nextToMoveSide = turnService.getWhichSideToMove(gameId);
    assertThat(nextToMoveSide).isEqualTo(expectedSide);
  }

  private static Stream<Arguments> getGames() {
    // Store gameId in a variable in order to pass to a MoveRecord
    // However, it doesn't matter if it's invalid, since reinitialization occur every test run
    String gameId = UUID.randomUUID().toString();
    return Stream.of(
        Arguments.of(gameId, List.of(), Side.LIGHT),
        Arguments.of(gameId,
            List.of(
                new MoveRecord(1L, gameId, 1L, Side.LIGHT, "21-17",
                    Checkerboard.getStartingState().getDark(), Checkerboard.getStartingState().getLight(),
                    List.of())
            ), Side.DARK),
        Arguments.of(gameId,
            List.of(
                new MoveRecord(1L, gameId, 1L, Side.LIGHT, "22-18", "14,15", "18,27", List.of()),
                new MoveRecord(2L, gameId, 2L, Side.DARK, "15x22", "14,22", "27", List.of())
            ), Side.LIGHT)
    );
  }
}
