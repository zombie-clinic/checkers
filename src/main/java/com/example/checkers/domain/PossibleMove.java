package com.example.checkers.domain;

import lombok.Builder;

@Builder
public record PossibleMove(Side side,
                           int position,
                           int destination,
                           Boolean isCapture,
                           Boolean isTerminal) {
}
