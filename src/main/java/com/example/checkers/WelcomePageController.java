package com.example.checkers;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Hidden
@RestController
public class WelcomePageController {

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
}
