# Checkers Move and Capture Logic

This document details the move and capturing logic within the checkers application, based on the analysis of the Java source code.

## 1. Generation of Possible Moves (`PossibleMoveProviderImpl`)

The `PossibleMoveProviderImpl` class is responsible for determining all valid moves for a given piece or side.

### Key Methods:

*   **`getPossibleMovesForPieceInternal(Piece piece, State state)`**: This is the central method for generating moves for a specific piece. It iterates through all predefined diagonals on the checkerboard (obtained from `Checkerboard.getDiagonals()`) and calls the `getMoves()` method for each.
*   **`getPossibleMovesForSide(Side side, State state)`**: This method iterates over all pieces of a given `side` on the board (obtained via `StateUtils.getSide()`), calling `getPossibleMovesForPieceInternal()` for each piece to aggregate all possible moves for that side.

### Move Generation (`getMoves`):

The `getMoves()` method is where the distinction between regular pieces and kings, as well as dark and light pieces, influences move calculation.

*   **Diagonal Orientation**: Dark pieces generally move "down" the board (increasing cell numbers), and light pieces move "up" (decreasing cell numbers). The `diagonal` list is ordered accordingly based on `piece.isLight()`.
*   **King vs. Regular Piece**:
    *   If the piece `isKing` (determined by checking if the piece's position is in the `state.getKings()` set):
        *   `collectKingMoves()`: Gathers all valid moves in the "forward" direction along the diagonal.
        *   `collectBackwardKingMoves()`: Gathers all valid moves in the "backward" (reversed diagonal) direction.
    *   If the piece is a regular (non-king) piece:
        *   `collectForwardMoves()`: Gathers standard forward moves (non-captures) and forward captures.
        *   `collectBackwardCaptures()`: Gathers backward captures only. Regular pieces cannot move backward without capturing.

### Move Types and Prioritization:

1.  **Normal Moves**: A piece can move one step diagonally forward to an empty square.
    *   In `collectForwardMoves()`: `res.add(new PossibleMove(piece, nextSquare, false));`
    *   In `collectKingMoves()`: `res.add(new PossibleMove(piece, destSquare, false));` (when no capture is in progress or done).
2.  **Captures**:
    *   A piece captures an opponent's piece by jumping over it into an empty square immediately beyond it.
    *   Handled by `checkIfCaptureIsPossible()` which is called from `collectForwardMoves()`. This method checks if the square beyond the opponent's piece is empty.
        *   `return Optional.of(new PossibleMove(piece, diagonal.get(nextNextIdx), true));`
    *   King captures are handled within `collectKingMoves()`. A king can capture an opponent's piece if there's an empty square anywhere beyond it on the same diagonal, and can continue capturing if multiple captures are available in sequence (though this multi-capture continuation logic seems to be primarily handled by the `captureDone` and `captureInProgress` flags within a single call to `collectKingMoves`).

### Mandatory Capture Rule:

*   The `getPossibleMovesForPieceInternal()` method enforces the mandatory capture rule.
*   After collecting all potential moves (`moves.addAll(getMoves(...))`), it checks if any of these moves are captures:
    ```java
    if (moves.stream().anyMatch(PossibleMove::isCapture)) {
      return moves.stream().filter(PossibleMove::isCapture).toList();
    }
    ```
*   If there are any capture moves available for a piece, only the capture moves are returned. Normal (non-capture) moves are filtered out.

## 2. Capture Prioritization

As mentioned above, the mandatory capture rule is implemented in `PossibleMoveProviderImpl.getPossibleMovesForPieceInternal()`. If a piece has one or more available captures, it *must* make a capture. Non-capture moves for that piece are disregarded.

## 3. King Piece Movement and Capture

King pieces have distinct movement and capture capabilities compared to regular pieces.

### Determining a King:

*   A piece is identified as a king if its position is present in the `kings` set stored within the `State` object.
    ```java
    // In PossibleMoveProviderImpl.getMoves()
    Set<Integer> kings = state.getKings() == null ? Set.of() : state.getKings();
    boolean isKing = side == Side.DARK ?
        (state.getDark().contains(piece.position()) && kings.contains(piece.position()))
        : (state.getLight().contains(piece.position()) && kings.contains(piece.position()));
    ```

### King Movement:

*   **Forward and Backward**: Kings can move both forwards and backwards along diagonals. This is handled by `collectKingMoves()` being called for both the original diagonal and its reversed version (`collectBackwardKingMoves()`).
*   **Multiple Steps**: Kings can move multiple empty squares in any allowed diagonal direction, as long as those squares are unoccupied.
    *   In `collectKingMoves()`: The loop `for (int destSquare : moveDirection)` iterates through all squares in a given direction. As long as squares are free (`isSquareFree`) and no opponent piece blocks the path directly, moves are added.

### King Capture:

*   **Long-Range Captures**: Kings can capture an opponent's piece from any distance along a diagonal, provided the square immediately beyond the captured piece (and all intermediate squares between the king and the opponent, and the landing square) are empty.
    *   In `collectKingMoves()`:
        *   When an opponent's piece is encountered (`isSquareOccupiedByWhatSide(state, piece.oppositeSide(), destSquare)`), `captureInProgress` is set to `true`.
        *   If `captureInProgress` is true and an empty square is found (`isSquareFree(state, destSquare)`), a capture move is added: `res.add(new PossibleMove(piece, destSquare, true));`.
        *   The `captureDone` flag is used to indicate a capture has been made in the current path. The logic allows for further moves to be added as part of the same "turn" if they are also captures (chain captures), but the current implementation of `collectKingMoves` seems to allow multiple captures in a sequence if they are available along that specific diagonal path segment.
*   **Mandatory Capture**: Like regular pieces, if a king has a capture available, it must take it.

## 4. Board State Update (`StateUtils`, `MoveServiceImpl`)

Updating the board state after a move or capture involves piece addition, removal, and potentially changing a piece's king status.

### `StateUtils.generateAfterMoveOrCaptureState(State state, MoveRequest moveRequest)`:

This is the core method for calculating the new board state.

*   **Inputs**: Current `State` and the `MoveRequest` (which contains the move string like "1-5" or "1x10", and the side making the move).
*   **Parsing Move**: The start and destination squares are parsed from `moveRequest.getMove()`.
*   **Piece Movement**:
    *   A copy of the current dark, light, and kings sets are made.
    *   The piece is removed from its `start` square and added to its `dest` square for the appropriate side.
        ```java
        if (side == LIGHT) {
            light.removeIf(e -> e.equals(start));
            light.add(dest);
        } else {
            dark.removeIf(e -> e.equals(start));
            dark.add(dest);
        }
        ```
*   **Captured Piece Removal (if applicable)**:
    *   If the move string contains "x" (indicating a capture):
        *   The `determineCapturedPieceIdx()` method is called to find the square of the piece being captured.
        *   The captured piece is removed from the opponent's piece set.
            ```java
            if (isCaptureMove(moveRequest)) { // isCaptureMove checks for "x"
              // ...
              // For DARK making a capture:
              light.remove(determineCapturedPieceIdx(Side.valueOf(moveRequest.getSide()), start, dest));
              // For LIGHT making a capture:
              dark.remove(determineCapturedPieceIdx(Side.valueOf(moveRequest.getSide()), start, dest));
            }
            ```
*   **King Status Update (Movement)**: If a piece that was a king moves, its king status is preserved. It's removed from the `kings` set at the `start` position and added at the `dest` position.
    ```java
    boolean removed = kings.removeIf(e -> e.equals(start));
    if (removed) {
      kings.add(dest);
    }
    ```
*   **Return Value**: A new `State` object is returned with the updated piece positions and king statuses.

### `StateUtils.determineCapturedPieceIdx(Side side, Integer start, Integer end)`:

*   This method calculates the position of the piece being jumped over.
*   It iterates through the `Checkerboard.getDiagonals()`.
*   For the correct diagonal containing both `start` and `end` points (oriented based on the capturing `side`), it finds the midpoint between `start` and `end` which corresponds to the captured piece's square.
    ```java
    // Simplified logic:
    // int startIdx = d.indexOf(start);
    // int endIdx = d.indexOf(end);
    // return d.get((startIdx + endIdx) / 2);
    ```

### `MoveServiceImpl.saveMove(UUID gameId, MoveRequest moveRequest)`:

This service method orchestrates saving a move and updating the game state.

1.  **Retrieves Current State**: It fetches the current game state, either the starting state or the state from the last recorded move (`getCurrentStateFromMove()`).
2.  **Generates New State**: It calls `StateUtils.generateAfterMoveOrCaptureState(currentState, moveRequest)` to get the state after the current move.
3.  **Creates Move Record**: A `Move` entity is created, storing the new dark and light piece positions (as comma-separated strings) and the set of kings.
4.  **Handles King Promotion**: This is done *after* `generateAfterMoveOrCaptureState`.
5.  **Saves Move**: The `moveRepository.save(move)` persists the new move and its resulting state.

## 5. King Promotion (`MoveServiceImpl`)

King promotion occurs when a regular piece reaches the opponent's back rank.

### `MoveServiceImpl.saveMove()`:

*   After the new state is generated by `StateUtils.generateAfterMoveOrCaptureState()`, and *before* saving the `Move` record, `MoveServiceImpl` explicitly checks for king promotion:
    ```java
    Integer dest = Integer.valueOf(move.getMove().split("[-x]")[1]);
    if (!move.getKings().contains(dest) && isMoveResultsInKings(Side.valueOf(move.getSide()), dest)) {
      Set<Integer> currentKings = move.getKings();
      currentKings.add(dest);
      move.setKings(currentKings);
    }
    ```
*   **`isMoveResultsInKings(Side side, Integer dest)`**: This private helper method determines if a piece should be promoted.
    *   If `side == DARK` (dark piece), promotion occurs if `dest` is one of the squares `29, 30, 31, 32` (the light side's back rank).
    *   If `side == LIGHT` (light piece), promotion occurs if `dest` is one of the squares `1, 2, 3, 4` (the dark side's back rank).
*   If a piece reaches the promotion rank and is not already a king, its destination square (`dest`) is added to the `kings` set of the `Move` object being saved. `StateUtils.generateAfterMoveOrCaptureState` does *not* handle promotion itself; it only preserves king status if a king moves. The promotion is an explicit step in `MoveServiceImpl`.

## 6. Key Classes and Methods

*   **`PossibleMoveProviderImpl`**:
    *   `getPossibleMovesForPieceInternal(Piece, State)`: Generates moves for a single piece.
    *   `getPossibleMovesForSide(Side, State)`: Generates moves for all pieces of a side.
    *   `getMoves(State, Piece, LinkedList<Integer>)`: Core logic for finding moves along a diagonal.
    *   `collectForwardMoves(...)`: For regular piece forward moves/captures.
    *   `collectBackwardCaptures(...)`: For regular piece backward captures.
    *   `collectKingMoves(...)`: For king moves/captures (both directions via reversal).
    *   `checkIfCaptureIsPossible(...)`: Helper to validate a capture.
*   **`MoveServiceImpl`**:
    *   `saveMove(UUID, MoveRequest)`: Orchestrates processing a move, updating state, handling promotion, and saving.
    *   `isMoveResultsInKings(Side, Integer)`: Checks if a move results in king promotion.
    *   `getCurrentStateFromMove(UUID)`: Retrieves the current board state from move history.
*   **`StateUtils`**:
    *   `generateAfterMoveOrCaptureState(State, MoveRequest)`: Calculates the new board state after a move or capture, including piece removal.
    *   `determineCapturedPieceIdx(Side, Integer, Integer)`: Identifies the piece captured during a jump.
    *   `isEmptyCell(int, State)`: Checks if a board square is empty.
    *   `getSide(Side, State)`: Retrieves all pieces of a specific side.
*   **`Checkerboard`**:
    *   `getDiagonals()`: Provides the list of all playable diagonals on the board.
    *   `getStartingState()`: Provides the initial setup of pieces.
*   **Domain Objects**:
    *   `State`: Represents the board state (sets of dark pieces, light pieces, and kings).
    *   `Piece`: Represents a checker piece (position and side).
    *   `PossibleMove`: Represents a potential move (origin, destination, isCapture flag).
    *   `MoveRequest`: DTO carrying move information from the client.
    *   `Move`: Entity representing a persisted move in the game history.

This concludes the detailed description of the move and capturing logic.
