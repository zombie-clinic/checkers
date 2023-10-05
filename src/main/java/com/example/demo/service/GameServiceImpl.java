package com.example.demo.service;

import com.example.demo.GameRepository;
import com.example.demo.GameStatus;
import com.example.demo.domain.GameResponse;
import com.example.demo.domain.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;


    @Override
    public GameResponse startGame() {
        Game game = new Game();
        game.setStatus(GameStatus.STARTING.toString());
        Game savedGame = gameRepository.save(game);
        var gameResponse = new GameResponse();
        gameResponse.setId(savedGame.getId());
        gameResponse.setStatus(GameStatus.STARTING.toString());
        return gameResponse;
    }

    @Override
    public List<GameResponse> getGamesByStatus(String gameStatus) {

        // validate
        GameStatus.valueOf(gameStatus);

        List<Game> games = gameRepository.findGameByStatus(gameStatus);

        return games.stream()
                .map(game -> {
                    var gameResponse = new GameResponse();
                    gameResponse.setId(game.getId());
                    gameResponse.setStatus(game.getStatus());
                    return gameResponse;
                })
                .collect(Collectors.toList());
    }

    @Override
    public GameResponse getGameById(String uuid) {
        Optional<Game> game = gameRepository.findGameById(uuid);
        if (game.isEmpty()) {
            return null;
        }

        GameResponse gameResponse = new GameResponse();
        gameResponse.setId(game.get().getId());
        gameResponse.setStatus(game.get().getStatus());

        return gameResponse;
    }
}
