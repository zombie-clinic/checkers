package com.example.checkers.service;

import com.example.checkers.domain.Piece;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.Side;
import com.example.checkers.domain.State;
import java.util.List;
import java.util.Map;

public interface PossibleMoveProvider {

  Map<Integer, List<PossibleMove>> getPossibleMovesForPiece(Piece piece, State state);

  Map<Integer, List<PossibleMove>> getPossibleMovesForSide(Side side, State state);
}
