package com.example.demo.service;

import com.example.demo.domain.PlayerResponse;
import com.example.demo.persistence.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    @Override
    public PlayerResponse findUserById(Long userId) {
        return playerRepository.findById(userId)
                .map(u -> new PlayerResponse(u.getId(), u.getName()))
                .orElse(null);
    }
}
