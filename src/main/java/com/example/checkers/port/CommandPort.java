package com.example.checkers.port;

import com.example.checkers.core.GameEntity;
import com.example.checkers.core.GameId;
import com.example.checkers.core.PlayerId;
import com.example.checkers.core.State;

public interface CommandPort {

  GameId persistNewGame(PlayerId id, State state);

  void updatePersistedGame(GameEntity game);
}
