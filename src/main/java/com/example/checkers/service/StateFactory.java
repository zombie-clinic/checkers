package com.example.checkers.service;

import com.example.checkers.domain.Move;
import com.example.checkers.model.State;
import java.util.Arrays;

public class StateFactory {

  public static State stateFrom(Move move) {
    return new State(
        Arrays.stream(move.getDark().split(",")).map(Integer::valueOf).toList(),
        Arrays.stream(move.getLight().split(",")).map(Integer::valueOf).toList()
    );
  }
}
