package com.example.checkers.controller;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseService {
  // Store all active clients
  private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  public void sendUpdate(@RequestBody String message) {
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event()
            .name("game-update")
            .data(message));
      } catch (IOException e) {
        emitters.remove(emitter);
      }
    }
  }

  SseEmitter subscribe() {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    emitters.add(emitter);

    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    emitter.onError((e) -> emitters.remove(emitter));

    try {
      emitter.send(SseEmitter.event().name("INIT").data("Subscribed"));
    } catch (Exception e) {
      emitters.remove(emitter);
    }

    return emitter;
  }
}
