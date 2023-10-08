package com.example.demo.service;

import com.example.demo.domain.MoveRequest;
import com.example.demo.domain.MoveResponse;

public interface MoveService {

    MoveResponse saveMove(String gameId, MoveRequest moveRequest);
}
