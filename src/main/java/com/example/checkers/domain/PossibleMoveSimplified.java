package com.example.checkers.domain;

public record PossibleMoveSimplified(int position,
                                     int destination,
                                     Boolean isCapture) {

  public static PossibleMoveSimplified fromMove(PossibleMove possibleMove) {
    return new PossibleMoveSimplified(
        possibleMove.piece().position(),
        possibleMove.destination(),
        possibleMove.isCapture()
    );
  }
}
