package com.example.checkers.service;

import com.example.checkers.domain.Game;
import com.example.checkers.model.State;
import com.example.checkers.persistence.GameRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@RequiredArgsConstructor
@Component
public class StartingStateLookupService {

    private final GameRepository gameRepository;

    State getStateFromStartingStateString(UUID gameId) {
        Optional<Game> currentGame = gameRepository.findGameById(gameId.toString());
        JsonNode startingState = null;
        try {
            startingState = new ObjectMapper().readTree(currentGame.get().getStartingState());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
        return new State(
                fromIteratorToList(startingState.get("dark").elements()),
                fromIteratorToList(startingState.get("light").elements())
        );
    }

    private List<Integer> fromIteratorToList(Iterator<JsonNode> elements) {
        List<Integer> list = new ArrayList<>();
        elements.forEachRemaining(e -> list.add(e.intValue()));
        return list;
    }
}
