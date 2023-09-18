package com.example.demo;

import com.example.demo.api.GameResponse;
import com.example.demo.domain.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class CheckersService implements Checkers {

    private final GameRepository gameRepository;

    @Override
    public List<GameResponse> getGamesByState(GameState state) {
        List<Game> games = gameRepository.findGameByState(state);

        return games.stream()
                .map(game -> {
                    var gameResponse = new GameResponse();
                    gameResponse.setId(game.getId());
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

        return gameResponse;
    }
}
