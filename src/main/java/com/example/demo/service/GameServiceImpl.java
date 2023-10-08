package com.example.demo.service;

import com.example.demo.domain.GameProgress;
import com.example.demo.persistence.GameRepository;
import com.example.demo.persistence.MoveRepository;
import com.example.demo.persistence.UserRepository;
import com.example.demo.domain.Game;
import com.example.demo.domain.GameResponse;
import com.example.demo.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    private final MoveRepository moveRepository;

    private final UserRepository userRepository;

    @Override
    public GameResponse startGame(Long userId) {
        Game game = new Game();
        game.setProgress(GameProgress.STARTING.toString());

        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new IllegalArgumentException("No such user, couldn't start game.");
        }

        game.setUser(user.get());

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
