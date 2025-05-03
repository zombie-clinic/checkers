package com.example.checkers.service;

import static com.example.checkers.domain.Side.DARK;
import static com.example.checkers.domain.Side.LIGHT;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.checkers.domain.Game;
import com.example.checkers.domain.GameProgress;
import com.example.checkers.domain.MoveRecord;
import com.example.checkers.domain.Player;
import com.example.checkers.domain.Side;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.State;
import com.example.checkers.persistence.GameRepository;
import com.example.checkers.persistence.MoveRepository;
import com.example.checkers.persistence.PlayerRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MoveServiceImplTest {

  @Autowired
  private GameRepository gameRepository;

  @Autowired
  private MovesReaderService movesReaderService;

  @Autowired
  private PlayerRepository playerRepository;

  @Autowired
  private MoveRepository moveRepository;

  @Autowired
  private MoveService moveService;

  @Test
  void saveMove() {

    Player playerOne = new Player();
    playerOne.setId(1L);

    Player playerTwo = new Player();
    playerTwo.setId(2L);

    var game = new Game();
    game.setProgress(GameProgress.ONGOING.toString());
    game.setPlayerOne(playerOne);
    game.setPlayerTwo(playerTwo);
    game.setStartingState("{\"dark\":[28, 8],\"light\":[5, 23]}");

    Game savedGame = gameRepository.save(game);
    String gameId = savedGame.getId();

    // 1
    State state = buildState(List.of(28, 8), List.of(5, 23), List.of());
    MoveRequest moveRequest = buildMoveRequest("5-1", LIGHT, state);
    moveService.saveMove(UUID.fromString(gameId), moveRequest);

    List<MoveRecord> moveRecords = movesReaderService.getMovesFor(gameId);
    assertThat(moveRecords.getLast().kings()).contains(1);

    // 2
    state = buildState(List.of(28, 8), List.of(1, 23), List.of(1));
    moveRequest = buildMoveRequest("28-32", DARK, state);
    moveService.saveMove(UUID.fromString(gameId), moveRequest);

    moveRecords = movesReaderService.getMovesFor(gameId);
    assertThat(moveRecords.getLast().kings()).containsAll(List.of(1, 32));

    // 3
    state = buildState(List.of(32, 8), List.of(1, 23), List.of(1, 32));
    moveRequest = buildMoveRequest("1-6", LIGHT, state);
    moveService.saveMove(UUID.fromString(gameId), moveRequest);

    moveRecords = movesReaderService.getMovesFor(gameId);
    assertThat(moveRecords.getLast().kings()).containsAll(List.of(6, 32));

    // 4
    state = buildState(List.of(32, 8), List.of(6, 23), List.of(6, 32));
    moveRequest = buildMoveRequest("32-27", DARK, state);
    moveService.saveMove(UUID.fromString(gameId), moveRequest);

    moveRecords = movesReaderService.getMovesFor(gameId);
    assertThat(moveRecords.getLast().kings()).containsAll(List.of(6, 27));

    // 5
    state = buildState(List.of(27, 8), List.of(6, 23), List.of(6, 27));
    moveRequest = buildMoveRequest("23x32", LIGHT, state);
    moveService.saveMove(UUID.fromString(gameId), moveRequest);

    moveRecords = movesReaderService.getMovesFor(gameId);
    assertThat(moveRecords.getLast().kings()).containsAll(List.of(6));
  }

  private State buildState(List<Integer> light, List<Integer> dark, List<Integer> kings) {
    var state = new State(light, dark);
    state.setKings(kings);
    return state;
  }

  private static MoveRequest buildMoveRequest(String move, Side side, State state) {
    MoveRequest req = new MoveRequest();
    req.setMove(move);
    req.setSide(side.toString());
    req.setPlayerId(side == LIGHT ? 1L : 2L);
    req.setState(state);
    return req;
  }
}
