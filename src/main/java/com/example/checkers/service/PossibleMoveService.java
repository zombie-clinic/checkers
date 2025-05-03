package com.example.checkers.service;

import java.util.UUID;

import com.example.checkers.model.MoveResponse;

public interface PossibleMoveService {

  // TODO This should return possibleMovesMap in order to work with types
  // TODO and then be mapped to MoveResponseDTO
  MoveResponse getNextMoves(UUID gameId);
}
