package com.example.checkers.service;

import java.util.Arrays;
import java.util.List;

import com.example.checkers.domain.MoveRecord;
import com.example.checkers.model.State;

/**
 * Utility board stat method that does that does not require any wiring.
 */
public class StateUtils {

  static State getCurrentState(List<MoveRecord> moveList) {

    if (moveList.isEmpty()) {
      throw new IllegalArgumentException("""
          Move list is empty, state should be constructed from starting state
          """);
    }

    String dark = moveList.getLast().dark();
    String light = moveList.getLast().light();

    List<Integer> darkList = parseList(dark);
    List<Integer> lightList = parseList(light);

    return new State(
        darkList,
        lightList);
  }

  private static List<Integer> parseList(String str) {
    if ("".equals(str)) {
      return List.of();
    }

    return Arrays.stream(str.split(",")).map(Integer::valueOf).toList();
  }

}
