package com.example.checkers.service;

import com.example.checkers.model.PlayerResponse;
import com.example.checkers.persistence.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<PlayerResponse> findAll() {
        return playerRepository.findAll()
                .stream().map(u -> new PlayerResponse(u.getId(), u.getName()))
                .toList();
    }
}
