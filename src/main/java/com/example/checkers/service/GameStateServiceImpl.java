package com.example.checkers.service;

import static com.example.checkers.domain.GameProgress.LOBBY;
import static com.example.checkers.service.MoveServiceImpl.getCurrentState;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.Game;
import com.example.checkers.domain.GameProgress;
import com.example.checkers.domain.MoveRecord;
import com.example.checkers.domain.Player;
import com.example.checkers.model.GameResponse;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.State;
import com.example.checkers.persistence.GameRepository;
import com.example.checkers.persistence.PlayerRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@RequiredArgsConstructor
@Service
public class GameStateServiceImpl implements GameStateService {

  private final GameRepository gameRepository;

  private final MovesReaderService movesReaderService;

  private final MoveService moveService;

  private final PlayerRepository playerRepository;

  private final StartingStateLookupService startingStateLookupService;

  @Override
  public boolean existsAndActive(UUID gameId) {
    Optional<Game> game = gameRepository.findGameById(gameId.toString());
    return game.isPresent();
    // fixme
//        return game.map(value ->
//                !List.of(GameProgress.FINISHED, GameProgress.ARCHIVED)
//                        .contains(value.getProgress())).orElse(false);
  }

  @Override
  public boolean isGameInProgressConsistent(UUID gameId, MoveRequest moveRequest) {
    State clientState = moveRequest.getState();
    List<MoveRecord> moves = movesReaderService.getMovesFor(gameId.toString());
    State serverState;
    if (moves.isEmpty()) {
      serverState = startingStateLookupService.getStateFromStartingStateString(gameId);
    } else {
      serverState = getCurrentState(moves);
    }
    return amountOfFiguresMatches(clientState, serverState) && positionsMatch(clientState,
        serverState);
  }

  @Transactional
  @Override
  public GameResponse startLobby(Long playerId, String side) {
    Player player = validateAndGet(playerId);

    Game lobbyGame = Game.builder()
        .playerOne(player)
        .startingState(
            String.format("{\"dark\":[%s],\"light\":[%s]}",
                Checkerboard.getStartingState().getDark().stream().map(String::valueOf).collect(Collectors.joining(",")),
                Checkerboard.getStartingState().getLight().stream().map(String::valueOf).collect(Collectors.joining(","))

            ))
        .build();

    Game savedGame = gameRepository.save(lobbyGame);
    GameResponse gameResponse = new GameResponse(savedGame.getId(), LOBBY.toString(), savedGame.getStartingState(),
        moveService.getNextMoves(UUID.fromString(savedGame.getId())));
    gameResponse.setPossibleMoves(
        moveService.getNextMoves(UUID.fromString(savedGame.getId())).getPossibleMoves()
    );
    return gameResponse;
  }

  @Transactional
  @Override
  public GameResponse startImportedGameLobby(Long playerId, String side, State state) {
    Player player = validateAndGet(playerId);

    Game lobbyGame = Game.builder()
        .playerOne(player)
        .startingState(
            String.format("{\"dark\":[%s],\"light\":[%s]}",
                state.getDark().stream().map(String::valueOf).collect(Collectors.joining(",")),
                state.getLight().stream().map(String::valueOf).collect(Collectors.joining(","))
            ))
        .build();

    Game savedGame = gameRepository.save(lobbyGame);
    GameResponse gameResponse = new GameResponse(savedGame.getId(), LOBBY.toString(), savedGame.getStartingState(),
        moveService.getNextMoves(UUID.fromString(savedGame.getId())));
    gameResponse.setPossibleMoves(
        moveService.getNextMoves(UUID.fromString(savedGame.getId())).getPossibleMoves()
    );
    return gameResponse;
  }

  @Transactional
  @Override
  public GameResponse startGame(String gameId, Long playerTwoId) {
    Player playerTwo = validateAndGet(playerTwoId);
    Game game = validateAndGetGame(gameId);
    if (game.getPlayerOne() == null) {
      throw new IllegalStateException(("Invalid game, there is no player one in the lobby: " +
          "%s").formatted(gameId));
    }

    game.setPlayerTwo(playerTwo);
    game.setProgress(GameProgress.STARTING.toString());

    return new GameResponse(gameId, GameProgress.STARTING.toString(), game.getStartingState(), moveService.getNextMoves(
        UUID.fromString(gameId)
    ));
  }

  @Override
  public List<GameResponse> getGamesByProgress(List<String> progressList) {
    if (progressList.isEmpty()) {
      return gameRepository.findAll().stream()
          .map(game -> {
            var gameResponse = new GameResponse();
            gameResponse.setGameId(game.getId());
            gameResponse.setProgress(game.getProgress());
            return gameResponse;
          })
          .collect(Collectors.toList());
    }
    // validate states
    progressList.forEach(GameProgress::valueOf);

    List<Game> games = gameRepository.findAllByProgressIn(progressList);

    return games.stream()
        .map(game -> {
          var gameResponse = new GameResponse();
          gameResponse.setGameId(game.getId());
          gameResponse.setProgress(game.getProgress());
          return gameResponse;
        })
        .collect(Collectors.toList());
  }

  @Override
  public GameResponse getGameById(String uuid) {
    // fixme possible moves
    return gameRepository.findGameById(uuid)
        .map(game -> new GameResponse(game.getId(), game.getProgress(), game.getStartingState(), List.of()))
        .orElse(null);
  }

  @Override
  public boolean isGameValid(String uuid) {
    return gameRepository.existsById(uuid);
  }

  @Override
  public void lobbyExistsAndPlayerIsDifferent(String gameId, Long playerTwoId) {
    Optional<Game> game = gameRepository.findGameById(gameId);
    if (game.isEmpty()) {
      throw new IllegalArgumentException("There is no lobby for game : %s".formatted(gameId));
    }
    if (Objects.equals(game.get().getPlayerOne().getId(), playerTwoId)) {
      throw new IllegalArgumentException("The game %s already has player %d".formatted(gameId, playerTwoId));
    }
  }

  private Game validateAndGetGame(String gameId) {
    return gameRepository.findGameById(gameId).orElseThrow(
        () -> new IllegalArgumentException("No such game: %s".formatted(gameId))
    );
  }

  private Player validateAndGet(Long playerId) {
    Optional<Player> player = playerRepository.findById(playerId);
    if (player.isEmpty()) {
      throw new IllegalArgumentException("No such player, couldn't start game.");
    }
    return player.get();
  }

  private static boolean positionsMatch(State clientState, State serverState) {
    return new HashSet<>(serverState.getDark()).containsAll(clientState.getDark()) && new HashSet<>(serverState.getLight()).containsAll(clientState.getLight());
  }

  private static boolean amountOfFiguresMatches(State requestedCheck, State current) {
    return requestedCheck.getDark().size() == current.getDark().size() && requestedCheck.getLight().size() == current.getLight().size();
  }
}
