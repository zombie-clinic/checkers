package com.example.checkers.service;

import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.Side;
import com.example.checkers.model.State;

import java.util.List;
import java.util.Map;

public interface BoardService {

    Map<Integer, List<PossibleMove>> getPossibleMoves(Side side, State state);

    List<BoardServiceImpl.Position> getAdjacentSquares(Integer square);
}
