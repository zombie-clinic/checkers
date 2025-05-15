package com.example.checkers.service;

import com.example.checkers.model.MoveResponse;
import java.util.UUID;

public interface PossibleMoveService {

  // TODO This should return possibleMovesMap in order to work with types
  // TODO and then be mapped to MoveResponseDTO
  MoveResponse getNextMoves(UUID gameId);
}
