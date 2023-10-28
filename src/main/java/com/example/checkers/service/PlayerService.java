package com.example.checkers.service;

import com.example.checkers.model.PlayerResponse;

import java.util.List;

public interface PlayerService {

    List<PlayerResponse> findAll();

    PlayerResponse findUserById(Long userId);
}
