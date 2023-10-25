package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.model.GameResponse;
import com.example.demo.model.MoveResponse;
import com.example.demo.persistence.GameRepository;
import com.example.demo.persistence.MoveRepository;
import com.example.demo.persistence.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.demo.domain.Board.getInitialState;


@RequiredArgsConstructor
@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    private final MoveRepository moveRepository;

    private final PlayerRepository playerRepository;

    private final BoardService boardService;

    @Transactional
    @Override
    public MoveResponse startGame(Long userId) {
        Game game = new Game();
        game.setProgress(GameProgress.STARTING.toString());

        Optional<Player> user = playerRepository.findById(userId);

        if (user.isEmpty()) {
            throw new IllegalArgumentException("No such user, couldn't start game.");
        }

        game.setPlayer(user.get());
        Game savedGame = gameRepository.save(game);
        return generateFirstMoveResponse(savedGame.getId());
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
