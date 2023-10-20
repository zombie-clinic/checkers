package com.example.demo.domain;

import lombok.Builder;

@Builder
public record PossibleMove(Side side,
                           int position,
                           int destination,
                           boolean isCapture,
                           boolean isTerminal) {
}
