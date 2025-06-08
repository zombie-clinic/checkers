package com.example.checkers.service;

import com.example.checkers.domain.State;
import com.example.checkers.model.GameResponse;
import com.example.checkers.model.MoveRequest;
import java.util.List;
import java.util.UUID;

public interface GameStateService {

  GameResponse startLobby(Long playerOneId, String side);

  GameResponse startGame(String gameId, Long playerTwoId);

  List<GameResponse> getGamesByProgress(List<String> progressList);

  GameResponse getGameById(String uuid);

  boolean isGameValid(String uuid);

  void lobbyExistsAndPlayerIsDifferent(String gameId, Long playerId);

  boolean existsAndActive(UUID gameId);

  boolean isGameInProgressConsistent(UUID gameId, MoveRequest moveRequest);

  GameResponse startImportedGameLobby(Long playerId, String side, State state);
}
