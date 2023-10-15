package com.example.demo.service;

import com.example.demo.domain.PlayerResponse;

public interface PlayerService {

    PlayerResponse findUserById(Long userId);
}
