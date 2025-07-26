package com.example.checkers.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Stateless component representing invariants associated with a checkerboard.
 */
// TODO Forbid instantiation
public final class Checkerboard {

  public static State getStartingState() {
    return new State(
        Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
        Set.of(21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32),
        Set.of()
    );
  }

  /**
   * Get playable diagonals of a 8x8 board.
   *
   * @return List of linked lists containing cell numbers.
   */
  public static List<LinkedList<Integer>> getDiagonals() {
    var left1 = new LinkedList<>(List.of(1, 5));
    var left2 = new LinkedList<>(List.of(2, 6, 9, 13));
    var left3 = new LinkedList<>(List.of(3, 7, 10, 14, 17, 21));
    var left4 = new LinkedList<>(List.of(4, 8, 11, 15, 18, 22, 25, 29));
    var left5 = new LinkedList<>(List.of(12, 16, 19, 23, 26, 30));
    var left6 = new LinkedList<>(List.of(20, 24, 27, 31));
    var left7 = new LinkedList<>(List.of(28, 32));

    var right1 = new LinkedList<>(List.of(4));
    var right2 = new LinkedList<>(List.of(3, 8, 12));
    var right3 = new LinkedList<>(List.of(2, 7, 11, 16, 20));
    var right4 = new LinkedList<>(List.of(1, 6, 10, 15, 19, 24, 28));
    var right5 = new LinkedList<>(List.of(5, 9, 14, 18, 23, 27, 32));
    var right6 = new LinkedList<>(List.of(13, 17, 22, 26, 31));
    var right7 = new LinkedList<>(List.of(21, 25, 30));
    var right8 = new LinkedList<>(List.of(29));

    return List.of(
        left1, left2, left3, left4, left5, left6, left7,
        right1, right2, right3, right4, right5, right6, right7, right8
    );
  }
}
