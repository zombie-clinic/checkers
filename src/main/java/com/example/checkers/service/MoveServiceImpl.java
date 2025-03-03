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

    private final PossibleMoveProvider possibleMoveProvider;

    private static State getCurrentState(List<Move> moveList) {
        String dark = moveList.getLast().getDark();
        String light = moveList.getLast().getLight();

        return new State(
                Arrays.stream(dark.split(",")).map(Integer::valueOf).toList(),
                Arrays.stream(light.split(",")).map(Integer::valueOf).toList());
    }

    @Transactional
    @Override
    public MoveResponse saveMove(String gameId, MoveRequest moveRequest) {
        Game game = validateAndGetGame(gameId);
        Player player = validateAndGetPlayer(moveRequest);

        List<Move> moves = moveRepository.findAllByGameId(gameId);

        if (moves.isEmpty()) {
            State startingState = Checkerboard.getStartingState();
            State resultingState = getResultingState(startingState, moveRequest);
            Move move = new Move(game, player, "LIGHT", moveRequest.getMove(),
                    resultingState.getDark().stream().map(String::valueOf).collect(Collectors.joining(",")),
                    resultingState.getLight().stream().map(String::valueOf).collect(Collectors.joining(",")));
            moveRepository.save(move);
            return generateMoveResponse(gameId, DARK);
        }

        if (isCapture(moveRequest)) {
            State currentState = getCurrentState(moveRepository.findAllByGameId(gameId));
            State afterCaptureState = generateAfterCaptureState(currentState, moveRequest);

            Move move = new Move(game, player, moveRequest.getSide(), moveRequest.getMove(),
                    afterCaptureState.getDark().stream().map(String::valueOf).collect(Collectors.joining(",")),
                    afterCaptureState.getLight().stream().map(String::valueOf).collect(Collectors.joining(",")));

            moveRepository.save(move);
            return generateMoveResponse(gameId, Side.valueOf(moveRequest.getSide()),
                    afterCaptureState);
        }

        // regular move
        var lastMove = moveRepository.findAllByGameId(gameId).getLast();
        var resultingState = getResultingState(
                new State(
                        Arrays.stream(lastMove.getDark().split(",")).map(Integer::valueOf).toList(),
                        Arrays.stream(lastMove.getLight().split(",")).map(Integer::valueOf).toList()
                ), moveRequest
        );

        moveRepository.save(
                // TODO Introduce Move builder
                // TODO Calculate state

                new Move(game, player, moveRequest.getSide(), moveRequest.getMove(),

                        resultingState.getDark().stream().map(String::valueOf).collect(Collectors.joining(",")),
                        resultingState.getLight().stream().map(String::valueOf).collect(Collectors.joining(","))));

        // TODO MoveResponse should contain board state and should not contain a move
        return generateMoveResponse(gameId, Side.valueOf(moveRequest.getSide()));
    }

    private State getResultingState(State originalState, MoveRequest moveRequest) {

        List<Integer> dark = new ArrayList<>(originalState.getDark());
        List<Integer> light = new ArrayList<>(originalState.getLight());

        State resultingState = new State();
        String[] split = moveRequest.getMove().split("-");

        int start = Integer.parseInt(split[0]);
        int dest = Integer.parseInt(split[1]);
        if (Side.valueOf(moveRequest.getSide()) == DARK) {
            dark.removeIf(e -> e.equals(start));
            dark.add(dest);
            resultingState.setDark(dark);
        } else {
            light.removeIf(e -> e.equals(start));
            light.add(dest);
            resultingState.setLight(light);
        }

        return resultingState;
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
    // On a side note - is it really needed? Players in a match could be represented by users
    @Override
    public MoveResponse generateMoveResponse(String gameId, Side side) {
        var moveList = moveRepository.findAllByGameId(gameId);
        if (moveList.isEmpty()) {
            var state = getStartingState();
            return new MoveResponse(gameId, state, side.name(),
                    getSimplifiedPossibleMoves(possibleMoveProvider.getPossibleMovesMap(side,
                            Checkerboard.state(state.getDark(), state.getLight()))));
        }
        var state = getCurrentState(moveList);
        Map<Integer, List<PossibleMove>> possibleMoves = possibleMoveProvider.getPossibleMovesMap(
                side, Checkerboard.state(state.getDark(), state.getLight())
        );
        return new MoveResponse(gameId, state, side.name(),
                getSimplifiedPossibleMoves(possibleMoves));
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

    public MoveResponse generateMoveResponse(String gameId, Side side, State afterCaptureState) {
        Map<Integer, List<PossibleMove>> possibleMoves =
                possibleMoveProvider.getPossibleMovesMap(side, Checkerboard.state(
                        afterCaptureState.getDark(),
                        afterCaptureState.getLight()
                ));
        return new MoveResponse(gameId, afterCaptureState, side.name(),
                getSimplifiedPossibleMoves(possibleMoves));
    }
}
