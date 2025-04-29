package com.example.checkers.service;

import static com.example.checkers.domain.Side.DARK;
import static com.example.checkers.domain.Side.LIGHT;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.Game;
import com.example.checkers.domain.Move;
import com.example.checkers.domain.MoveRecord;
import com.example.checkers.domain.Player;
import com.example.checkers.domain.PossibleMove;
import com.example.checkers.domain.PossibleMoveSimplified;
import com.example.checkers.domain.Side;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.MoveResponse;
import com.example.checkers.model.State;
import com.example.checkers.persistence.GameRepository;
import com.example.checkers.persistence.MoveRepository;
import com.example.checkers.persistence.PlayerRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MoveServiceImpl implements MoveService {

  private final MoveRepository moveRepository;

  private final GameRepository gameRepository;

  private final PlayerRepository playerRepository;

  private final PossibleMoveProvider possibleMoveProvider;

  private final TurnService turnService;

  private final MovesReaderService movesReaderService;

  private final StartingStateLookupService startingStateLookupService;

  // TODO do we need side? Depends on if we are fetching from db
  @Override
  @Transactional
  public MoveResponse getNextMoves(UUID gameId) {
    var moveList = movesReaderService.getMovesFor(gameId.toString());


    var nextToMoveSide = turnService.getWhichSideToMove(gameId.toString());
    if (nextToMoveSide == null) {
      // meaning last player to capture a piece wins
      // we return empty possible moves and null next turn
      // in order front end to recognize end time
      return new MoveResponse(gameId.toString(),
          getCurrentState(moveList.stream().toList()),
          null,
          Map.of()
      );
    }
    // log.info("Next move: {}", nextToMoveSide);
    return generateMoveResponse(gameId.toString(), nextToMoveSide);
  }

  @Override
  @Transactional
  public void saveMove(UUID gameId, MoveRequest moveRequest) {
    Game game = gameRepository.findById(gameId.toString()).orElseThrow(
        () -> new IllegalArgumentException("No game found")
    );
    Player player = playerRepository.findById(moveRequest.getPlayerId()).orElseThrow(
        () -> new IllegalArgumentException("No player found")
    );
    Side movingSide = Side.valueOf(moveRequest.getSide());

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
    validateMoveRequest(currentState, moveRequest.getState());

    State state = moveRequest.getState();
    String moveStr = moveRequest.getMove();

    State newState = generateNewState(state, movingSide, moveStr, mv -> mv.contains("x"));
    Move move = new Move(game, player, movingSide.name(), moveRequest.getMove(),
        newState.getDark().stream().map(String::valueOf).collect(Collectors.joining(",")),
        newState.getLight().stream().map(String::valueOf).collect(Collectors.joining(",")));

    // TODO There is no need to introduce possible move abstraction, we could go with Move DTO

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

  // TODO Add a distinction between Player and User
  // TODO User becomes Player when a game starts, Player has a user id and a side
  private MoveResponse generateMoveResponse(String gameId, Side nextToMoveSide) {
    var moveList = movesReaderService.getMovesFor(gameId);

    State state;
    if (moveList.isEmpty()) {
      // fixme seems to be unreachable code
      state = startingStateLookupService.getStateFromStartingStateString(UUID.fromString(gameId));
      state.setKings(List.of());
    } else {
      state = getCurrentState(moveList);
      MoveRecord last = moveList.getLast();
      if (last == null) {
        state.setKings(List.of());
      } else {
        if(last.kings().isEmpty()) {
          state.setKings(List.of());
        } else {
          state.setKings(new ArrayList<>(last.kings()));
        }
      }
    }
    // regular move, game in progress
    Map<Integer, List<PossibleMove>> possibleMoves = possibleMoveProvider.getPossibleMovesForSide(
        nextToMoveSide, Checkerboard.state(state.getDark(), state.getLight())
    );
    Map<Integer, List<PossibleMoveSimplified>> simplifiedPossibleMoves =
        getSimplifiedPossibleMoves(possibleMoves);

    Map<Integer, List<PossibleMoveSimplified>> res = new HashMap<>();

    for (Map.Entry<Integer, List<PossibleMoveSimplified>> e :
        simplifiedPossibleMoves.entrySet()) {
      if (e.getValue().stream().anyMatch(PossibleMoveSimplified::isCapture)) {
        res.put(e.getKey(), e.getValue());
      }
    }

    if (res.isEmpty()) {
      // no captures, regular move
      return new MoveResponse(gameId, state, nextToMoveSide.name(),
          simplifiedPossibleMoves);
    }

    // Only captures below
    Side currentSide = Side.valueOf(nextToMoveSide.name());
    Side lastMoveSide = moveList.getLast().side();

    if (currentSide == lastMoveSide) {
      Integer lastMoveCellDest = Integer.valueOf(moveList.getLast().move().split("x")[1]);
      Map<Integer, List<PossibleMoveSimplified>> resFilteredForChainCaptures =
          new HashMap<>();
      resFilteredForChainCaptures.put(lastMoveCellDest, res.get(lastMoveCellDest));

      return new MoveResponse(gameId, state, currentSide.name(),

          resFilteredForChainCaptures.entrySet().stream()
              .filter(e -> {
                var list = e.getValue();
                return list.stream().anyMatch(
                    PossibleMoveSimplified::isCapture);
              }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    return new MoveResponse(gameId, state, currentSide.name(), res);
  }

  private Map<Integer, List<PossibleMoveSimplified>> getSimplifiedPossibleMoves(Map<Integer,
      List<PossibleMove>> moves) {
    Map<Integer, List<PossibleMoveSimplified>> map = new HashMap<>();
    for (Map.Entry<Integer, List<PossibleMove>> entry : moves.entrySet()) {
      map.put(entry.getKey(), entry.getValue().stream()
          .map(PossibleMoveSimplified::fromMove).toList());
    }
    return map;
  }

  static State getCurrentState(List<MoveRecord> moveList) {

    if (moveList.isEmpty()) {
      throw new IllegalArgumentException("Move list is empty, state should be constructed " +
          "from starting state");
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

  private static boolean isGameStart(List<MoveRecord> moves) {
    return moves.isEmpty();
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
    throw new IllegalStateException(String.format("Trying to determine impossible capture: " +
            "%s%s",
        start, end));
  }
}
