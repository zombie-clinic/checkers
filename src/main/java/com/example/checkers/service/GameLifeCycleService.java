package com.example.checkers.service;

import com.example.checkers.core.Checkerboard;
import com.example.checkers.core.GameEntity;
import com.example.checkers.core.GameId;
import com.example.checkers.core.GameProgress;
import com.example.checkers.core.PlayerEntity;
import com.example.checkers.core.PlayerId;
import com.example.checkers.core.Side;
import com.example.checkers.core.State;
import com.example.checkers.core.UseCaseInteractor;
import com.example.checkers.port.CommandPort;
import com.example.checkers.port.QueryPort;
import com.example.checkers.usecase.UpdateGameLifeCycleUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@UseCaseInteractor
class GameLifeCycleService implements UpdateGameLifeCycleUseCase {

  private final CommandPort commandPort;
  private final QueryPort queryPort;

  // TODO where to validate incoming request? Including validations making calls to db,
  //  or rather instead let's throw and exception and handle it in GlobalHandler,
  //  since it's a fetch of a concrete player by id
  @Override
  public GameId createNewGameLobby(PlayerId player1, Side side) {
    PlayerEntity player = queryPort.getPlayerById(player1);
    return commandPort.persistNewGame(player.id(), Checkerboard.getStartingState());
  }

  @Override
  public GameId createImportedGameLobby(PlayerId playerId, Side side, State importedState) {
    PlayerEntity player = queryPort.getPlayerById(playerId);
    return commandPort.persistNewGame(player.id(), importedState);
  }

  @Override
  @Transactional
  public void startGame(GameId gameId, PlayerId player2) {
    GameEntity game = queryPort.getGameById(gameId);
    if (game.playerOne() == null) {
      throw new IllegalStateException(
          "Invalid game, there is no player one in the lobby: " + "%s".formatted(gameId));
    }
    PlayerEntity player = queryPort.getPlayerById(player2);
    GameEntity newGame = game.withPlayerTwo(player).withProgress(GameProgress.STARTING);
    commandPort.updatePersistedGame(newGame);
  }

  @Override
  public void archiveGame(GameId gameId) {
    GameEntity gameById = queryPort.getGameById(gameId);
    GameEntity gameEntity = gameById.withProgress(GameProgress.ARCHIVED);
    commandPort.updatePersistedGame(gameEntity);
  }
}


