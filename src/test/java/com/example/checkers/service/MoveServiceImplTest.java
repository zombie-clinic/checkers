package com.example.checkers.service;

import com.example.checkers.domain.*;
import com.example.checkers.model.MoveResponse;
import com.example.checkers.persistence.MoveRepository;
import org.junit.jupiter.api.BeforeEach;
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

    record Tuple<T>(T left, T right ) {

    }

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
        assertEquals("{22=[PossibleMoveSimplified[position=22, destination=15, isCapture=true, isTerminal=true]]}", moveResponse.getPossibleMoves().toString());
    }
}