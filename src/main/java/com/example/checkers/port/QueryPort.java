package com.example.checkers.port;

import com.example.checkers.core.GameEntity;
import com.example.checkers.core.GameId;
import com.example.checkers.core.PlayerEntity;
import com.example.checkers.core.PlayerId;

public interface QueryPort {

  PlayerEntity getPlayerById(PlayerId id);

  GameEntity getGameById(GameId gameId);


}
