package com.example.demo.service;

import com.example.demo.api.MoveRequest;
import com.example.demo.api.MoveResponse;

public interface MoveService {

    MoveResponse saveMove(String gameId, MoveRequest moveRequest);
}
