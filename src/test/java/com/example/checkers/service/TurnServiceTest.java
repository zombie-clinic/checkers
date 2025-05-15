package com.example.checkers.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.MoveRecord;
import com.example.checkers.domain.Side;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TurnServiceTest {

  @Mock
  private MovesReaderService movesReaderService;

  @Spy
  private PossibleMoveProvider possibleMoveProvider = new PossibleMoveProviderImpl();

  @InjectMocks
  private TurnService turnService;

  @ParameterizedTest
  @MethodSource("getGames")
  void getNextToMoveSide(String gameId, List<MoveRecord> moveList, Side expectedSide) {
    BDDMockito.given(movesReaderService.getMovesFor(gameId)).willReturn(moveList);
    Side nextToMoveSide = turnService.getWhichSideToMove(gameId);
    assertThat(nextToMoveSide).isEqualTo(expectedSide);
  }

  private static Stream<Arguments> getGames() {
    String gameId1 = UUID.randomUUID().toString();
    String gameId2 = UUID.randomUUID().toString();
    String gameId3 = UUID.randomUUID().toString();
    return Stream.of(
        Arguments.of(gameId1, List.of(), Side.LIGHT),
        Arguments.of(gameId2,
            List.of(
                new MoveRecord(1L, gameId2, 1L, Side.LIGHT, "21-17",
                    Checkerboard.getStartingState().getDark(), Checkerboard.getStartingState().getLight(),
                    Set.of())
            ), Side.DARK),
        Arguments.of(gameId3,
            List.of(
                new MoveRecord(1L, gameId3, 1L, Side.LIGHT, "22-18", "14,15", "18,27", Set.of()),
                new MoveRecord(2L, gameId3, 2L, Side.DARK, "15x22", "14,22", "27", Set.of())
            ), Side.LIGHT)
    );
  }
}
