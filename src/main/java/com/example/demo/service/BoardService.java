package com.example.demo.service;

import com.example.demo.domain.PossibleMove;
import com.example.demo.domain.Side;
import com.example.demo.model.State;

import java.util.List;
import java.util.Map;

public interface BoardService {
    Map<Integer, List<PossibleMove>> getPossibleMoves(Side side, State state);
}
