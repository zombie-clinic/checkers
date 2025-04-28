package com.example.checkers.domain;

import lombok.Builder;

@Builder
public record PossibleMove(Piece piece,
                           int destination,
                           Boolean isCapture,
                           Boolean isTerminal) {
}
