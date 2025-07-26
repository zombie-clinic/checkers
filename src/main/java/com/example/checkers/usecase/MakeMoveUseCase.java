package com.example.checkers.usecase;

import com.example.checkers.core.GameId;
import com.example.checkers.core.MoveCommand;
import com.example.checkers.core.PlayerId;

public interface MakeMoveUseCase {

  void makeMove(GameId gameId, PlayerId playerId, MoveCommand moveCommand);
}
