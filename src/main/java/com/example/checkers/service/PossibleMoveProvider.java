package com.example.checkers.service;

import com.example.checkers.core.Piece;
import com.example.checkers.core.PossibleMove;
import com.example.checkers.core.Side;
import com.example.checkers.core.State;
import java.util.List;
import java.util.Map;

public interface PossibleMoveProvider {

  Map<Integer, List<PossibleMove>> getPossibleMovesForPiece(Piece piece, State state);

  Map<Integer, List<PossibleMove>> getPossibleMovesForSide(Side side, State state);
}
