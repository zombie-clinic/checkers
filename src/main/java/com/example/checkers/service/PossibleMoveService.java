package com.example.checkers.service;

import java.util.UUID;

import com.example.checkers.model.MoveResponse;

public interface PossibleMoveService {

  MoveResponse getNextMoves(UUID gameId);
}
