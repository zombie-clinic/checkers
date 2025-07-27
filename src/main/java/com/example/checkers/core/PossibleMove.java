package com.example.checkers.core;

import lombok.Builder;

@Builder
public record PossibleMove(Piece piece,
                           int destination,
                           Boolean isCapture) {
}
