package com.example.checkers.api;

import static com.example.checkers.service.StateUtils.fromJsonNodeIteratorToSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.checkers.core.Side;
import com.example.checkers.core.State;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExampleGameIT {

  String startLobbyRequest = "{\"value\": 1, \"side\": \"DARK\"}";
  String joinLobbyRequest = "{\"value\": %s, \"value\": \"%s\"}";

  String importGameRequest = "{\"value\":1,\"side\":\"LIGHT\",\"clientState\":{\"dark\":[11,27],\"light\":[2],\"kings\":[2]}}";

  @Autowired
  private MockMvc mockMvc;

  @Test
  @Disabled
    // FIXME 1. there should be a separate test suite for possible moves. 2. Find a way to compare unsorted set in such scenarios
  void givenStartingGame_whenOpponentJoins_shouldReturnPossibleMoves() throws Exception {
    var jsonMapper = new ObjectMapper();
    var startLobbyRequest = "{\"value\": 2, \"side\": \"LIGHT\"}";
    String response = startLobby(startLobbyRequest);

    var gameId = jsonMapper.readTree(response).get("value").asText();
    String s = joinLobby(1L, gameId);
    assertEquals(String.format(
        "{\"value\":\"%s\",\"progress\":\"STARTING\",\"startingState\":\"{\\\"dark\\\":[1,2,3,4,5,6,7,8,9,10,11,12]," +
            "\\\"light\\\":[21,22,23,24,25,26,27,28,29,30,31,32],\\\"kings\\\":[]}\",\"possibleMoves\":{\"value\":\"%s\"," +
            "\"serverState\":{\"dark\":[1,2,3,4,5,6,7,8,9,10,11,12],\"light\":[21,22,23,24,25,26,27,28,29,30,31,32],\"kings\":[]},\"side\":\"LIGHT\"," +
            "\"possibleMoves\":{\"21\":[{\"position\":21,\"destination\":17,\"isCapture\":false}],\"22\":[{\"position\":22,\"destination\":18," +
            "\"isCapture\":false},{\"position\":22,\"destination\":17,\"isCapture\":false}],\"23\":[{\"position\":23,\"destination\":19,\"isCapture\":false}," +
            "{\"position\":23,\"destination\":18,\"isCapture\":false}],\"24\":[{\"position\":24,\"destination\":20,\"isCapture\":false},{\"position\":24," +
            "\"destination\":19,\"isCapture\":false}]}}}", gameId, gameId), s);
  }

  @Test
  void testImportedGameWithKingMove() throws Exception {

    var jsonMapper = new ObjectMapper();

    String response = startImportedGame();
    var gameId = jsonMapper.readTree(response).get("value").asText();

    joinLobby(2L, gameId);

    var actualState = new State(Set.of(11, 27), Set.of(2), Set.of(2));

    TestFixture tf = new TestFixture(gameId, actualState);

    tf.makeMove("2x20", Side.LIGHT).and().expectStateToBe(new State(Set.of(27), Set.of(20), Set.of(20)));
    tf.makeMove("20x31", Side.LIGHT).and().expectStateToBe(new State(Set.of(), Set.of(31), Set.of(31)));
  }

  @Test
  void playExampleMatch() throws Exception {
    var jsonMapper = new ObjectMapper();

    String response = startLobby();
    var gameId = jsonMapper.readTree(response).get("value").asText();

    joinLobby(2L, gameId);

    var actualState = new State(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
        Set.of(21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32),
        Set.of());

    TestFixture tf = new TestFixture(gameId, actualState);

    tf.makeMove("21-17", Side.LIGHT).and()
        .expectStateToBe(new State(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), Set.of(22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 17), Set.of()));
    tf.makeMove("10-15", Side.DARK).and()
        .expectStateToBe(new State(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 15), Set.of(22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 17), Set.of()));
    tf.makeMove("22-18", Side.LIGHT).and()
        .expectStateToBe(new State(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 15), Set.of(23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 17, 18), Set.of()));
    tf.makeMove("15x22", Side.DARK).and()
        .expectStateToBe(new State(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 22), Set.of(23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 17), Set.of()));
    tf.makeMove("22x13", Side.DARK).and()
        .expectStateToBe(new State(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13), Set.of(23, 24, 25, 26, 27, 28, 29, 30, 31, 32), Set.of()));
  }

  private String joinLobby(long playerId, String gameId) throws Exception {
    var joinLobby = put("/games").contentType(APPLICATION_JSON)
        .content(joinLobbyRequest.formatted(playerId, gameId));

    return getResponseContentString(joinLobby);
  }

  private String getResponseContentString(MockHttpServletRequestBuilder request) throws Exception {
    return mockMvc.perform(request)
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  private String startLobby(String startLobbyRequest) throws Exception {
    var startLobby = post("/games").contentType(APPLICATION_JSON).content(startLobbyRequest);
    return getResponseContentString(startLobby);
  }

  private String startLobby() throws Exception {
    var startLobby = post("/games").contentType(APPLICATION_JSON).content(startLobbyRequest);
    return getResponseContentString(startLobby);
  }

  private String startImportedGame() throws Exception {
    var startLobby = post("/games?isImport=true").contentType(APPLICATION_JSON).content(importGameRequest);
    return getResponseContentString(startLobby);
  }

  class TestFixture {

    private final String gameId;

    private State actualState;

    public TestFixture(String gameId, State actualState) {
      this.gameId = gameId;
      this.actualState = actualState;
    }

    TestFixture makeMove(String move, Side side) throws Exception {
      String response = sendMove(side, move);
      JsonNode serverState = new ObjectMapper().readTree(response).get("serverState");
      ArrayNode dark = (ArrayNode) serverState.get("dark");
      ArrayNode light = (ArrayNode) serverState.get("light");
      ArrayNode kings = (ArrayNode) serverState.get("kings");
      actualState = new State(fromJsonNodeIteratorToSet(dark.elements()),
          fromJsonNodeIteratorToSet(light.elements()),
          fromJsonNodeIteratorToSet(kings.elements()));
      return this;
    }

    TestFixture and() {
      return this;
    }

    void expectStateToBe(State expectedState) {
      compareStates(expectedState, actualState);
    }

    private String sendMove(Side side, String move) throws Exception {
      String url = "/games/%s/moves".formatted(gameId);

      var jsonMapper = new ObjectMapper();

      var sideValue = jsonMapper.writeValueAsString(side);
      var moveValue = jsonMapper.writeValueAsString(move);
      var stateValue = jsonMapper.writeValueAsString(actualState);

      String content = "{\"side\":%s,\"move\":%s,\"clientState\":%s,\"value\":%d}"
          // TODO When player logic ready should either side or player ids
          .formatted(sideValue, moveValue, stateValue, side == Side.LIGHT ? 1L : 2L);
      var put = put(url).contentType(APPLICATION_JSON).content(content);
      return mockMvc.perform(put)
          .andReturn()
          .getResponse()
          .getContentAsString();
    }

    private void compareStates(State expectedState, State actualState) {
      assertEquals(expectedState, actualState);
    }
  }
}




