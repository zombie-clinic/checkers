package com.example.checkers.api;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.checkers.domain.GameProgress;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiTest {

  String startLobbyRequest = "{\"playerId\": 1, \"side\": \"DARK\"}";
  String joinLobbyRequest = "{\"playerId\": 2, \"gameId\": \"%s\"}";

  @Autowired
  private MockMvc mockMvc;

  @Test
  void givenLobbyNotExist_startLobby() throws Exception {
    var startLobby = post("/games").contentType(APPLICATION_JSON).content(startLobbyRequest);
    mockMvc.perform(startLobby)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.progress").value(GameProgress.LOBBY.toString()))
        .andExpect(jsonPath("$.gameId").isNotEmpty());
  }

  @Test
  void givenLobbyExist_joinLobby() throws Exception {
    var startLobby = post("/games").contentType(APPLICATION_JSON).content(startLobbyRequest);
    String response = mockMvc.perform(startLobby).andReturn().getResponse().getContentAsString();
    String gameId = new ObjectMapper().readTree(response).get("gameId").asText();
    var request = MockMvcRequestBuilders.put("/games")
        .contentType(APPLICATION_JSON)
        .content(joinLobbyRequest.formatted(gameId));

    mockMvc.perform(request)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gameId").value(gameId))
        .andExpect(jsonPath("$.progress").value(GameProgress.STARTING.toString()));
  }

  @Test
  void givenGamesExist_whenFilterOngoing_fetchOngoingGames() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.get("/games?progress=ONGOING")
        ).andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void givenGamesExist_whenMultipleFilters_fetchGamesWithMultipleStatuses() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.get("/games?progress=LOBBY,STARTING,ONGOING,FINISHED,ARCHIVED")
        ).andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3));
  }
}
