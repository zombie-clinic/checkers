package com.example.demo.service;

import com.example.demo.model.PlayerResponse;

import java.util.List;

public interface PlayerService {

    List<PlayerResponse> findAll();

    PlayerResponse findUserById(Long userId);
}
