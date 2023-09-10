package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class GameController {

    @GetMapping("/")
    @ResponseBody
    public ResponseEntity<?> index() {
        Map<String, String> response = new HashMap<>();
        response.put("choice1", "white");
        response.put("choice2", "black");
        response.put("choice3", "random");
        response.put("button", "start");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/start")
    public ResponseEntity<?> startGame(@RequestParam String side) {
        return ResponseEntity.ok("You are playing " + side + " in the game " + UUID.randomUUID());
    }

    @GetMapping("/game/{id}")
    public ResponseEntity<?> joinGame(@PathVariable String id) {
        return ResponseEntity.ok("In the game " + id);
    }
}
