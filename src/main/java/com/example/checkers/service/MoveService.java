package com.example.checkers.service;

import java.util.UUID;

import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.MoveResponse;

public interface MoveService {

  void saveMove(UUID gameId, MoveRequest moveRequest);

  MoveResponse getNextMoves(UUID gameId);
}
