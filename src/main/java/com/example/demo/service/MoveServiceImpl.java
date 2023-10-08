package com.example.demo.service;

import com.example.demo.persistence.GameRepository;
import com.example.demo.persistence.MoveRepository;
import com.example.demo.persistence.UserRepository;
import com.example.demo.domain.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.example.demo.service.StateCalculator.initialState;

@RequiredArgsConstructor
@Service
public class MoveServiceImpl implements MoveService {

    private final MoveRepository moveRepository;

    private final GameRepository gameRepository;
    
    private final UserRepository userRepository;

    // TODO Use mapper or json parser to validate move request, e.g.
    @Transactional
    @Override
    public MoveResponse saveMove(String gameId, MoveRequest moveRequest) {
        Optional<Game> game = gameRepository.findGameById(gameId);
        if (game.isEmpty()) {
            throw new IllegalArgumentException("No game found");
        }

        Optional<User> user = userRepository.findById(moveRequest.getUserId());
        if (user.isEmpty()) {
            throw new IllegalArgumentException("No user found");
        }
        
        List<Move> moves = moveRepository.findAllByGameId(gameId);

        if (moves.isEmpty()) {
            moveRepository.save(
                    new Move(
                            game.get(), user.get(), "white", "initial state", moveRequest.getMove()
                    )
            );

            moveRequest.setState("initial state");
            return generateMoveResponse(gameId, moveRequest);
        }

        if (isInconsistentGame(moveRequest, moves)) {
            throw new IllegalStateException("Inconsistent game, provide a valid move, here is you previous state: []");
        }

        moveRepository.save(
                // TODO Introduce Move builder
                // TODO Calculate state
                new Move(
                        game.get(), user.get(), moveRequest.getSide(), StateCalculator.calculateNextState(
                        moveRequest).toString(), moveRequest.getMove()
                )
        );

        // TODO MoveResponse should contain board state and should not contain a move
        return generateMoveResponse(gameId, moveRequest);
    }

    @Override
    public MoveResponse getCurrentState(String gameId) {

        var moveList = moveRepository.findAllByGameId(gameId);
        var state = moveList.isEmpty() ? initialState() : getCurrentState(moveList);
        return new MoveResponse(gameId, state.toString());
    }

    private static String getCurrentState(List<Move> moveList) {
        return moveList.get(moveList.size() - 1).getState();
    }

    private static MoveResponse generateMoveResponse(String gameId, MoveRequest moveRequest) {
        var moveResponse = new MoveResponse();
        moveResponse.setGameId(gameId);
        moveResponse.setState(StateCalculator.calculateNextState(moveRequest).toString());
        return moveResponse;
    }

    // TODO moves should not be empty
    private static boolean isInconsistentGame(MoveRequest moveRequest, List<Move> moves) {
        return !getCurrentState(moves).equals(moveRequest.getState());
    }
}
