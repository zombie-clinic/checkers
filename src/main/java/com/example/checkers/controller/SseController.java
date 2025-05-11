package com.example.checkers.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Server side events component, responsible for pushing moves to frontend,
 * when game is in active state.
 */
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/sse")
public class SseController {

  private final SseService sseService;

  /**
   * Subscribe to start receiving events.
   */
  @GetMapping("/subscribe")
  public SseEmitter subscribe() {
    return sseService.subscribe();
  }
}

