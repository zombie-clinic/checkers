package com.example.demo.service;

import com.example.demo.domain.MoveRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.demo.service.StateCalculator.calculateNextState;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StateCalculatorTest {

    private MoveRequest moveRequest;

    @BeforeEach
    void setup() {
        moveRequest = new MoveRequest();
        moveRequest.setSide("white");
        moveRequest.setMove("11-18");
        moveRequest.setState("initial state");
    }

    @Test
    void getNextState_shouldThrowExceptionIfInputNull() {
        assertThrows(IllegalArgumentException.class, () -> calculateNextState(null));
    }

    @Test
    void getNextState_shouldThrowExceptionIfResultNull() {
        assertThrows(IllegalStateException.class, () -> calculateNextState(generateInvalidMoveRequest()));
    }

    @Test
    void getNextState_shouldCalculateNextState1stMove() throws JsonProcessingException {
        MoveRequest moveRequest = generateWhiteMoveRequest();
        State actual = calculateNextState(moveRequest);
        var objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(actual.toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        assertThat(objectMapper.writeValueAsString(jsonNode)).isEqualTo("{\"black\":[1,2,3,4,5,6,7,8,9,10,11,12],\"white\":[21,23,24,25,26,27,28,29,30,31,32,18]}"
        );
    }

    @Test
    void getNextState_shouldCalculateNextState2ndMove() throws JsonProcessingException {
        MoveRequest moveRequest = generateBlackMoveRequest();
        State actual = calculateNextState(moveRequest);
        var objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(actual.toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        assertThat(objectMapper.writeValueAsString(jsonNode)).isEqualTo("{\"black\":[1,2,3,4,5,6,7,8,9,11,12,15],\"white\":[21,23,24,25,26,27,28,29,30,31,32,18]}"
        );
    }

    private static MoveRequest generateBlackMoveRequest() {
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setState("{\"black\": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]," +
                "\"white\":[21, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 18]}");
        moveRequest.setSide("black");
        moveRequest.setMove("10-15");
        return moveRequest;
    }

    private static MoveRequest generateWhiteMoveRequest() {
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setState("{\"black\": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]," +
                "\"white\":[21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32]}");
        moveRequest.setSide("white");
        moveRequest.setMove("22-18");
        return moveRequest;
    }

    private static MoveRequest generateInvalidMoveRequest() {
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setState("""
                {
                  "black": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 1
                  2],
                  "white": 1, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32]
                                
                """);
        moveRequest.setSide("white");
        moveRequest.setMove("22-18");
        return moveRequest;
    }
}