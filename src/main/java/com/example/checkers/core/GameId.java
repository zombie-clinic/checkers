package com.example.checkers.core;

import java.util.UUID;

public record GameId(UUID value) {

  public static GameId of(String gameId) {
    return new GameId(UUID.fromString(gameId));
  }
}
