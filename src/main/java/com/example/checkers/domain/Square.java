package com.example.checkers.domain;

import java.util.List;

public record Square(Integer number, Piece pieceType, List<Integer> neighborSquares) {

}
