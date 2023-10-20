package com.example.demo.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoardServiceTest {

    @Test
    void shouldReturnValidBoard() {
        assertEquals(64, Arrays.stream(new BoardServiceImpl().getBoard()).flatMap(
                Arrays::stream
        ).toList().size());
    }

    @Test
    void shouldReturnProperAmountOfCells() {
        assertEquals(32, new BoardServiceImpl().getValidCells().size());
    }
}