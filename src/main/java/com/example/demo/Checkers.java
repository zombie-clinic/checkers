package com.example.demo;

import com.example.demo.api.GameResponse;
import com.example.demo.api.Move;

import java.util.List;

public interface Checkers {

    List<GameResponse> getGamesByState(String state);

    GameResponse getGameById(String uuid);

    void saveMove(String gameId, Move move);
}
