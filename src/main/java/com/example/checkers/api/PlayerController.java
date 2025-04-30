package com.example.checkers.api;

import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.example.checkers.model.PlayerResponse;
import com.example.checkers.service.PlayerService;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RequiredArgsConstructor
@RestController
public class PlayerController implements PlayerApi {

  private final PlayerService playerService;

  @Override
  public ResponseEntity<List<PlayerResponse>> getPlayers() {
    return ok(playerService.findAll());
  }


  @Override
  public ResponseEntity<PlayerResponse> getPlayerById(Long playerId) {
    PlayerResponse userById = playerService.findUserById(playerId);
    if (userById != null) {
      return ok(userById);
    }

    throw new IllegalArgumentException("No such player");
  }
}
