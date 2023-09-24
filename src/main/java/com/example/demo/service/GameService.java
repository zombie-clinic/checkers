package com.example.demo.service;

import com.example.demo.api.GameResponse;

import java.util.List;

public interface GameService {

    GameResponse startGame();

    List<GameResponse> getGamesByStatus(String gameStatus);

    GameResponse getGameById(String uuid);
}
