package com.example.checkers.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Hidden
@RestController
public class WelcomePageController {

    @GetMapping("/")
    @ResponseBody
    public ResponseEntity<?> index() {
        log.info("hello react");
        return ResponseEntity.ok("hello react");
    }
}
