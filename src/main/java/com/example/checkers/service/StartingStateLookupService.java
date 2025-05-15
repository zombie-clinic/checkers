package com.example.checkers.service;

import static com.example.checkers.service.StateUtils.fromJsonNodeIteratorToSet;

import com.example.checkers.domain.Game;
import com.example.checkers.domain.State;
import com.example.checkers.persistence.GameRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
        fromJsonNodeIteratorToSet(startingState.get("dark").elements()),
        fromJsonNodeIteratorToSet(startingState.get("light").elements()),
        fromJsonNodeIteratorToSet(startingState.get("kings") == null ? Collections.emptyIterator()
            : startingState.get("kings").elements())
    );
  }
}
