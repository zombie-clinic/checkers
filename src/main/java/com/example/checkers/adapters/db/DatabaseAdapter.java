package com.example.checkers.adapters.db;

import com.example.checkers.core.GameEntity;
import com.example.checkers.core.GameId;
import com.example.checkers.core.GameProgress;
import com.example.checkers.core.Player;
import com.example.checkers.core.PlayerEntity;
import com.example.checkers.core.PlayerId;
import com.example.checkers.core.State;
import com.example.checkers.core.exception.GameNotFoundException;
import com.example.checkers.port.CommandPort;
import com.example.checkers.port.QueryPort;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
class DatabaseAdapter implements CommandPort, QueryPort {

  private final GameRepository gameRepository;

  private final PlayerRepository playerRepository;

  @Override
  // TODO should it be used only when two players present?
  public void updatePersistedGame(GameEntity game) {
    Optional<Player> player1;
    Optional<Player> player2 = Optional.empty();
    player1 = playerRepository.findById(game.playerOne().id().value());
    if (game.playerTwo() != null) {
      player2 = playerRepository.findById(game.playerTwo().id().value());
    }


    PersistentGame persistentGame = gameRepository.findGameById(game.id()).orElse(null);

    // FIXME possible NPE
    persistentGame.setProgress(game.progress().toString());
    persistentGame.setPlayerOne(player1.orElseThrow(() -> new IllegalStateException("No players")));
    persistentGame.setPlayerTwo(player2.orElse(null));
    // DO NOT DO - do not update starting state here, should be fetched from db and never change
    //    persistentGame.setStartingState(game.startingState());
    gameRepository.save(persistentGame);
  }

  @Override
  public GameId persistNewGame(PlayerId id, State state) {
    var player1 = playerRepository.findById(id.value());
    PersistentGame persistentGame = new PersistentGame();
    persistentGame.setPlayerOne(player1.orElseThrow(() -> new IllegalStateException("No players")));
    persistentGame.setStartingState(getStateString(state));
    // TODO Check - as it is a new game, where to put Lobby?
    persistentGame.setProgress(GameProgress.LOBBY.toString());
    PersistentGame persisted = gameRepository.save(persistentGame);
    return GameId.of(persisted.getId());
  }

  @Override
  public GameEntity getGameById(GameId gameId) {

    var persistentGame = gameRepository.findGameById(gameId.value().toString());
    if (persistentGame.isEmpty()) {
      throw new GameNotFoundException("Game not found: %s".formatted(gameId));
    }

    return new GameEntity(
        persistentGame.get().getId(),
        GameProgress.valueOf(persistentGame.get().getProgress()),
        new PlayerEntity(PlayerId.of(persistentGame.get().getPlayerOne().getId()),
            persistentGame.get().getPlayerOne().getName()),
        persistentGame.get().getPlayerTwo() == null ? null :
            new PlayerEntity(PlayerId.of(persistentGame.get().getPlayerTwo().getId()),
                persistentGame.get().getPlayerTwo().getName()),
        persistentGame.get().getStartingState()
    );
  }

  @Override
  // TODO Should we return optional? Or throw exception? How exceptional if player not found?
  //  draw warning?
  public PlayerEntity getPlayerById(PlayerId id) {
    var player = playerRepository.findById(id.value());
    return new PlayerEntity(PlayerId.of(player.get().getId()), player.get().getName());
  }

  // TODO This abomination needs to go, state persistence needs more elegant solution
  private String getStateString(State state) {
    return
        String.format("{\"dark\":[%s],\"light\":[%s],\"kings\":[%s]}",
            state.getDark().stream().map(String::valueOf).collect(Collectors.joining(",")),
            state.getLight().stream().map(String::valueOf).collect(Collectors.joining(",")),
            state.getKings().stream().map(String::valueOf).collect(Collectors.joining(",")));
  }
}
