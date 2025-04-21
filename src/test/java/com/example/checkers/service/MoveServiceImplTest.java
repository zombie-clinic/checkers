package com.example.checkers.service;

import com.example.checkers.domain.*;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.MoveResponse;
import com.example.checkers.model.State;
import com.example.checkers.persistence.GameRepository;
import com.example.checkers.persistence.MoveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MoveServiceImplTest {

    @Mock
    private MoveRepository moveRepository;

    @Mock
    private MovesReaderService movesReaderService;

    @Mock
    private TurnService turnService;

    @Mock
    private GameRepository gameRepository;

    @Spy
    private PossibleMoveProvider possibleMoveProvider;

    @InjectMocks
    private MoveServiceImpl moveService;

    private UUID gameId = UUID.randomUUID();
    private Game game;
    private Tuple<Player> players;

    @BeforeEach
    void setUp() {
        players = setPlayers();
        game = new Game();
        game.setId(gameId.toString());
        game.setPlayerOne(players.left);
        game.setPlayerTwo(players.right);
        game.setProgress(GameProgress.ONGOING.name());
        game.setStartingState(
                String.format("{\"dark\":[%s],\"light\":[%s]}",
                        Checkerboard.getStartingState().getDark().stream().map(String::valueOf).collect(Collectors.joining(",")),
                        Checkerboard.getStartingState().getLight().stream().map(String::valueOf).collect(Collectors.joining(",")))
        );

        when(gameRepository.findGameById(anyString())).thenReturn(
                Optional.ofNullable(game)
        );

    }

    private Tuple<Player> setPlayers() {
        var player1 = new Player();
        player1.setId(1L);
        player1.setName("1");
        var player2 = new Player();
        player2.setId(2L);
        player2.setName("2");
        return new Tuple<>(player1, player2);
    }

    @Test
    void givenMoveResultsInCapture_whenMove_returnValidState() {
        when(movesReaderService.getMovesFor(anyString())).thenReturn(
                List.of(
                        MoveRecord.fromMove(new Move(game, players.left, Side.LIGHT.name(), "21-17", "13", "17"))
                )
        );

        // FIXME let's use real method
        when(turnService.getWhichSideToMove(game.getId())).thenReturn(Side.DARK);

        MoveResponse moveResponse = moveService.getNextMoves(gameId);
                assertEquals("{13=[PossibleMoveSimplified[position=13, destination=22, isCapture=true, " +
                "isTerminal=true]]}", moveResponse.getPossibleMoves().toString());
    }

    @Test
    void givenMoveIsNotTerminal_whenMove_returnSameSidePossibleMoves() {
        when(movesReaderService.getMovesFor(anyString())).thenReturn(
                List.of(
                        new MoveRecord(1L, game.getId(), players.left.getId(), Side.LIGHT, "21-17"
                                , "13", "17,18"),
                        new MoveRecord(2L, game.getId(), players.right.getId(), Side.DARK, "13x22"
                                , "22", "18")
                )
        );

        // FIXME let's use real method
        when(turnService.getWhichSideToMove(game.getId())).thenReturn(Side.DARK);

        MoveResponse moveResponse = moveService.getNextMoves(gameId);

        assertEquals("{22=[PossibleMoveSimplified[position=22, destination=15, isCapture=true, " +
                "isTerminal=true]]}", moveResponse.getPossibleMoves().toString());
    }

    @Test
    void givenDarkSide_whenCaptureMove_shouldReturnProperResponse() {

        // case 1
        var currentState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 11, 12, 15, 14),
                List.of(23, 25, 26, 27, 28, 29, 30, 31, 32, 18, 17, 20)
        );

        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setState(currentState);
        moveRequest.setMove("15x22");
        moveRequest.setSide(Side.DARK.name());
        moveRequest.setPlayerId(2L);
        State expectedState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 11, 12, 14, 22),
                List.of(23, 25, 26, 27, 28, 29, 30, 31, 32, 17, 20)
        );
        State actualState = moveService.generateAfterCaptureState(currentState, moveRequest);
        assertEquals(expectedState, actualState);


        // case 2
        currentState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 14),
                List.of(21, 24, 26, 27, 28, 29, 30, 31, 32, 17, 22, 16)
        );

        moveRequest = new MoveRequest();
        moveRequest.setState(currentState);
        moveRequest.setMove("14x21");
        moveRequest.setSide(Side.DARK.name());
        moveRequest.setPlayerId(1L);

        actualState = moveService.generateAfterCaptureState(currentState, moveRequest);
        expectedState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 21),
                List.of(21, 24, 26, 27, 28, 29, 30, 31, 32, 22, 16)
        );
        assertEquals(expectedState, actualState);
    }

    @Test
    void givenMoveIsNotTerminal_whenMove_returnSameSidePossibleMovesButOnlySamePiece() {
        when(movesReaderService.getMovesFor(eq(gameId.toString()))).thenReturn(
                List.of(
                        new MoveRecord(1L, game.getId(), players.left.getId(), Side.LIGHT, "22-18"
                                , "14,15", "18,27"),
                        new MoveRecord(2L, game.getId(), players.right.getId(), Side.DARK, "15x22",
                                "14,22", "27")
                )
        );

        // FIXME let's use real method
        when(turnService.getWhichSideToMove(game.getId())).thenReturn(Side.LIGHT);

        Map<Integer, List<PossibleMove>> actual =
                (Map<Integer, List<PossibleMove>>) moveService.getNextMoves(gameId)
                        .getPossibleMoves();

        Map<Integer, List<PossibleMoveSimplified>> expected = new HashMap<>();
        expected.put(27, List.of(
                new PossibleMoveSimplified(27, 24, false, true),
                new PossibleMoveSimplified(27, 23, false, true)));

        assertThat(expected.toString()).endsWith(actual.toString());
    }

    record Tuple<T>(T left, T right) {

    }
}