package com.example.checkers.domain;

import java.util.List;
import java.util.stream.Collectors;

public record MoveRecord(
    // fixme move id should be related to the game but not to the database, we need two
    //  separate fields
    Long moveId,
    String gameId,
    Long playerId,
    Side side,
    String move,
    String dark,
    String light) {

  public MoveRecord(Long moveId,
                    String gameId,
                    Long playerId,
                    Side side,
                    String move,
                    List<Integer> dark,
                    List<Integer> light) {
    this(moveId, gameId, playerId, side, move,
        dark.stream().map(String::valueOf).collect(Collectors.joining(",")),
        light.stream().map(String::valueOf).collect(Collectors.joining(",")));
  }

  public static MoveRecord fromMove(Move move) {
    return new MoveRecord(
        move.getId(),
        move.getGame().getId(),
        move.getPlayer().getId(),
        Side.valueOf(move.getSide()),
        move.getMove(),
        move.getDark(),
        move.getLight());
  }
}
