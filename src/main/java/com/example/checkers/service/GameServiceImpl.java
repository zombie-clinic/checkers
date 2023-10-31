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
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.checkers.domain.Board.getInitialState;


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
    public MoveResponse startGame(Long playerId, String side) {
        Player player = validateAndGet(playerId);

        Game startingGame = Game.builder()
                .player(player)
                .build();

        Game savedGame = gameRepository.save(startingGame);
        return moveService.generateMoveResponse(savedGame.getId(), Side.valueOf(side));
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
        response.setState(getInitialState());
        response.setPossibleMoves(boardService.getPossibleMoves(Side.WHITE, Board.getInitialState()));
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
}
