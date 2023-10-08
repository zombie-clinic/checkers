package com.example.demo.service;

import com.example.demo.GameProgress;
import com.example.demo.GameRepository;
import com.example.demo.MoveRepository;
import com.example.demo.domain.Game;
import com.example.demo.domain.GameResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    private final MoveRepository moveRepository;

    @Override
    public GameResponse startGame() {
        Game game = new Game();
        game.setProgress(GameProgress.STARTING.toString());
        Game savedGame = gameRepository.save(game);
        var gameResponse = new GameResponse();
        gameResponse.setGameId(savedGame.getId());
        gameResponse.setProgress(GameProgress.STARTING.toString());
        return gameResponse;
    }

    @Override
    public List<GameResponse> getGamesByStatus(List<String> progressList) {
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
