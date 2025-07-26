package com.example.checkers.service;

import com.example.checkers.model.MoveRequest;
import java.util.UUID;


/**
 * Service responsible for parsing, validating and saving a move.
 */
public interface MoveService {

  /**
   * Persist a move.
   *
   * @param gameId      A valid uuid of an ongoing game
   * @param moveRequest Payload with a move accompanied by previous state
   */
  @Deprecated
  void saveMove(UUID gameId, MoveRequest moveRequest);
}
