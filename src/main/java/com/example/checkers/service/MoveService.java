package com.example.checkers.service;

import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.MoveResponse;

import java.util.UUID;

public interface MoveService {

    void saveMove(UUID gameId, MoveRequest moveRequest);

    MoveResponse getNextMoves(UUID gameId);
}
