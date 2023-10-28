package com.example.checkers.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO With MockMvc there was no need to adjust test when GameApiController was split
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiTest {

    String playerJson = """
                {"playerId": 1}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenGameNotExist_whenStart_startNewGame() throws Exception {
        var startNewGame = post("/games").contentType(APPLICATION_JSON).content(playerJson);
        mockMvc.perform(startNewGame)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state.length()").value(2))
                .andExpect(jsonPath("$.possibleMoves.*..destination.length()").value(hasSize(7)));
    }

    @Test
    void givenGamesExist_whenFilterEmpty_fetchAllGames() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/games")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10));
    }

    @Test
    void givenGamesExist_whenFilterOngoing_fetchOngoingGames() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/games?progress=ONGOING")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void givenGamesExist_whenMultipleFilters_fetchGamesWithMultipleStatuses() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/games?progress=ONGOING,COMPLETED")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }
}