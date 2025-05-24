package com.example.checkers.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseService {

  // This contains all active clients at any given moment
  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

  public void sendUpdate(String eventName, MessageData messageData) {
    String key = "%s:%d".formatted(messageData.gameId(), messageData.playerId());
    SseEmitter emitter = emitters.get(key);
    try {
      emitter.send(SseEmitter.event()
          .name(eventName)
          .comment("timestamp: %s".formatted(LocalDateTime.now()))
          .data(messageData.message())
      );
    } catch (IOException e) {
      emitters.remove(key);
    }
  }

  SseEmitter subscribe(SessionData sessionData) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    String key = "%s:%d".formatted(sessionData.gameId(), sessionData.playerId());
    emitters.put(key, emitter);

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
