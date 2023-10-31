package com.example.checkers.service;

import com.example.checkers.domain.Side;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.MoveResponse;

public interface MoveService {

    MoveResponse saveMove(String gameId, MoveRequest moveRequest);

    MoveResponse generateMoveResponse(String gameId, Side side);
}
