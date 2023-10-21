package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.model.MoveRequest;
import com.example.demo.model.MoveResponse;
import com.example.demo.model.State;
import com.example.demo.persistence.GameRepository;
import com.example.demo.persistence.MoveRepository;
import com.example.demo.persistence.PlayerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class MoveServiceImpl implements MoveService {

    public static String INITIAL_BLACK_STATE = "1,2,3,4,5,6,7,8,9,10,11,12";
    public static String INITIAL_WHITE_STATE = "11,12,13,14,15,16,17,18,19,20,21,22";

    private final MoveRepository moveRepository;

    private final GameRepository gameRepository;

    private final PlayerRepository playerRepository;

    private final BoardService boardService;

    // TODO Use mapper or json parser to validate move request, e.g.
    @Transactional
    @Override
    public MoveResponse saveMove(String gameId, MoveRequest moveRequest) {
        Optional<Game> game = gameRepository.findGameById(gameId);
        if (game.isEmpty()) {
            throw new IllegalArgumentException("No game found");
        }

        Optional<Player> user = playerRepository.findById(moveRequest.getPlayerId());
        if (user.isEmpty()) {
            throw new IllegalArgumentException("No user found");
        }

        List<Move> moves = moveRepository.findAllByGameId(gameId);

        if (moves.isEmpty()) {
            moveRepository.save(
                    new Move(
                            game.get(), user.get(), "white", moveRequest.getMove(),
                            INITIAL_BLACK_STATE,
                            INITIAL_WHITE_STATE
                    )
            );
            return generateMoveResponse(gameId, moveRequest);
        }

        if (isInconsistentGame(moveRequest, moves)) {
            throw new IllegalStateException("Inconsistent game, provide a valid move, here is you previous state: []");
        }

        String[] split = moveRequest.getMove().split("-");

        if (moveRequest.getSide().equals("white")) {
            moveRequest.getState().getWhite().remove(Integer.valueOf(split[0]));
            moveRequest.getState().getWhite().add(Integer.valueOf(split[1]));
        } else {
            moveRequest.getState().getBlack().remove(Integer.valueOf(split[0]));
            moveRequest.getState().getBlack().add(Integer.valueOf(split[1]));
        }

        moveRepository.save(
                // TODO Introduce Move builder
                // TODO Calculate state

                new Move(
                        game.get(), user.get(), moveRequest.getSide(), moveRequest.getMove(),
                        moveRequest.getState().getBlack().stream().map(String::valueOf).collect(Collectors.joining(",")),
                        moveRequest.getState().getWhite().stream().map(String::valueOf).collect(Collectors.joining(","))
                )
        );

        // TODO MoveResponse should contain board state and should not contain a move
        return generateMoveResponse(gameId, moveRequest);
    }

    @Override
    public MoveResponse getCurrentState(String gameId) {

        var moveList = moveRepository.findAllByGameId(gameId);
        State state = moveList.isEmpty() ? new State(
                Arrays.stream(INITIAL_BLACK_STATE.split(",")).map(Integer::valueOf).toList(),
                Arrays.stream(INITIAL_WHITE_STATE.split(",")).map(Integer::valueOf).toList()
        ) : getCurrentState(moveList);
        Map<Integer, List<PossibleMove>> possibleMoves = boardService.getPossibleMoves(
                Side.BLACK, state);
        return new MoveResponse(gameId, state, possibleMoves);
    }

    private static State getCurrentState(List<Move> moveList) {
        String black = moveList.get(moveList.size() - 1).getBlack();
        String white = moveList.get(moveList.size() - 1).getWhite();

        return new State(
                Arrays.stream(black.split(",")).map(Integer::valueOf).toList(),
                Arrays.stream(white.split(",")).map(Integer::valueOf).toList()
        );
    }

    private MoveResponse generateMoveResponse(String gameId, MoveRequest moveRequest) {
        var moveResponse = new MoveResponse();
        moveResponse.setGameId(gameId);
        moveResponse.setState(new State(
                moveRequest.getState().getBlack(),
                moveRequest.getState().getWhite()
        ));
        moveResponse.setPossibleMoves(boardService.getPossibleMoves(
                        Side.valueOf(moveRequest.getSide().toUpperCase()), moveRequest.getState()
                ));

        return moveResponse;
    }

    @SneakyThrows
    // TODO moves should not be empty
    private static boolean isInconsistentGame(MoveRequest moveRequest, List<Move> moves) {
        State clientState = moveRequest.getState();
        State serverState = getCurrentState(moves);
        return !amountOfFiguresMatches(clientState, serverState) || !positionsMatch(clientState, serverState);
    }

    private static boolean positionsMatch(State clientState, State serverState) {
        return new HashSet<>(serverState.getBlack()).containsAll(clientState.getBlack()) && new HashSet<>(serverState.getWhite()).containsAll(clientState.getWhite());
    }

    private static boolean amountOfFiguresMatches(State requestedCheck, State current) {
        return requestedCheck.getBlack().size() == current.getBlack().size() && requestedCheck.getWhite().size() == current.getWhite().size();
    }
}
