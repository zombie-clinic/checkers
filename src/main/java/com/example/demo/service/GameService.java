package com.example.demo.service;

import com.example.demo.model.GameResponse;

import java.util.List;

public interface GameService {

    GameResponse startGame(Long userId);

    List<GameResponse> getGamesByProgress(List<String> progressList);

    GameResponse getGameById(String uuid);

    boolean isGameValid(String uuid);
}
