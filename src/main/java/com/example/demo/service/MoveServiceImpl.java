package com.example.demo.service;

import com.example.demo.GameRepository;
import com.example.demo.MoveRepository;
import com.example.demo.api.MoveRequest;
import com.example.demo.api.MoveResponse;
import com.example.demo.domain.Game;
import com.example.demo.domain.Move;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MoveServiceImpl implements MoveService {

    private final MoveRepository moveRepository;

    private final GameRepository gameRepository;

    // TODO Use mapper or json parser to validate move request, e.g.
    // TODO previousGameState vs previousState
    @Transactional
    @Override
    public MoveResponse saveMove(String gameId, MoveRequest moveRequest) {
        Optional<Game> game = gameRepository.findGameById(gameId);
        if (game.isEmpty()) {
            throw new IllegalArgumentException("No game found");
        }
        List<Move> moves = moveRepository.findAllByGameId(gameId);
        String newGameState = UUID.randomUUID().toString();

        if (moves.isEmpty()) {
            moveRepository.save(
                    new Move(
                            game.get(), "white", "initial state", moveRequest.getMove()
                    )
            );

            MoveResponse moveResponse = new MoveResponse();
            moveResponse.setMove(moveRequest.getSide());
            moveResponse.setPreviousState(newGameState);

            return moveResponse;
        }

        if (isInconsistentGame(moveRequest, moves)) {
            throw new IllegalStateException("Inconsistent game, provide a valid move, here is you previous state: []");
        }

        moveRepository.save(
                // TODO Introduce Move builder
                // TODO Calculate state
                new Move(
                        game.get(), "white", newGameState, moveRequest.getMove()
                )
        );

        // TODO MoveResponse should contain board state and should not contain a move
        MoveResponse moveResponse = new MoveResponse();
        moveResponse.setMove(moveRequest.getSide());
        moveResponse.setPreviousState(newGameState);

        return moveResponse;
    }

    // TODO moves should not be empty
    private static boolean isInconsistentGame(MoveRequest moveRequest, List<Move> moves) {
        return !moves.get(moves.size() - 1).getPreviousGameState().equals(moveRequest.getPreviousState());
    }
}
