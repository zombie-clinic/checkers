package com.example.checkers.domain;


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