package com.example.checkers.service;

import com.example.checkers.domain.*;
import com.example.checkers.model.MoveRequest;
import com.example.checkers.model.MoveResponse;
import com.example.checkers.model.State;
import com.example.checkers.persistence.GameRepository;
import com.example.checkers.persistence.MoveRepository;
import com.example.checkers.persistence.PlayerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.checkers.domain.Board.getInitialState;
import static com.example.checkers.domain.Side.BLACK;
import static com.example.checkers.domain.Side.WHITE;

@RequiredArgsConstructor
@Service
public class MoveServiceImpl implements MoveService {

    private final MoveRepository moveRepository;

    private final GameRepository gameRepository;

    private final PlayerRepository playerRepository;

    private final BoardService boardService;

    private final CaptureService captureService;

    // TODO Use mapper or json parser to validate move request, e.g.
    @Transactional
    @Override
    public MoveResponse saveMove(String gameId, MoveRequest moveRequest) {
        Game game = validateAndGetGame(gameId);
        Player player = validateAndGetPlayer(moveRequest);

        List<Move> moves = moveRepository.findAllByGameId(gameId);

        if (moves.isEmpty()) {

            String[] split = moveRequest.getMove().split("-");

            moveRequest.getState().getBlack().remove(Integer.valueOf(split[0]));
            moveRequest.getState().getBlack().add(Integer.valueOf(split[1]));

            Move move = new Move(game, player, "BLACK", moveRequest.getMove(),
                    moveRequest.getState().getBlack().stream().map(String::valueOf).collect(Collectors.joining(",")),
                    moveRequest.getState().getWhite().stream().map(String::valueOf).collect(Collectors.joining(",")));

            moveRepository.save(move);
            return generateMoveResponse(gameId, BLACK);
        }

        if (isInconsistentGame(moveRequest, moves)) {
            throw new IllegalStateException("Inconsistent game, provide a valid move, here is you previous state: []");
        }

        if (isCapture(moveRequest)) {
            State afterCaptureState = captureService.generateAfterCaptureState(moveRequest);
            Move move = new Move(game, player, moveRequest.getSide(), moveRequest.getMove(),
                    afterCaptureState.getBlack().stream().map(String::valueOf).collect(Collectors.joining(",")),
                    afterCaptureState.getWhite().stream().map(String::valueOf).collect(Collectors.joining(",")));

            moveRepository.save(move);
            return generateMoveResponse(gameId, Side.valueOf(moveRequest.getSide()), afterCaptureState);
        }

        // regular move
        String[] split = moveRequest.getMove().split("-");

        if (moveRequest.getSide().equals(WHITE.toString())) {
            moveRequest.getState().getWhite().remove(Integer.valueOf(split[0]));
            moveRequest.getState().getWhite().add(Integer.valueOf(split[1]));
        } else {
            moveRequest.getState().getBlack().remove(Integer.valueOf(split[0]));
            moveRequest.getState().getBlack().add(Integer.valueOf(split[1]));
        }

        moveRepository.save(
                // TODO Introduce Move builder
                // TODO Calculate state

                new Move(game, player, moveRequest.getSide(), moveRequest.getMove(), moveRequest.getState().getBlack().stream().map(String::valueOf).collect(Collectors.joining(",")), moveRequest.getState().getWhite().stream().map(String::valueOf).collect(Collectors.joining(","))));

        // TODO MoveResponse should contain board state and should not contain a move
        return generateMoveResponse(gameId, Side.valueOf(moveRequest.getSide()));
    }

    private boolean isCapture(MoveRequest moveRequest) {
        /** TODO
         * Where should it be deduced from?
         * From request object?
         * From previous possible capture moves?
         * From parsed move? (x) as it is now
         */
        return moveRequest.getMove().contains("x");
    }

    private Player validateAndGetPlayer(MoveRequest moveRequest) {
        Optional<Player> player = playerRepository.findById(moveRequest.getPlayerId());
        if (player.isEmpty()) {
            throw new IllegalArgumentException("No player found");
        }
        return player.get();
    }

    private Game validateAndGetGame(String gameId) {
        Optional<Game> game = gameRepository.findGameById(gameId);
        if (game.isEmpty()) {
            throw new IllegalArgumentException("No game found");
        }
        return game.get();
    }

    // TODO Add a distinction between Player and User
    // User becomes Player when a game starts, Player has a user id and a side
    @Override
    public MoveResponse generateMoveResponse(String gameId, Side side) {

        var moveList = moveRepository.findAllByGameId(gameId);
        if (moveList.isEmpty()) {
            var state = getInitialState();
            return new MoveResponse(gameId, state, boardService.getPossibleMoves(side, state));
        }
        var state = getCurrentState(moveList);
        // TODO Fix it, need to determine whose move it is
        Map<Integer, List<PossibleMove>> possibleMoves = boardService.getPossibleMoves(side, state);
        return new MoveResponse(gameId, state, possibleMoves);
    }

    public MoveResponse generateMoveResponse(String gameId, Side side, State afterCaptureState) {
        Map<Integer, List<PossibleMove>> possibleMoves = boardService.getPossibleMoves(side, afterCaptureState);
        return new MoveResponse(gameId, afterCaptureState, possibleMoves);
    }



    private static State getCurrentState(List<Move> moveList) {
        String black = moveList.getLast().getBlack();
        String white = moveList.getLast().getWhite();

        return new State(
                Arrays.stream(black.split(",")).map(Integer::valueOf).toList(),
                Arrays.stream(white.split(",")).map(Integer::valueOf).toList());
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
