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
        return ResponseEntity.ok("hello react");
    }
}
