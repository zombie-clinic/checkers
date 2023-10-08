package com.example.demo.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenGamesExist_whenFilterEmpty_fetchAllGames() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/game")
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(10));
    }

    @Test
    void givenGamesExist_whenFilterOngoing_fetchOngoingGames() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/game?progress=ONGOING")
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2));
    }

    @Test
    void givenGamesExist_whenMultipleFilters_fetchGamesWithMultipleStatuses() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/game?progress=ONGOING,COMPLETED")
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(6));
    }
}