package com.example.checkers.api;

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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExampleGameIT {

    String startLobbyRequest = "{\"playerId\": 1, \"side\": \"DARK\"}";
    String joinLobbyRequest = "{\"playerId\": 2, \"gameId\": \"%s\"}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void playExampleMatch() throws Exception {
        var jsonMapper = new ObjectMapper();

        String response = startLobby();
        var gameId = jsonMapper.readTree(response).get("gameId").asText();

        joinLobby(gameId);

        JsonNode actualState = jsonMapper.readTree(
                """
                        {
                            "dark":[1,2,3,4,5,6,7,8,9,10,11,12],
                            "light":[21,22,23,24,25,26,27,28,29,30,31,32]
                        }
                        """
        );

        // 1
        response = makeMove(gameId, Side.LIGHT, "21-17", actualState, 1);
        State expectedState = new State(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                List.of(22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 17));
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);

        // 2
        response = makeMove(gameId, Side.DARK, "10-15", actualState, 2);
        expectedState = new State(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 15), List.of(22, 23
                , 24, 25, 26, 27, 28, 29, 30, 31, 32, 17));
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);

        // 3
        response = makeMove(gameId, Side.LIGHT, "22-18", actualState, 1);
        expectedState = new State(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 15), List.of(23, 24
                , 25, 26, 27, 28, 29, 30, 31, 32, 17, 18));
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);

        // 4
        response = makeMove(gameId, Side.DARK, "15x22", actualState, 2);
        System.out.println(response);
        expectedState = new State(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 22), List.of(23, 24
                , 25, 26, 27, 28, 29, 30, 31, 32, 17));
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);

        // 5
        response = makeMove(gameId, Side.DARK, "22x13", actualState, 2);
        expectedState = new State(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13), List.of(23, 24
                , 25, 26, 27, 28, 29, 30, 31, 32));
        actualState = jsonMapper.readTree(response).get("state");
        compareStates(expectedState, actualState);
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

    private String makeMove(String gameId, Side side, String move, JsonNode state, int playerId) throws Exception {
        String url = "/games/%s/moves".formatted(gameId);

        var jsonMapper = new ObjectMapper();
        var sideValue = jsonMapper.writeValueAsString(side);
        var moveValue = jsonMapper.writeValueAsString(move);
        var stateValue = jsonMapper.writeValueAsString(state);

        String content = "{\"side\":%s,\"move\":%s,\"state\":%s,\"playerId\":%d}"
                .formatted(sideValue, moveValue, stateValue, playerId);
        var put = put(url).contentType(APPLICATION_JSON).content(content);
        String contentAsString = mockMvc.perform(put)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return contentAsString;
    }
}




