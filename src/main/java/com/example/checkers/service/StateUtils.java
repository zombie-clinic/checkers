package com.example.checkers.service;

import static com.example.checkers.domain.Side.DARK;
import static com.example.checkers.domain.Side.LIGHT;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.Side;
import com.example.checkers.model.MoveRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.example.checkers.domain.MoveRecord;
import com.example.checkers.model.State;
import java.util.Objects;

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

  static State generateAfterCaptureState(State state, MoveRequest moveRequest) {

    State calculated;

    Integer start = Integer.valueOf(moveRequest.getMove().split("[x\\-]")[0]);
    Integer dest = Integer.valueOf(moveRequest.getMove().split("[x\\-]")[1]);


    if (Side.valueOf(moveRequest.getSide()) == DARK) {
      var darkPieces = new ArrayList<>(state.getDark());
      var lightPieces = new ArrayList<>(state.getLight());
      darkPieces.removeIf(el -> Objects.equals(el, start));
      darkPieces.add(dest);
      if (isCaptureMove(moveRequest)) {
        lightPieces.remove(determineCapturedPieceIdx(Side.valueOf(moveRequest.getSide()),
            start,
            dest));
      }
      calculated = new State(
          darkPieces, lightPieces
      );
    } else {
      var darkPieces = new ArrayList<>(state.getDark());
      var lightPieces = new ArrayList<>(state.getLight());
      lightPieces.removeIf(el -> Objects.equals(el, start));
      lightPieces.add(dest);
      if (isCaptureMove(moveRequest)) {
        darkPieces.remove(determineCapturedPieceIdx(Side.valueOf(moveRequest.getSide()),
            start,
            dest));
      }
      calculated = new State(
          darkPieces, lightPieces
      );

    }

    return calculated;
  }

  private static Integer determineCapturedPieceIdx(Side side, Integer start, Integer end) {
    for (LinkedList<Integer> diagonal : Checkerboard.getDiagonals()) {
      LinkedList<Integer> d;
      if (side == LIGHT) {
        d = new LinkedList<>(diagonal.reversed());
      } else {
        d = new LinkedList<>(diagonal);
      }
      if (d.contains(start) && d.contains(end)) {
        int startIdx = d.indexOf(start);
        int endIdx = d.indexOf(end);
        return d.get((startIdx + endIdx) / 2);
      }
    }
    throw new IllegalStateException(String.format("""
            Trying to determine impossible capture: %s%s
            """,
        start, end));
  }

  private static boolean isCaptureMove(MoveRequest moveRequest) {
    return moveRequest.getMove().contains("x");
  }
}
