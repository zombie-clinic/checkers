package com.example.checkers.domain;

import java.util.LinkedList;
import java.util.List;

import com.example.checkers.model.State;

public class Checkerboard {

  private final List<Integer> darkPieces;

  private final List<Integer> lightPieces;

  public Checkerboard(List<Integer> darkPieces, List<Integer> lightPieces) {
    this.darkPieces = darkPieces;
    this.lightPieces = lightPieces;
  }

  public static State getStartingState() {
    return new State(
        List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
        List.of(21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32));
  }

  public static Checkerboard state(List<Integer> darkPieces, List<Integer> lightPieces) {
    return new Checkerboard(darkPieces, lightPieces);
  }

  public static List<LinkedList<Integer>> getDiagonals() {
    LinkedList<Integer> left1 = new LinkedList<>(List.of(1, 5));
    LinkedList<Integer> left2 = new LinkedList<>(List.of(2, 6, 9, 13));
    LinkedList<Integer> left3 = new LinkedList<>(List.of(3, 7, 10, 14, 17, 21));
    LinkedList<Integer> left4 = new LinkedList<>(List.of(4, 8, 11, 15, 18, 22, 25, 29));
    LinkedList<Integer> left5 = new LinkedList<>(List.of(12, 16, 19, 23, 26, 30));
    LinkedList<Integer> left6 = new LinkedList<>(List.of(20, 24, 27, 31));
    LinkedList<Integer> left7 = new LinkedList<>(List.of(28, 32));

    LinkedList<Integer> right1 = new LinkedList<>(List.of(4));
    LinkedList<Integer> right2 = new LinkedList<>(List.of(3, 8, 12));
    LinkedList<Integer> right3 = new LinkedList<>(List.of(2, 7, 11, 16, 20));
    LinkedList<Integer> right4 = new LinkedList<>(List.of(1, 6, 10, 15, 19, 24, 28));
    LinkedList<Integer> right5 = new LinkedList<>(List.of(5, 9, 14, 18, 23, 27, 32));
    LinkedList<Integer> right6 = new LinkedList<>(List.of(13, 17, 22, 26, 31));
    LinkedList<Integer> right7 = new LinkedList<>(List.of(21, 25, 30));
    LinkedList<Integer> right8 = new LinkedList<>(List.of(29));

    return List.of(
        left1, left2, left3, left4, left5, left6, left7,
        right1, right2, right3, right4, right5, right6, right7, right8
    );
  }

  public List<Integer> getSide(Side side) {
    return switch (side) {
      case DARK -> darkPieces;
      case LIGHT -> lightPieces;
    };
  }

  public boolean isEmptyCell(int num) {
    return !darkPieces.contains(num) && !lightPieces.contains(num);
  }
}
