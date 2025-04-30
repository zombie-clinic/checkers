package com.example.checkers.service;

import java.util.List;
import java.util.Map;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.Piece;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.Side;

public interface PossibleMoveProvider {

  Map<Integer, List<PossibleMove>> getPossibleMovesForPiece(Piece piece, Checkerboard state);

  Map<Integer, List<PossibleMove>> getPossibleMovesForSide(Side side, Checkerboard state);
}
