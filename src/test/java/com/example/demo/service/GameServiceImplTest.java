package com.example.demo.service;

import com.example.demo.domain.GameProgress;
import com.example.demo.domain.GameResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class GameServiceImplTest {

    @Autowired
    private GameService gameService;

    @Test
    void givenNewGame_whenStarted_shouldHaveStartingStatus() {
        GameResponse gameResponse = gameService.startGame();
        assertNotNull(gameResponse.getProgress());
        assertEquals(gameResponse.getProgress(), GameProgress.STARTING.toString());
    }
}