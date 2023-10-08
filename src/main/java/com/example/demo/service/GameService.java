package com.example.demo.service;

import com.example.demo.domain.GameResponse;

import java.util.List;

public interface GameService {

    GameResponse startGame(Long userId);

    List<GameResponse> getGamesByStatus(List<String> statusList);

    GameResponse getGameById(String uuid);

    boolean isGameValid(String uuid);
}
