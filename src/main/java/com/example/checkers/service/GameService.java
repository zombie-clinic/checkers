package com.example.checkers.service;

import com.example.checkers.model.GameResponse;
import com.example.checkers.model.MoveResponse;

import java.util.List;

public interface GameService {

    MoveResponse startGame(Long userId);

    List<GameResponse> getGamesByProgress(List<String> progressList);

    GameResponse getGameById(String uuid);

    boolean isGameValid(String uuid);
}
