package com.example.checkers.service;

import com.example.checkers.domain.Game;
import com.example.checkers.domain.GameProgress;
import com.example.checkers.domain.MoveRecord;
import com.example.checkers.domain.Player;
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
    game.setStartingState("{\"dark\":[28],\"light\":[5]}");

    Game savedGame = gameRepository.save(game);
    String gameId = savedGame.getId();

    MoveRequest moveRequest1 = new MoveRequest();
    moveRequest1.setMove("5-1");
    moveRequest1.setSide("LIGHT");
    moveRequest1.setPlayerId(1L);
    State state1 = new State(List.of(28), List.of(5));
    state1.setKings(List.of());
    moveRequest1.setState(state1);
    moveService.saveMove(UUID.fromString(gameId), moveRequest1);

    List<MoveRecord> moveRecords = movesReaderService.getMovesFor(gameId);
    assert moveRecords.getLast().kings().contains(1);

    MoveRequest moveRequest2 = new MoveRequest();
    moveRequest2.setMove("28-32");
    moveRequest2.setSide("DARK");
    moveRequest2.setPlayerId(2L);
    State state2 = new State(List.of(28), List.of(1));
    state2.setKings(List.of(1));
    moveRequest2.setState(state2);
    moveService.saveMove(UUID.fromString(gameId), moveRequest2);

    moveRecords = movesReaderService.getMovesFor(gameId);
    assert moveRecords.getLast().kings().containsAll(List.of(1, 32));

    MoveRequest moveRequest3 = new MoveRequest();
    moveRequest3.setMove("1-6");
    moveRequest3.setSide("LIGHT");
    moveRequest3.setPlayerId(1L);
    State state3 = new State(List.of(32), List.of(1));
    state3.setKings(List.of(1, 32));
    moveRequest3.setState(state3);
    moveService.saveMove(UUID.fromString(gameId), moveRequest3);

    moveRecords = movesReaderService.getMovesFor(gameId);
    assert moveRecords.getLast().kings().containsAll(List.of(6, 32));
  }
}
