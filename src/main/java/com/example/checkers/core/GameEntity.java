package com.example.checkers.core;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.With;

@With
// TODO Rename to Game
public record GameEntity(String id,
                         GameProgress progress,
                         PlayerEntity playerOne,
                         PlayerEntity playerTwo,
                         String startingState) {

  public GameEntity {
    // FIXME Eliminate this abomination
    // TODO Mind this is invoked on every GameEntity creation
    startingState = String.format("{\"dark\":[%s],\"light\":[%s],\"kings\":[%s]}",
        Checkerboard.getStartingState().getDark().stream().map(String::valueOf).collect(Collectors.joining(",")),
        Checkerboard.getStartingState().getLight().stream().map(String::valueOf).collect(Collectors.joining(",")),
        Stream.of().map(String::valueOf).collect(Collectors.joining(",")));
  }
}
