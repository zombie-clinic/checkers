package com.example.checkers.service;

import com.example.checkers.domain.MoveRecord;
import com.example.checkers.persistence.MoveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MovesReaderService {

    private final MoveRepository moveRepository;

    public List<MoveRecord> getMovesFor(String gameId) {
        return moveRepository.findAllByGameId(gameId)
                .stream()
                .map(MoveRecord::fromMove)
                .toList();
    }
}
