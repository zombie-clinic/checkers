package com.example.checkers.service;

import static com.example.checkers.domain.Side.DARK;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

    State newState = StateUtils.generateAfterCaptureState(currentState, moveRequest);

    // state, movingSide, moveStr, mv -> mv.contains("x"));
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
    Set<Integer> kingsSet = new HashSet<>(kings);

    Integer start = Integer.valueOf(move.getMove().split("[-x]")[0]);
    Integer dest = Integer.valueOf(move.getMove().split("[-x]")[1]);
    if (isMoveResultsInKings(Side.valueOf(move.getSide()), dest)) {
      kingsSet.add(dest);
      move.setKings(kingsSet.stream().toList());
    } else {
      if (kingsSet.contains(start)) {
        kingsSet.remove(start);
        kingsSet.add(dest);
      }
      move.setKings(kingsSet.stream().toList());
    }
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

  private boolean isMoveResultsInKings(Side side, Integer dest) {
    if (side == DARK) {
      return List.of(29, 30, 31, 32).contains(dest);
    }
    return List.of(1, 2, 3, 4).contains(dest);
  }

  private void validateMoveRequest(State serverState, State clientState) {
    if (!serverState.getDark().equals(clientState.getDark())
        || !serverState.getLight().equals(clientState.getLight())) {
      throw new IllegalArgumentException("provided state not consistent, wait for you turn");
    }
  }
}

