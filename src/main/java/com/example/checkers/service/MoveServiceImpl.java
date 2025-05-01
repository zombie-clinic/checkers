package com.example.checkers.service;

import static com.example.checkers.domain.Side.DARK;
import static com.example.checkers.domain.Side.LIGHT;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.Game;
import com.example.checkers.domain.Move;
import com.example.checkers.domain.Player;
import com.example.checkers.domain.Side;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.State;
import com.example.checkers.persistence.GameRepository;
import com.example.checkers.persistence.MoveRepository;
import com.example.checkers.persistence.PlayerRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MoveServiceImpl implements MoveService {

  private final MoveRepository moveRepository;

  private final GameRepository gameRepository;

  private final PlayerRepository playerRepository;

  private final StartingStateLookupService startingStateLookupService;

  @Override
  @Transactional
  public void saveMove(UUID gameId, MoveRequest moveRequest) {
    Game game = findGame(gameId);
    Player player = findPlayer(moveRequest);
    Side movingSide = Side.valueOf(moveRequest.getSide());

    // FIXME duplication with getCurrentState
    State currentState = getCurrentStateFromMove(gameId);

    validateMoveRequest(currentState, moveRequest.getState());

    State state = moveRequest.getState();
    String moveStr = moveRequest.getMove();

    State newState = generateNewState(state, movingSide, moveStr, mv -> mv.contains("x"));
    Move move = new Move(game, player, movingSide.name(), moveRequest.getMove(),
        newState.getDark().stream().map(String::valueOf).collect(Collectors.joining(",")),
        newState.getLight().stream().map(String::valueOf).collect(Collectors.joining(",")));

    // TODO There is no need to introduce possible move abstraction, we could go with Move DTO

    // FIXME redundant call to database
    List<Move> moves = moveRepository.findAllByGameId(gameId.toString());

    List<Integer> kings;
    if (moves.isEmpty()) {
      kings = new ArrayList<>();
    } else {
      kings = new ArrayList<>(moves.getLast().getKings());
    }
    Integer dest = Integer.valueOf(move.getMove().split("[-x]")[1]);
    if (moveDestKings(Side.valueOf(move.getSide()), dest)) {
      kings.add(dest);
      move.setKings(kings);
    } else {
      move.setKings(kings);
    }

    // done if last move contains kings
    // done if this moves ends on opponent's end (king creation)
    // todo check kings being captured as well
    moveRepository.save(move);
  }

  private State getCurrentStateFromMove(UUID gameId) {
    List<Move> moves = moveRepository.findAllByGameId(gameId.toString());
    State currentState;
    if (moves.isEmpty()) {
      currentState = startingStateLookupService.getStateFromStartingStateString(gameId);
      currentState.setKings(List.of());
    } else {
      currentState = new State(
          Arrays.stream(moves.getLast().getDark().split(",")).map(Integer::valueOf).toList(),
          Arrays.stream(moves.getLast().getLight().split(",")).map(Integer::valueOf).toList()
      );
      List<Integer> kings = moves.getLast().getKings();
      if (kings == null) {
        kings = Collections.emptyList();
      }
      currentState.setKings(kings);
    }
    return currentState;
  }

  private Player findPlayer(MoveRequest moveRequest) {
    return playerRepository.findById(moveRequest.getPlayerId()).orElseThrow(
        () -> new IllegalArgumentException("No player found")
    );
  }

  private Game findGame(UUID gameId) {
    return gameRepository.findById(gameId.toString()).orElseThrow(
        () -> new IllegalArgumentException("No game found")
    );
  }

  private boolean moveDestKings(Side side, Integer dest) {

    if (side == DARK) {
      return List.of(29, 30, 31, 32).contains(dest);
    }
    return List.of(1, 2, 3, 4).contains(dest);
  }

  private void validateMoveRequest(State serverState, State clientState) {
    if (!serverState.getDark().equals(clientState.getDark()) || !serverState.getLight().equals(clientState.getLight())) {
      throw new IllegalArgumentException("provided state not consistent, wait for you turn");
    }
  }

  private static State generateNewState(State currentState,
                                        Side side,
                                        String move,
                                        Predicate<String> isCapture) {
    State newState = new State();
    String[] split = move.split("[-x]");

    String start = split[0];
    String dest = split[1];

    if (isCapture.test(move)) {
      // todo remove deprecated method
      newState = generateAfterCaptureState(currentState, side, String.join("x", start, dest));
    } else {
      int startInt = Integer.parseInt(start);
      int destInt = Integer.parseInt(dest);
      var light = new ArrayList<>(currentState.getLight());
      var dark = new ArrayList<>(currentState.getDark());

      if (side == LIGHT) {
        light.removeIf(e -> e.equals(startInt));
        light.add(destInt);
      } else {
        dark.removeIf(e -> e.equals(startInt));
        dark.add(destInt);
      }

      newState.setDark(dark);
      newState.setLight(light);
    }


    return newState;
  }

  private static boolean isCaptureMove(MoveRequest moveRequest) {
    return moveRequest.getMove().contains("x");
  }

  static State generateAfterCaptureState(State state, Side side, String move) {
    MoveRequest moveRequest = new MoveRequest();
    moveRequest.setSide(side.toString());
    moveRequest.setMove(move);

    return generateAfterCaptureState(state, moveRequest);
  }

  static State generateAfterCaptureState(State state, MoveRequest moveRequest) {

    // TODO Database call could be done earlier
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
}

