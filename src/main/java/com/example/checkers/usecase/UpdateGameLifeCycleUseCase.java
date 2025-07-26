package com.example.checkers.usecase;

import com.example.checkers.core.GameId;
import com.example.checkers.core.PlayerId;
import com.example.checkers.core.Side;
import com.example.checkers.core.State;

public interface UpdateGameLifeCycleUseCase {

  GameId createNewGameLobby(PlayerId player1, Side side);

  GameId createImportedGameLobby(PlayerId player1, Side side, State clientState);

  void startGame(GameId gameId, PlayerId player2);

  void archiveGame(GameId gameId);
}
