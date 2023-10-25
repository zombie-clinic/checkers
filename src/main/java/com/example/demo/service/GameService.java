package com.example.demo.service;

import com.example.demo.model.GameResponse;
import com.example.demo.model.MoveResponse;

import java.util.List;

public interface GameService {

    MoveResponse startGame(Long userId);

    List<GameResponse> getGamesByProgress(List<String> progressList);

    GameResponse getGameById(String uuid);

    boolean isGameValid(String uuid);
}
