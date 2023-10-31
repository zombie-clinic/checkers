package com.example.checkers.api;

import com.example.checkers.domain.Board;
import com.example.checkers.domain.Side;
import com.example.checkers.model.State;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExampleGameIT {

    String playerJson = """
                {"playerId": 1}
            """;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void playExampleMatch() throws Exception {
        var jsonMapper = new ObjectMapper();

        // start game
        String response = startGame();
        var gameId = jsonMapper.readTree(response).get("gameId").asText();

        State expectedState = Board.getInitialState();
        JsonNode actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);

        // 1
        response = makeMove(gameId, Side.BLACK, "9-14", actualState, 1);
        expectedState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 14),
                List.of(21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32)
        );
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);

        // 2
        response = makeMove(gameId, Side.WHITE, "22-17", actualState, 2);
        expectedState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 14),
                List.of(21, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 17)
        );
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);

        // 3
        response = makeMove(gameId, Side.BLACK, "11-15", actualState, 1);
        expectedState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 14, 15),
                List.of(21, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 17)
        );
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);

        // 4
        response = makeMove(gameId, Side.WHITE, "25-22", actualState, 2);
        expectedState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 14, 15),
                List.of(21, 23, 24, 26, 27, 28, 29, 30, 31, 32, 17, 22)
        );
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);

        // 5
        response = makeMove(gameId, Side.BLACK, "15-19", actualState, 1);
        expectedState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 14, 19),
                List.of(21, 23, 24, 26, 27, 28, 29, 30, 31, 32, 17, 22)
        );
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);

        // 6
        response = makeMove(gameId, Side.WHITE, "23x16", actualState, 2);
        expectedState = new State(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 14),
                List.of(21, 24, 26, 27, 28, 29, 30, 31, 32, 17, 22, 16)
        );
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);
    }

    private String startGame() throws Exception {
        var startNewGame = post("/games").contentType(APPLICATION_JSON).content(playerJson);
        return mockMvc.perform(startNewGame)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private void compareStates(State expectedState, JsonNode actualState) throws IOException {

        // TODO Should be sorted elsewhere
        var sortedListBlack = new ArrayList<>(expectedState.getBlack());
        var sortedListWhite = new ArrayList<>(expectedState.getWhite());

        Collections.sort(sortedListBlack);
        Collections.sort(sortedListWhite);

        expectedState.setBlack(sortedListBlack);
        expectedState.setWhite(sortedListWhite);

        ArrayNode black = (ArrayNode) actualState.get("black");
        ArrayNode white = (ArrayNode) actualState.get("white");

        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(new TypeReference<List<Integer>>() {
        });

        List<Integer> blackList = reader.readValue(black);
        List<Integer> whiteList = reader.readValue(white);

        Collections.sort(blackList);
        Collections.sort(whiteList);

        // TODO Black/White positions should be a type
        State actual = new State(
                blackList, whiteList
        );

        assertEquals(expectedState, actual);
    }

    private String makeMove(String gameId, Side side, String move, JsonNode state, int playerId) throws Exception {
        String url = STR. "/games/\{ gameId }/moves" ;

        var jsonMapper = new ObjectMapper();
        var sideValue = jsonMapper.writeValueAsString(side);
        var moveValue = jsonMapper.writeValueAsString(move);
        var stateValue = jsonMapper.writeValueAsString(state);

        String content = STR. "{\"side\":\{ sideValue },\"move\":\{ moveValue },\"state\":\{ stateValue },\"playerId\":\{ playerId }}" ;
        var put = MockMvcRequestBuilders.put(url).contentType(APPLICATION_JSON).content(content);
        return mockMvc.perform(put)
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}



