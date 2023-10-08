package com.example.demo.service;

import com.example.demo.GameRepository;
import com.example.demo.GameProgress;
import com.example.demo.MoveRepository;
import com.example.demo.domain.GameResponse;
import com.example.demo.domain.Game;
import com.example.demo.domain.Move;
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

    @Override
    public GameResponse startGame() {
        Game game = new Game();
        game.setProgress(GameProgress.STARTING.toString());
        Game savedGame = gameRepository.save(game);
        var gameResponse = new GameResponse();
        gameResponse.setId(savedGame.getId());
        gameResponse.setProgress(GameProgress.STARTING.toString());
        return gameResponse;
    }

    @Override
    public List<GameResponse> getGamesByStatus(List<String> progressList) {
        if (progressList.isEmpty()) {
            return gameRepository.findAll().stream()
                    .map(game -> {
                        var gameResponse = new GameResponse();
                        gameResponse.setId(game.getId());
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
                    gameResponse.setId(game.getId());
                    gameResponse.setProgress(game.getProgress());
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
        gameResponse.setProgress(game.get().getProgress());

        List<Move> moves = moveRepository.findAllByGameId(game.get().getId());

        if (moves.isEmpty()) {
            gameResponse.setProgress("");
        } else {
            String state = moves.get(moves.size() - 1).getState().replace("\\\"", "");
            gameResponse.setProgress(state);
        }

        return gameResponse;
    }
}
