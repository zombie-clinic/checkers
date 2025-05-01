package com.example.checkers.service;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.Piece;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.Side;
import java.util.List;
import java.util.Map;

public interface PossibleMoveProvider {

  Map<Integer, List<PossibleMove>> getPossibleMovesForPiece(Piece piece, Checkerboard state);

  Map<Integer, List<PossibleMove>> getPossibleMovesForSide(Side side, Checkerboard state);
}
