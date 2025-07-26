package com.example.checkers.service;

import static com.example.checkers.core.Side.DARK;

import com.example.checkers.adapters.db.GameRepository;
import com.example.checkers.adapters.db.MoveRepository;
import com.example.checkers.adapters.db.PersistentGame;
import com.example.checkers.adapters.db.PlayerRepository;
import com.example.checkers.core.Move;
import com.example.checkers.core.Player;
import com.example.checkers.core.Side;
import com.example.checkers.core.State;
import com.example.checkers.core.UseCaseInteractor;
import com.example.checkers.model.MoveRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@UseCaseInteractor
public class MoveServiceImpl implements MoveService {

  private final MoveRepository moveRepository;

  private final GameRepository gameRepository;

  private final PlayerRepository playerRepository;

  private final StartingStateLookupService startingStateLookupService;

  @Override
  @Transactional
  public void saveMove(UUID gameId, MoveRequest moveRequest) {
    PersistentGame game = findGame(gameId);
    Player player = findPlayer(moveRequest);
    Side movingSide = Side.valueOf(moveRequest.getSide());

    // FIXME duplication with getCurrentState
    State currentState = getCurrentStateFromMove(gameId);

    validateMoveRequest(currentState, State.from(moveRequest.getClientState()));

    State newState = StateUtils.generateAfterMoveOrCaptureState(currentState, moveRequest);

    // state, movingSide, moveStr, mv -> mv.contains("x"));
    Move move = new Move(game, player, movingSide.name(), moveRequest.getMove(),
        newState.getDark().stream().map(String::valueOf).collect(Collectors.joining(",")),
        newState.getLight().stream().map(String::valueOf).collect(Collectors.joining(",")));

    move.setKings(newState.getKings());

    Integer dest = Integer.valueOf(move.getMove().split("[-x]")[1]);
    if (!move.getKings().contains(dest) && isMoveResultsInKings(Side.valueOf(move.getSide()), dest)) {
      Set<Integer> currentKings = move.getKings();
      currentKings.add(dest);
      move.setKings(currentKings);
    }

    moveRepository.save(move);
  }

  private State getCurrentStateFromMove(UUID gameId) {
    List<Move> moves = moveRepository.findAllByGameId(gameId.toString());
    State currentState;
    if (moves.isEmpty()) {
      currentState = startingStateLookupService.getStateFromStartingStateString(gameId);
    } else {
      currentState = new State(
          Arrays.stream(moves.getLast().getDark().split(",")).map(Integer::valueOf).collect(Collectors.toSet()),
          Arrays.stream(moves.getLast().getLight().split(",")).map(Integer::valueOf).collect(Collectors.toSet()),
          moves.getLast().getKings()
      );
      Set<Integer> kings = moves.getLast().getKings();
      if (kings == null) {
        kings = Collections.emptySet();
      }
      currentState = new State(currentState.getDark(), currentState.getLight(), kings);
    }
    return currentState;
  }

  private Player findPlayer(MoveRequest moveRequest) {
    return playerRepository.findById(moveRequest.getPlayerId()).orElseThrow(
        () -> new IllegalArgumentException("No player found")
    );
  }

  private PersistentGame findGame(UUID gameId) {
    return gameRepository.findById(gameId.toString()).orElseThrow(
        () -> new IllegalArgumentException("No game found")
    );
  }

  private boolean isMoveResultsInKings(Side side, Integer dest) {
    if (side == DARK) {
      return List.of(29, 30, 31, 32).contains(dest);
    }
    return List.of(1, 2, 3, 4).contains(dest);
  }

  // TODO refactor
  private void validateMoveRequest(State serverState, State clientState) {
    List<Integer> serverDark = new ArrayList<>(serverState.getDark());
    List<Integer> serverLight = new ArrayList<>(serverState.getLight());
    List<Integer> clientDark = new ArrayList<>(clientState.getDark());
    List<Integer> clientLight = new ArrayList<>(clientState.getLight());
    Collections.sort(serverDark);
    Collections.sort(serverLight);
    Collections.sort(clientDark);
    Collections.sort(clientLight);
    if (!serverDark.equals(clientDark) || !serverLight.equals(clientLight)) {
      throw new IllegalArgumentException("provided state not consistent, wait for you turn");
    }
  }
}

