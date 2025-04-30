package com.example.checkers.service;

import java.util.List;

import com.example.checkers.model.PlayerResponse;

public interface PlayerService {

  List<PlayerResponse> findAll();

  PlayerResponse findUserById(Long userId);
}
