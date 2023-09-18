package com.example.demo;

import com.example.demo.api.GameResponse;

import java.util.List;

public interface Checkers {

    List<GameResponse> getGamesByState(GameState state);

    GameResponse getGameById(String uuid);
}
