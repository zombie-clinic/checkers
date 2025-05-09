package com.example.checkers.service;

import static com.example.checkers.domain.Side.DARK;
import static com.example.checkers.domain.Side.LIGHT;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.MoveRecord;
import com.example.checkers.domain.Side;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Utility board state methods that do not require any wiring.
 */
public class StateUtils {

  // FIXME Change to accept last move
  // FIXME Ensure immutability

  /**
   * Given a move (in this case a last move) return representation of the board.
   *
   * @param moveList Move to infer state from
   * @return immutable State object
   */
  public static State getStateFromMoveList(List<MoveRecord> moveList) {

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
        lightList,
        moveList.getLast().kings()
    );
  }

  // FIXME Ensure immutability
  // TODO Should be a method for every move, not only capture
  // FIXME Don't need to pass all MoveRequest

  /**
   * Is certain cell empty, i.e. free for a move to be performed.
   *
   * @param num   cell number
   * @param state current state against which check is performed
   * @return true or false
   */
  public static boolean isEmptyCell(int num, State state) {
    return !state.getDark().contains(num) && !state.getLight().contains(num);
  }

  /**
   * Get a list of cell of a certain side.
   *
   * @param side on DARK, LIGHT
   * @return immutable list of corresponding board positions
   */
  public static List<Integer> getSide(Side side, State state) {
    return switch (side) {
      case DARK -> state.getDark();
      case LIGHT -> state.getLight();
    };
  }

  /**
   * Given a move, create an immutable State object, which reflects a piece being captured.
   *
   * @param state       immutable source State object
   * @param moveRequest moveRequest provided by a client
   * @return immutable target State object
   */
  static State generateAfterMoveOrCaptureState(State state, MoveRequest moveRequest) {
    Integer start = Integer.valueOf(moveRequest.getMove().split("[x\\-]")[0]);
    Integer dest = Integer.valueOf(moveRequest.getMove().split("[x\\-]")[1]);
    Side side = Side.valueOf(moveRequest.getSide());
    var light = new ArrayList<>(state.getLight());
    var dark = new ArrayList<>(state.getDark());
    var kings = new ArrayList<>(state.getKings());

    if (!moveRequest.getMove().contains("x")) {
      if (side == LIGHT) {
        light.removeIf(e -> e.equals(start));
        light.add(dest);
      } else {
        dark.removeIf(e -> e.equals(start));
        dark.add(dest);
      }
    } else {
      if (Side.valueOf(moveRequest.getSide()) == DARK) {
        dark.removeIf(el -> Objects.equals(el, start));
        dark.add(dest);
        if (isCaptureMove(moveRequest)) {
          light.remove(determineCapturedPieceIdx(Side.valueOf(moveRequest.getSide()),
              start,
              dest));
        }
      } else {
        light.removeIf(el -> Objects.equals(el, start));
        light.add(dest);
        if (isCaptureMove(moveRequest)) {
          dark.remove(determineCapturedPieceIdx(Side.valueOf(moveRequest.getSide()),
              start,
              dest));
        }
      }
    }

    boolean removed = kings.removeIf(e -> e.equals(start));
    if (removed) {
      kings.add(dest);
    }

    return new State(dark, light, kings);
  }

  private static List<Integer> parseList(String str) {
    if ("".equals(str)) {
      return List.of();
    }

    return Arrays.stream(str.split(",")).map(Integer::valueOf).toList();
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

  // TODO Maybe return a Pair.of pieces and corresponding kings?

  private static boolean isCaptureMove(MoveRequest moveRequest) {
    return moveRequest.getMove().contains("x");
  }
}
