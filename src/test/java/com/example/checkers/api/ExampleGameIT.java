package com.example.checkers.api;

import com.example.checkers.domain.Checkerboard;
import com.example.checkers.domain.Side;
import com.example.checkers.model.State;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExampleGameIT {

    String startRequest = """
                 {
                    "playerId": 1, "side": "DARK"
                 }
            """;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void playExampleMatch() throws Exception {

        var state0 = startGame(startRequest, startGameChecks());
        var state1 = makeMove(1, "9-14", state0, firstMoveChecks());
        var state2 = makeMove(2, "22-17", state1, secondMoveChecks());


        var jsonMapper = new ObjectMapper();

        // start game
        String response = startGame();
        var gameId = jsonMapper.readTree(response).get("gameId").asText();

        State expectedState = Checkerboard.getStartingState();
        JsonNode jsonNode = jsonMapper.readTree(response);
        assertEquals(7, jsonNode.get("possibleMoves").size());
        JsonNode actualState = jsonNode.get("state");
        compareStates(expectedState, actualState);

        // 1
        response = makeMove(gameId, Side.DARK, "9-14", actualState, 1);
        expectedState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 14),
                List.of(21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32)
        );
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);

        // 2
        response = makeMove(gameId, Side.LIGHT, "22-17", actualState, 2);
        expectedState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 14),
                List.of(21, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 17)
        );
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);

        // 3
        response = makeMove(gameId, Side.DARK, "11-15", actualState, 1);
        expectedState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 14, 15),
                List.of(21, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 17)
        );
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);

        // 4
        response = makeMove(gameId, Side.LIGHT, "25-22", actualState, 2);
        expectedState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 14, 15),
                List.of(21, 23, 24, 26, 27, 28, 29, 30, 31, 32, 17, 22)
        );
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);

        // 5
        response = makeMove(gameId, Side.DARK, "15-19", actualState, 1);
        expectedState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 14, 19),
                List.of(21, 23, 24, 26, 27, 28, 29, 30, 31, 32, 17, 22)
        );
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);

        // 6
        response = makeMove(gameId, Side.LIGHT, "23x16", actualState, 2);
        expectedState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 14),
                List.of(21, 24, 26, 27, 28, 29, 30, 31, 32, 17, 22, 16)
        );
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);
    }

    private List<Predicate<JsonNode>> startGameChecks() {
        return List.of(
                jsonNode -> jsonNode.get("state").properties().size() == 2,
                jsonNode -> jsonNode.get("possibleMoves").properties().size() == 4
        );
    }

    private List<Predicate<JsonNode>> firstMoveChecks() {
        return List.of(
                jsonNode -> jsonNode.get("state").properties().size() == 2,
                jsonNode -> jsonNode.get("possibleMoves").properties().size() == 4
        );
    }

    private List<Predicate<JsonNode>> secondMoveChecks() {
        return List.of(
                jsonNode -> jsonNode.get("state").properties().size() == 2,
                jsonNode -> jsonNode.get("possibleMoves").properties().size() == 4
        );
    }


    private JsonNode startGame(String startRequest,
                               List<Predicate<JsonNode>> conditions) throws Exception {
        var request = post("/games").contentType(APPLICATION_JSON).content(startRequest);
        String response = getResponse(request);
        JsonNode jsonNode = parseResponse(response);
        conditions.forEach(a -> assertTrue(a.test(jsonNode)));
        return jsonNode;
    }

    private JsonNode parseResponse(String response) throws JsonProcessingException {
        return new ObjectMapper().readTree(response);
    }

    private String getResponse(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private void compareStates(State expectedState, JsonNode actualState) throws IOException {

        // TODO Should be sorted elsewhere
        var sortedListDark = new ArrayList<>(expectedState.getDark());
        var sortedListLight = new ArrayList<>(expectedState.getLight());

        Collections.sort(sortedListDark);
        Collections.sort(sortedListLight);

        expectedState.setDark(sortedListDark);
        expectedState.setLight(sortedListLight);

        ArrayNode dark = (ArrayNode) actualState.get("dark");
        ArrayNode light = (ArrayNode) actualState.get("light");

        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(new TypeReference<List<Integer>>() {
        });

        List<Integer> darkList = reader.readValue(dark);
        List<Integer> lightList = reader.readValue(light);

        Collections.sort(darkList);
        Collections.sort(lightList);

        // TODO Black/White positions should be a type
        State actual = new State(
                darkList, lightList
        );

        assertEquals(expectedState, actual);
    }

    private String makeMove(JsonNode currentState,
                            Long playerId,
                            String move,
                            List<Predicate<JsonNode>> afterMoveChecks) throws Exception {
        String gameId = currentState.get("gameId").asText();
        String side = currentState.get("sides").get(playerId);

        String url = STR. "/games/\{ gameId }/moves" ;

        var jsonMapper = new ObjectMapper();
        var sideValue = jsonMapper.writeValueAsString(side);
        var moveValue = jsonMapper.writeValueAsString(move);
        var stateValue = jsonMapper.writeValueAsString(currentState);

        String content = STR. "{\"side\":\{ sideValue },\"move\":\{ moveValue },\"state\":\{ stateValue },\"playerId\":\{ playerId }}" ;
        var put = MockMvcRequestBuilders.put(url).contentType(APPLICATION_JSON).content(content);
        String response = getResponse(put);
        JsonNode jsonNode = parseResponse(response);

        afterMoveChecks.forEach(p -> assertTrue(p.test(jsonNode)));
        return response;
    }
}



