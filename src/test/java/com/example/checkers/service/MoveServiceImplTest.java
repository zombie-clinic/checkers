package com.example.checkers.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoveServiceImplTest {

    @Mock
    private MoveRepository moveRepository;

    @Spy
    private BoardService boardService = new BoardServiceImpl();

    @InjectMocks
    private MoveServiceImpl moveService;

    @BeforeEach
    void setUp() {

        var player1 = new Player();
        player1.setId(1L);
        player1.setName("1");

        var player2 = new Player();
        player1.setId(2L);
        player2.setName("2");

        Game game = new Game();
        game.setId("test");
        game.setPlayerOne(player1);
        game.setPlayerTwo(player2);
        game.setProgress(GameProgress.ONGOING.name());
        when(moveRepository.findAllByGameId(anyString())).thenReturn(
                List.of(
                        new Move(game, player1, Side.LIGHT.name(), "21-17", "13", "17")
                )
        );
    }

    @Test
    void givenMoveResultsInCapture_whenMove_returnValidState() {
        MoveResponse moveResponse = moveService.generateMoveResponse("test", Side.DARK);
        assertEquals("{13=[PossibleMoveSimplified[position=13, destination=22, isCapture=true, isTerminal=null]]}", moveResponse.getPossibleMoves().toString());
    }
}