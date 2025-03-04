package com.example.checkers.service;

import com.example.checkers.domain.*;
import com.example.checkers.model.GameResponse;
import com.example.checkers.model.State;
import com.example.checkers.persistence.GameRepository;
import com.example.checkers.persistence.MoveRepository;
import com.example.checkers.persistence.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.checkers.domain.GameProgress.LOBBY;


@Slf4j
@RequiredArgsConstructor
@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    private final MoveRepository moveRepository;

    private final PlayerRepository playerRepository;

    private final PossibleMoveProvider provider;

    @Transactional
    @Override
    public GameResponse startLobby(Long playerId, String side) {
        Player player = validateAndGet(playerId);

        Game lobbyGame = Game.builder()
                .playerOne(player)
                .build();

        Game savedGame = gameRepository.save(lobbyGame);
        return new GameResponse(savedGame.getId(), LOBBY.toString());
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

        return new GameResponse(gameId, GameProgress.STARTING.toString());
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
        return gameRepository.findGameById(uuid)
                .map(game -> new GameResponse(game.getId(), game.getProgress()))
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

    @Override
    public Side getCurrentSide(String gameId) {
        List<Move> moves = moveRepository.findAllByGameId(gameId);
        Optional<Long> lastMoveId = moves.stream().map(Move::getId).max(Comparator.naturalOrder());
        if (lastMoveId.isEmpty()) {
            return Side.LIGHT;
        }
        Move lastMove = moves.getLast();

        State currentState = MoveServiceImpl.getCurrentState(moves);
        Map<Integer, List<PossibleMove>> possibleMovesMap = provider.getPossibleMovesMap(
                Side.valueOf(lastMove.getSide()),
                new Checkerboard(currentState.getDark(),
                        currentState.getLight()
                ));
        List<PossibleMove> filtered = possibleMovesMap.entrySet().stream()
                .flatMap(p -> p.getValue().stream())
                .filter(p -> p.isCapture() && !p.isTerminal())
                .toList();

        if (!filtered.isEmpty()) {
            return Side.valueOf(lastMove.getSide());
        }

        return Side.valueOf(lastMove.getSide()) == Side.LIGHT ? Side.DARK : Side.LIGHT;
    }
}
