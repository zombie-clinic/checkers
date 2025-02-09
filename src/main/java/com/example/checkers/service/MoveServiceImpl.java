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

import static com.example.checkers.domain.Checkerboard.getStartingState;
import static com.example.checkers.domain.Side.DARK;
import static com.example.checkers.domain.Side.LIGHT;

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
            State state = moveRequest.getState();
            List<Integer> dark = state.getDark();
            List<Integer> light = state.getLight();
            int start = Integer.parseInt(split[0]);
            int dest = Integer.parseInt(split[1]);
            if (Side.valueOf(moveRequest.getSide()) == DARK) {
                dark.removeIf(e -> e.equals(start));
                dark.add(dest);
                state.setDark(dark);
            } else {
                light.removeIf(e -> e.equals(start));
                light.add(dest);
                state.setLight(light);
            }

            moveRequest.setState(state);

            Move move = new Move(game, player, "LIGHT", moveRequest.getMove(),
                    moveRequest.getState().getDark().stream().map(String::valueOf).collect(Collectors.joining(",")),
                    moveRequest.getState().getLight().stream().map(String::valueOf).collect(Collectors.joining(",")));

            moveRepository.save(move);
            return generateMoveResponse(gameId, DARK);
        }

        if (isInconsistentGame(moveRequest, moves)) {
            throw new IllegalStateException("Inconsistent game, provide a valid move, here is you previous state: []");
        }

        if (isCapture(moveRequest)) {
            State afterCaptureState = captureService.generateAfterCaptureState(moveRequest);
            Move move = new Move(game, player, moveRequest.getSide(), moveRequest.getMove(),
                    afterCaptureState.getDark().stream().map(String::valueOf).collect(Collectors.joining(",")),
                    afterCaptureState.getLight().stream().map(String::valueOf).collect(Collectors.joining(",")));

            moveRepository.save(move);
            return generateMoveResponse(gameId, Side.valueOf(moveRequest.getSide()), afterCaptureState);
        }

        // regular move
        String[] split = moveRequest.getMove().split("-");

        if (moveRequest.getSide().equals(LIGHT.toString())) {
            moveRequest.getState().getLight().remove(Integer.valueOf(split[0]));
            moveRequest.getState().getLight().add(Integer.valueOf(split[1]));
        } else {
            moveRequest.getState().getDark().remove(Integer.valueOf(split[0]));
            moveRequest.getState().getDark().add(Integer.valueOf(split[1]));
        }

        moveRepository.save(
                // TODO Introduce Move builder
                // TODO Calculate state

                new Move(game, player, moveRequest.getSide(), moveRequest.getMove(), moveRequest.getState().getDark().stream().map(String::valueOf).collect(Collectors.joining(",")), moveRequest.getState().getLight().stream().map(String::valueOf).collect(Collectors.joining(","))));

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
            var state = getStartingState();
            return new MoveResponse(gameId, state, side.name(), getSimplifiedPossibleMoves(boardService.getPossibleMoves(side, state)));
        }
        var state = getCurrentState(moveList);
        // TODO Fix it, need to determine whose move it is
        Map<Integer, List<PossibleMove>> possibleMoves = boardService.getPossibleMoves(side, state);
        return new MoveResponse(gameId, state, side.name(), getSimplifiedPossibleMoves(possibleMoves));
    }

    private Map<Integer, List<PossibleMoveSimplified>> getSimplifiedPossibleMoves(Map<Integer, List<PossibleMove>> moves) {
       Map<Integer, List<PossibleMoveSimplified>> map = new HashMap<>();
       for (Map.Entry<Integer, List<PossibleMove>> entry: moves.entrySet()) {
           map.put(entry.getKey(), entry.getValue().stream()
                   .map(PossibleMoveSimplified::fromMove).toList());
       }
       return map;
}
    public MoveResponse generateMoveResponse(String gameId, Side side, State afterCaptureState) {
        Map<Integer, List<PossibleMove>> possibleMoves = boardService.getPossibleMoves(side, afterCaptureState);
        return new MoveResponse(gameId, afterCaptureState, side.name(), getSimplifiedPossibleMoves(possibleMoves));
    }



    private static State getCurrentState(List<Move> moveList) {
        String dark = moveList.getLast().getDark();
        String light = moveList.getLast().getLight();

        return new State(
                Arrays.stream(dark.split(",")).map(Integer::valueOf).toList(),
                Arrays.stream(light.split(",")).map(Integer::valueOf).toList());
    }

    @SneakyThrows
    // TODO moves should not be empty
    private static boolean isInconsistentGame(MoveRequest moveRequest, List<Move> moves) {
        State clientState = moveRequest.getState();
        State serverState = getCurrentState(moves);
        return !amountOfFiguresMatches(clientState, serverState) || !positionsMatch(clientState, serverState);
    }

    private static boolean positionsMatch(State clientState, State serverState) {
        return new HashSet<>(serverState.getDark()).containsAll(clientState.getDark()) && new HashSet<>(serverState.getLight()).containsAll(clientState.getLight());
    }

    private static boolean amountOfFiguresMatches(State requestedCheck, State current) {
        return requestedCheck.getDark().size() == current.getDark().size() && requestedCheck.getLight().size() == current.getLight().size();
    }
}
