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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.checkers.domain.Checkerboard.getStartingState;
import static com.example.checkers.domain.Side.DARK;
import static com.example.checkers.domain.Side.LIGHT;

@Slf4j
@RequiredArgsConstructor
@Service
public class MoveServiceImpl implements MoveService {

    private final MoveRepository moveRepository;

    private final GameRepository gameRepository;

    private final PlayerRepository playerRepository;

    private final PossibleMoveProvider possibleMoveProvider;

    static State getCurrentState(List<Move> moveList) {
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
        return !amountOfFiguresMatches(clientState, serverState) || !positionsMatch(clientState,
                serverState);
    }

    private static boolean positionsMatch(State clientState, State serverState) {
        return new HashSet<>(serverState.getDark()).containsAll(clientState.getDark()) && new HashSet<>(serverState.getLight()).containsAll(clientState.getLight());
    }

    private static boolean amountOfFiguresMatches(State requestedCheck, State current) {
        return requestedCheck.getDark().size() == current.getDark().size() && requestedCheck.getLight().size() == current.getLight().size();
    }

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
            throw new IllegalStateException("Inconsistent game, provide a valid move, here is you" +
                    " previous state: []");
        }

        if (isCapture(moveRequest)) {
            State currentState = getCurrentState(moveRepository.findAllByGameId(gameId));
            State afterCaptureState = generateAfterCaptureState(currentState, moveRequest);

            Move move = new Move(game, player, moveRequest.getSide(), moveRequest.getMove(),
                    afterCaptureState.getDark().stream().map(String::valueOf).collect(Collectors.joining(",")),
                    afterCaptureState.getLight().stream().map(String::valueOf).collect(Collectors.joining(",")));

            moveRepository.save(move);


            Map<Integer, List<PossibleMove>> possibleMoves =
                    possibleMoveProvider.getPossibleMovesMap(Side.valueOf(moveRequest.getSide()), Checkerboard.state(
                            afterCaptureState.getDark(),
                            afterCaptureState.getLight()
                    ));
            return new MoveResponse(gameId, afterCaptureState, Side.valueOf(moveRequest.getSide()).name(),
                    getSimplifiedPossibleMoves(possibleMoves));
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

                new Move(game, player, moveRequest.getSide(), moveRequest.getMove(),
                        moveRequest.getState().getDark().stream().map(String::valueOf).collect(Collectors.joining(",")), moveRequest.getState().getLight().stream().map(String::valueOf).collect(Collectors.joining(","))));

        // TODO MoveResponse should contain board state and should not contain a move
        return generateMoveResponse(gameId, Side.valueOf(moveRequest.getSide()));
    }

    State generateAfterCaptureState(State state, MoveRequest moveRequest) {

        // TODO Database call could be done earlier
        State calculated;

        Integer start = Integer.valueOf(moveRequest.getMove().split("[x\\-]")[0]);
        Integer dest = Integer.valueOf(moveRequest.getMove().split("[x\\-]")[1]);


        if (Side.valueOf(moveRequest.getSide()) == DARK) {
            var darkPieces = new ArrayList<>(state.getDark());
            var lightPieces = new ArrayList<>(state.getLight());
            darkPieces.removeIf(el -> Objects.equals(el, start));
            darkPieces.add(dest);
            if (moveRequest.getMove().contains("x")) {
                lightPieces.remove(determineCapturedPieceIdx(Side.valueOf(moveRequest.getSide()),
                    start,
                        dest));
            }
            calculated = new State(
                    darkPieces, lightPieces
            );
        } else {
            var darkPieces = new ArrayList<>(state.getDark());
            var lightPieces = new ArrayList<>(state.getLight());
            lightPieces.removeIf(el -> Objects.equals(el, start));
            lightPieces.add(dest);
            if (moveRequest.getMove().contains("x")) {
                darkPieces.remove(determineCapturedPieceIdx(Side.valueOf(moveRequest.getSide()),
                    start,
                        dest));
            }
            calculated = new State(
                    darkPieces, lightPieces
            );

        }

        return calculated;
    }

    private Integer determineCapturedPieceIdx(Side side, Integer start, Integer end) {
        for (LinkedList<Integer> diagonal : Checkerboard.getDiagonals()) {
            LinkedList<Integer> d;
            if (side == LIGHT) {
                d = new LinkedList<>(diagonal.reversed());
            } else {
                d = new LinkedList<>(diagonal);
            }
            if (d.contains(start) && d.contains(end)) {
                int startIdx = d.indexOf(start);
                int endIdx = d.indexOf(end);
                return d.get((startIdx + endIdx) / 2);
            }
        }
        throw new IllegalStateException(String.format("Trying to determine impossible capture: " +
                        "%s%s",
                start, end));
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

        // game begins
        if (moveList.isEmpty()) {
            var state = getStartingState();
            return new MoveResponse(gameId, state, side.name(),
                    getSimplifiedPossibleMoves(possibleMoveProvider.getPossibleMovesMap(side,
                            Checkerboard.state(state.getDark(), state.getLight()))));
        }

        var state = getCurrentState(moveList);
        // regular move, game in progress
        Map<Integer, List<PossibleMove>> possibleMoves = possibleMoveProvider.getPossibleMovesMap(
                side, Checkerboard.state(state.getDark(), state.getLight())
        );
        Map<Integer, List<PossibleMoveSimplified>> simplifiedPossibleMoves = getSimplifiedPossibleMoves(possibleMoves);

        Map<Integer, List<PossibleMoveSimplified>> res = new HashMap<>();

        for (Map.Entry<Integer, List<PossibleMoveSimplified>> e: simplifiedPossibleMoves.entrySet()) {
            if (e.getValue().stream().anyMatch(PossibleMoveSimplified::isCapture)) {
                res.put(e.getKey(), e.getValue());
            }
        }

        // return only captures, if there are any
        if (!res.isEmpty()) {
            Side currentSide = Side.valueOf(side.name());
            Side lastMoveSide = Side.valueOf(moveList.getLast().getSide());

            log.info("Current move side: {}", currentSide);
            log.info("Previous (last) move side: {} last move: {}", lastMoveSide, moveList.getLast().getMove());

            if (currentSide == lastMoveSide) {
                Integer lastMoveCell = Integer.valueOf(moveList.getLast().getMove().split("x")[0]);
                Map<Integer, List<PossibleMoveSimplified>> resFilteredForChainCaptures = new HashMap<>();
                resFilteredForChainCaptures.put(lastMoveCell, res.get(lastMoveCell));
                return new MoveResponse(gameId, state, currentSide.name(),
                        resFilteredForChainCaptures
                );
            }
            return new MoveResponse(gameId, state, currentSide.name(), res);
        }

        return new MoveResponse(gameId, state, side.name(),
                simplifiedPossibleMoves);
    }

    private Map<Integer, List<PossibleMoveSimplified>> getSimplifiedPossibleMoves(Map<Integer,
            List<PossibleMove>> moves) {
        Map<Integer, List<PossibleMoveSimplified>> map = new HashMap<>();
        for (Map.Entry<Integer, List<PossibleMove>> entry : moves.entrySet()) {
            map.put(entry.getKey(), entry.getValue().stream()
                    .map(PossibleMoveSimplified::fromMove).toList());
        }
        return map;
    }
}
