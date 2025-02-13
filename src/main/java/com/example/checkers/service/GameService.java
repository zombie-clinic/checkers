package com.example.checkers.service;

import com.example.checkers.domain.Side;
import com.example.checkers.model.GameResponse;

import java.util.List;

public interface GameService {

    GameResponse startLobby(Long playerOneId, String side);

    GameResponse startGame(String  gameId, Long playerTwoId);

    List<GameResponse> getGamesByProgress(List<String> progressList);

    GameResponse getGameById(String uuid);

    boolean isGameValid(String uuid);

    void lobbyExistsAndPlayerIsDifferent(String gameId, Long playerId);

    Side getCurrentSide(String gameId);
}
