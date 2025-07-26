package com.example.checkers.service;

import static com.example.checkers.service.StateUtils.getStateFromMoveList;

import com.example.checkers.adapters.db.GameRepository;
import com.example.checkers.adapters.db.PersistentGame;
import com.example.checkers.adapters.db.PlayerRepository;
import com.example.checkers.core.GameProgress;
import com.example.checkers.core.MoveRecord;
import com.example.checkers.core.Player;
import com.example.checkers.core.State;
import com.example.checkers.model.GameResponse;
import com.example.checkers.model.MoveRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService, QueryService {


  private final GameRepository gameRepository;
  private final PlayerRepository playerRepository;
  private final MovesReaderService movesReaderService;
  private final StartingStateLookupService startingStateLookupService;

  @Override
  public boolean existsAndActive(UUID gameId) {
    Optional<PersistentGame> game = gameRepository.findGameById(gameId.toString());
    return game.isPresent();
    // fixme
//        return game.map(value ->
//                !List.of(GameProgress.FINISHED, GameProgress.ARCHIVED)
//                        .contains(value.getProgress())).orElse(false);
  }

  @Override
  public boolean isGameInProgressConsistent(UUID gameId, MoveRequest moveRequest) {
    State clientState = State.from(moveRequest.getClientState());
    List<MoveRecord> moves = movesReaderService.getMovesFor(gameId.toString());
    State serverState;
    if (moves.isEmpty()) {
      serverState = startingStateLookupService.getStateFromStartingStateString(gameId);
    } else {
      serverState = getStateFromMoveList(moves);
    }
    return amountOfFiguresMatches(clientState, serverState) && positionsMatch(clientState,
        serverState);
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

    List<PersistentGame> games = gameRepository.findAllByProgressIn(progressList);

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
    // FIXME possible moves currently set up in GameController
    Optional<PersistentGame> game = gameRepository.findGameById(uuid);
    GameResponse gameResponse = new GameResponse();
    gameResponse.setGameId(game.get().getId());
    gameResponse.setProgress(game.get().getProgress());
    gameResponse.setStartingState(game.get().getStartingState());
    return gameResponse;
  }

  @Override
  public boolean isGameValid(String uuid) {
    return gameRepository.existsById(uuid);
  }

  @Override
  public void lobbyExistsAndPlayerIsDifferent(String gameId, Long playerTwoId) {
    Optional<PersistentGame> game = gameRepository.findGameById(gameId);
    if (game.isEmpty()) {
      throw new IllegalArgumentException("There is no lobby for game : %s".formatted(gameId));
    }
    if (Objects.equals(game.get().getPlayerOne().getId(), playerTwoId)) {
      throw new IllegalArgumentException(
          "The game %s already has player %d".formatted(gameId, playerTwoId));
    }
  }

  private PersistentGame validateAndGetGame(String gameId) {
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
    return new HashSet<>(serverState.getDark()).containsAll(clientState.getDark()) &&
        new HashSet<>(serverState.getLight()).containsAll(clientState.getLight());
  }

  private static boolean amountOfFiguresMatches(State requestedCheck, State current) {
    return requestedCheck.getDark().size() == current.getDark().size() &&
        requestedCheck.getLight().size() == current.getLight().size();
  }
}
