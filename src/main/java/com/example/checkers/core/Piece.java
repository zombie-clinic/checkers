package com.example.checkers.core;


/**
 * A piece is a container class to hold a position and a side of checker.
 *
 * @param position cell num according to conventional checkers placement
 * @param side     one of the value of Side enum
 */
public record Piece(int position, Side side) {

  public boolean isLight() {
    return side == Side.LIGHT;
  }

  public boolean isDark() {
    return !isLight();
  }

  public Side oppositeSide() {
    return isLight() ? Side.DARK : Side.LIGHT;
  }

  public static Piece of(int position, Side side) {
    return new Piece(position, side);
  }
}
