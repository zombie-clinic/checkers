package com.example.checkers.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.checkers.domain.Side;
import com.example.checkers.model.State;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

  String startLobbyRequest = "{\"playerId\": 1, \"side\": \"DARK\"}";
  String joinLobbyRequest = "{\"playerId\": 2, \"gameId\": \"%s\"}";

  String importGameRequest = "{\"playerId\":1,\"side\":\"LIGHT\",\"state\":{\"dark\":[11,27],\"light\":[2],\"kings\":[2]}}";

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testImportedGameWithKingMove() throws Exception {

    var jsonMapper = new ObjectMapper();

    String response = startImportedGame();
    var gameId = jsonMapper.readTree(response).get("gameId").asText();

    joinLobby(gameId);

    var actualState = new State(List.of(11, 27), List.of(2), List.of(2));

    TestFixture tf = new TestFixture(gameId, actualState);

    tf.makeMove("2x20", Side.LIGHT).and().expectStateToBe(new State(List.of(27), List.of(20), List.of(20)));
    tf.makeMove("20x31", Side.LIGHT).and().expectStateToBe(new State(List.of(), List.of(31), List.of(31)));
  }

  @Test
  void playExampleMatch() throws Exception {
    var jsonMapper = new ObjectMapper();

    String response = startLobby();
    var gameId = jsonMapper.readTree(response).get("gameId").asText();

    joinLobby(gameId);

    var actualState = new State(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
        List.of(21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32),
        List.of());

    TestFixture tf = new TestFixture(gameId, actualState);

    tf.makeMove("21-17", Side.LIGHT).and()
        .expectStateToBe(new State(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), List.of(22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 17), List.of()));
    tf.makeMove("10-15", Side.DARK).and()
        .expectStateToBe(new State(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 15), List.of(22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 17), List.of()));
    tf.makeMove("22-18", Side.LIGHT).and()
        .expectStateToBe(new State(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 15), List.of(23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 17, 18), List.of()));
    tf.makeMove("15x22", Side.DARK).and()
        .expectStateToBe(new State(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 22), List.of(23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 17), List.of()));
    tf.makeMove("22x13", Side.DARK).and()
        .expectStateToBe(new State(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13), List.of(23, 24, 25, 26, 27, 28, 29, 30, 31, 32), List.of()));
  }

  private String joinLobby(String gameId) throws Exception {
    var joinLobby = put("/games").contentType(APPLICATION_JSON)
        .content(joinLobbyRequest.formatted(gameId));

    return getResponseContentString(joinLobby);
  }

  private String getResponseContentString(MockHttpServletRequestBuilder request) throws Exception {
    return mockMvc.perform(request)
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
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

    private String gameId;

    private State actualState;

    public TestFixture(String gameId, State actualState) {
      this.gameId = gameId;
      this.actualState = actualState;
    }

    TestFixture makeMove(String move, Side side) throws Exception {
      String response = sendMove(side, move);
      JsonNode aState = new ObjectMapper().readTree(response).get("state");
      ArrayNode dark = (ArrayNode) aState.get("dark");
      ArrayNode light = (ArrayNode) aState.get("light");
      ArrayNode kings = (ArrayNode) aState.get("kings");
      actualState = new State(fromIteratorToList(dark.elements()),
          fromIteratorToList(light.elements()),
          fromIteratorToList(kings.elements()));
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

      String content = "{\"side\":%s,\"move\":%s,\"state\":%s,\"playerId\":%d}"
          // TODO When player logic ready should either side or player ids
          .formatted(sideValue, moveValue, stateValue, side == Side.LIGHT ? 1L : 2L);
      var put = put(url).contentType(APPLICATION_JSON).content(content);
      String contentAsString = mockMvc.perform(put)
          .andReturn()
          .getResponse()
          .getContentAsString();
      return contentAsString;
    }

    private void compareStates(State expectedState, State actualState) {

      // TODO Should be sorted elsewhere
      // TODO Should either changed to sets of sorted in equals
      var sortedListDark = new ArrayList<>(expectedState.getDark());
      var sortedListLight = new ArrayList<>(expectedState.getLight());
      var sortedListKings = new ArrayList<>(expectedState.getKings());

      Collections.sort(sortedListDark);
      Collections.sort(sortedListLight);
      Collections.sort(sortedListKings);

      expectedState.setDark(sortedListDark);
      expectedState.setLight(sortedListLight);
      expectedState.setKings(sortedListKings);

      var sortedActualListDark = new ArrayList<>(actualState.getDark());
      var sortedActualListLight = new ArrayList<>(actualState.getLight());
      var sortedActualListKings = new ArrayList<>(actualState.getKings());

      Collections.sort(sortedActualListDark);
      Collections.sort(sortedActualListLight);
      Collections.sort(sortedActualListKings);

      actualState.setDark(sortedActualListDark);
      actualState.setLight(sortedActualListLight);
      actualState.setKings(sortedActualListKings);

      // TODO Black/White positions should be a type
      State actual = new State(
          sortedActualListDark, sortedActualListLight, sortedActualListKings
      );

      assertEquals(expectedState, actual);
    }

    private List<Integer> fromIteratorToList(Iterator<JsonNode> elements) {
      List<Integer> list = new ArrayList<>();
      elements.forEachRemaining(e -> list.add(e.intValue()));
      return list;
    }
  }
}




