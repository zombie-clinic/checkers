package com.example.checkers.service;

import com.example.checkers.model.GameResponse;
import com.example.checkers.model.MoveResponse;

import java.util.List;

public interface GameService {

    GameResponse startLobby(Long playerOneId, String side);

    MoveResponse startGame(String  gameId, Long playerTwoId);

    List<GameResponse> getGamesByProgress(List<String> progressList);

    GameResponse getGameById(String uuid);

    boolean isGameValid(String uuid);

    void lobbyExistsAndPlayerIsDifferent(String gameId, Long playerId);
}
