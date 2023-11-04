package com.example.checkers.service;

import com.example.checkers.domain.*;
import com.example.checkers.model.GameResponse;
import com.example.checkers.model.MoveResponse;
import com.example.checkers.persistence.GameRepository;
import com.example.checkers.persistence.MoveRepository;
import com.example.checkers.persistence.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.checkers.domain.Checkerboard.getStartingState;
import static com.example.checkers.domain.GameProgress.LOBBY;


@RequiredArgsConstructor
@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    private final MoveRepository moveRepository;

    private final PlayerRepository playerRepository;

    private final BoardService boardService;

    private final MoveService moveService;

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
    public MoveResponse startGame(String gameId, Long playerTwoId) {
        Player playerTwo = validateAndGet(playerTwoId);
        Game game = validateAndGetGame(gameId);
        if (game.getPlayerOne() == null) {
            throw new IllegalStateException(STR. "Invalid game, there is no player one in the lobby: \{ gameId }" );
        }

        game.setPlayerTwo(playerTwo);
        game.setProgress(GameProgress.STARTING.toString());

        return new MoveResponse(gameId,
                Checkerboard.getStartingState(),
                boardService.getPossibleMoves(Side.LIGHT, Checkerboard.getStartingState()));
    }

    private Game validateAndGetGame(String gameId) {
        return gameRepository.findGameById(gameId).orElseThrow(
                () -> new IllegalArgumentException(STR. "No such game: \{ gameId }" )
        );
    }

    private Player validateAndGet(Long playerId) {
        Optional<Player> player = playerRepository.findById(playerId);
        if (player.isEmpty()) {
            throw new IllegalArgumentException("No such player, couldn't start game.");
        }
        return player.get();
    }

    private MoveResponse generateFirstMoveResponse(String gameId) {
        var response = new MoveResponse();
        response.setGameId(gameId);
        response.setState(getStartingState());
        response.setPossibleMoves(boardService.getPossibleMoves(Side.LIGHT, Checkerboard.getStartingState()));
        return response;
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
            throw new IllegalArgumentException(STR. "There is no lobby for game : \{ gameId }" );
        }
        if (Objects.equals(game.get().getPlayerOne().getId(), playerTwoId)) {
            throw new IllegalArgumentException(STR. "The game \{ gameId } already has player \{ playerTwoId } " );
        }
    }
}
