package com.example.checkers.core;

public record PlayerId(Long value) {

  public static PlayerId of(Long id) {
    return new PlayerId(id);
  }
}
