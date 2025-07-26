package com.example.checkers.service;

import com.example.checkers.model.GameResponse;
import java.util.List;

public interface QueryService {

  List<GameResponse> getGamesByProgress(List<String> progressList);

  GameResponse getGameById(String uuid);
}
