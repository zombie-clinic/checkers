package com.example.demo.service;

import com.example.demo.model.MoveRequest;
import com.example.demo.model.MoveResponse;

public interface MoveService {

    MoveResponse saveMove(String gameId, MoveRequest moveRequest);

    MoveResponse getCurrentState(String gameId);
}
