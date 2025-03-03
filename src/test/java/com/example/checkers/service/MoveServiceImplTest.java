package com.example.checkers.service;

import com.example.checkers.domain.*;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.MoveResponse;
import com.example.checkers.model.State;
import com.example.checkers.persistence.MoveRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoveServiceImplTest {

    @Mock
    private MoveRepository moveRepository;

    @Spy
    private PossibleMoveProvider possibleMoveProvider;

    @InjectMocks
    private MoveServiceImpl moveService;
    private String gameId = UUID.randomUUID().toString();
    private Game game;
    private Tuple<Player> players;

    @BeforeEach
    void setUp() {
        players = setPlayers();
        game = new Game();
        game.setId(gameId);
        game.setPlayerOne(players.left);
        game.setPlayerTwo(players.right);
        game.setProgress(GameProgress.ONGOING.name());

    }

    private Tuple<Player> setPlayers() {
        var player1 = new Player();
        player1.setId(1L);
        player1.setName("1");
        var player2 = new Player();
        player1.setId(2L);
        player2.setName("2");
        return new Tuple<>(player1, player2);
    }

    @Test
    void givenMoveResultsInCapture_whenMove_returnValidState() {
        when(moveRepository.findAllByGameId(eq(gameId))).thenReturn(
                List.of(
                        new Move(game, players.left, Side.LIGHT.name(), "21-17", "13", "17")
                )
        );

        MoveResponse moveResponse = moveService.generateMoveResponse(gameId, Side.DARK);
        assertEquals("{13=[PossibleMoveSimplified[position=13, destination=22, isCapture=true, " +
                "isTerminal=true]]}", moveResponse.getPossibleMoves().toString());
    }

    @Test
    void givenMoveIsNotTerminal_whenMove_returnSameSidePossibleMoves() {
        when(moveRepository.findAllByGameId(eq(gameId))).thenReturn(
                List.of(
                        new Move(game, players.left, Side.LIGHT.name(), "21-17", "13", "17,18"),
                        new Move(game, players.right, Side.DARK.name(), "13x22", "22", "18")
                )
        );

        MoveResponse moveResponse = moveService.generateMoveResponse(gameId, Side.DARK);
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

    record Tuple<T>(T left, T right) {

    }
}