package com.example.checkers.service;

import com.example.checkers.model.MoveRequest;
import java.util.UUID;

public interface ValidationService {

  boolean isGameValid(String uuid);

  void lobbyExistsAndPlayerIsDifferent(String gameId, Long playerId);

  boolean existsAndActive(UUID gameId);

  boolean isGameInProgressConsistent(UUID gameId, MoveRequest moveRequest);

}
