# Final Checkers Logic Analysis Report

This report provides a comprehensive analysis of the current move and capturing logic within the checkers application, followed by proposals for improvement.

# Part 1: Current Move and Capturing Logic

This section details the move and capturing logic within the checkers application, based on the analysis of the Java source code.

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

# Part 2: Proposed Improvements for Checkers Move and Capture Logic

Based on the analysis of the codebase, several areas in the move and capturing logic can be improved for better maintainability, performance, and clarity.

## 1. State Representation in `Move` Entity and Game State Reconstruction

**Problem:**

*   **String-Based State in `Move` Entity:** The `Move` entity currently stores the board state (`dark`, `light` piece positions) as comma-separated strings. The `kings` are stored as a `Set<Integer>`, which is better, but still part of a denormalized representation within each move. This approach has several drawbacks:
    *   **Error-Prone:** Manual string parsing (e.g., `split(",")`, `map(Integer::valueOf)`) is susceptible to errors if the format changes or if data is corrupted.
    *   **Inefficient Queries:** It's impossible to query the state of specific squares or pieces directly from the database without fetching and parsing these strings.
    *   **Poor Readability:** String-based states are hard to inspect and debug directly in the database.
*   **State Reconstruction Inefficiency:** To get the current state of a game, `MoveServiceImpl.getCurrentStateFromMove()` fetches all moves for the game and reconstructs the state from the *last* move's string representation. While it only uses the last move, this still relies on string parsing. If it ever needed to replay moves, this would be very slow.

**Proposal:**

*   **Structured State Storage:**
    *   Modify the `Move` entity to store a representation of the board state that is more structured. Instead of `String dark` and `String light`, consider:
        *   **JSONB/JSON Type:** If the database supports it (e.g., PostgreSQL), store the `State` object (or a simplified version of it, perhaps just the piece lists and kings) directly as a JSONB column. This allows for structured data while being relatively easy to map with JPA (e.g., using `@Convert` with a custom `AttributeConverter` or native Hibernate types).
        *   **Dedicated State Entity (Less Ideal for `Move`):** For the `Move` entity itself, storing a full related `State` entity per move might be too heavy if `State` becomes complex. However, this is a good approach for the *current* game state (see next point).
*   **Type Safety for Piece Sets:** The `State` record (`Set<Integer> dark, Set<Integer> light, Set<Integer> kings`) is good. Ensure this structure is consistently used. When serializing for the database within a `Move` (if not using JSON for the whole state), these sets could be stored in a more structured way than simple comma-delimited strings, perhaps three separate text columns that are clearly identified.
*   **JPA Converters:** Use JPA `AttributeConverter`s to handle the transformation between `Set<Integer>` and their string representations if direct JSON mapping is not used. This centralizes the conversion logic and makes entity code cleaner.

    ```java
    // Example AttributeConverter (conceptual)
    // public class IntegerSetConverter implements AttributeConverter<Set<Integer>, String> {
    //    @Override
    //    public String convertToDatabaseColumn(Set<Integer> attribute) { /* ... */ }
    //    @Override
    //    public Set<Integer> convertToEntityAttribute(String dbData) { /* ... */ }
    // }
    //
    // In Move.java:
    // @Convert(converter = IntegerSetConverter.class)
    // private Set<Integer> darkPieces;
    // @Convert(converter = IntegerSetConverter.class)
    // private Set<Integer> lightPieces;
    ```

## 2. Current State Calculation and Storage

**Problem:**

*   `MoveServiceImpl.getCurrentStateFromMove()` calculates the current board state by fetching the list of all moves for a game and then parsing the state from the very last `Move` entity. This is inefficient as it requires a database query that might return many moves, even though only the last one is used for state reconstruction.
*   The `Game` entity itself has a `startingState` as a JSON-like string but doesn't directly hold the *current* `State` of the game.

**Proposal:**

*   **Store Current State in `Game` Entity:**
    *   Add a field to the `Game` entity to directly store the current `State` of the board.
        ```java
        // In Game.java
        // Option 1: Embed State directly (if simple enough and DB supports JSON/structured types well)
        // @Embedded // Or use @Convert for JSON
        // private State currentState;

        // Option 2: Store as structured columns (similar to Move entity suggestion)
        // @ElementCollection
        // @CollectionTable(name = "game_current_dark_pieces", joinColumns = @JoinColumn(name = "game_id"))
        // @Column(name = "piece_position")
        // private Set<Integer> currentDarkPieces;
        //
        // @ElementCollection
        // @CollectionTable(name = "game_current_light_pieces", joinColumns = @JoinColumn(name = "game_id"))
        // @Column(name = "piece_position")
        // private Set<Integer> currentLightPieces;
        //
        // @ElementCollection
        // @CollectionTable(name = "game_current_kings", joinColumns = @JoinColumn(name = "game_id"))
        // @Column(name = "piece_position")
        // private Set<Integer> currentKings;
        ```
    *   When a game is created, initialize `currentState` with `Checkerboard.getStartingState()`.
    *   After each move is successfully processed in `MoveServiceImpl.saveMove()`, update the `game.setCurrentState(newState)` and save the `Game` entity along with the `Move` entity.
*   **Benefits:**
    *   **Efficiency:** Retrieving the current state becomes a direct fetch from the `Game` entity, eliminating the need to query and process the `Move` history.
    *   **Simplicity:** Simplifies `MoveServiceImpl.getCurrentStateFromMove()` to just `game.getCurrentState()`.
    *   **Atomicity:** The game's current state and the move that led to it can be updated transactionally.

## 3. Clarity of `PossibleMoveProviderImpl`

**Problem:**

*   **Backward Captures for Non-Kings:** The method `collectBackwardCaptures()` calls `collectForwardMoves()` with a reversed diagonal and a boolean flag `isBackwardsCaptureCheck = true`. This flag is used inside `collectForwardMoves()` to prevent adding non-capture moves. While functional, this makes `collectForwardMoves()` do two slightly different things based on a flag, reducing its single responsibility and clarity.
*   **King Movement/Capture Logic (`collectKingMoves`):**
    *   The `collectKingMoves` method handles both forward and backward moves (when called with a reversed diagonal by `collectBackwardKingMoves`).
    *   The logic with `captureInProgress` and `captureDone` flags to handle sequences of empty squares and captures can be hard to follow. It tries to manage state (like whether a capture has just been made in the current scan along the diagonal) within the loop.
    *   It seems to allow adding multiple captures if they are available one after another in the *same* direction during a single scan. While checkers kings *can* multi-jump, this is usually handled by the player making subsequent capture moves, not by the move generator providing a single "move" that encompasses multiple jumps. The current logic might be generating "long capture moves" instead of single jump captures. If it's intended to generate only the *first* capture in a sequence, the logic could be simplified.

**Proposal:**

*   **Refactor `collectForwardMoves` and `collectBackwardCaptures`:**
    *   Create a more specific method for backward captures for non-kings, e.g., `collectBackwardManCaptures(State state, Piece piece, LinkedList<Integer> diagonal, ArrayList<PossibleMove> res)`.
    *   This new method would only contain the logic for checking one step backward for an opponent and then one step further for an empty landing spot.
    *   `collectForwardMoves` would then be simplified to only handle forward moves (capture or non-capture) for non-kings, removing the `isBackwardsCaptureCheck` flag.
*   **Refactor `collectKingMoves`:**
    *   **Separate Scan Directions:** Instead of passing reversed diagonals, have `collectKingMovesInDirection(State state, Piece piece, LinkedList<Integer> diagonalSegment, ArrayList<PossibleMove> res)`. Call this for both forward and backward segments from the king's position.
    *   **Simplify Capture Logic:**
        *   Iterate along the diagonal segment.
        *   If an own piece is encountered, stop for this segment.
        *   If an opponent piece is encountered:
            *   Check the *next* square. If it's empty, add this as a *single* capture move and then **stop scanning further in this direction for this particular call**. The ability for kings to make multiple jumps is typically handled by the game engine allowing another turn/move if the previous move was a capture and further captures are available from the new position. Generating all segments of a multi-jump as one "PossibleMove" might be overly complex or not align with standard game flow.
            *   If the square beyond the opponent is occupied, stop (blocked).
        *   If an empty square is encountered (and no opponent was just jumped), add it as a normal (non-capture) move. Continue scanning.
    *   This simplification makes each call to `collectKingMovesInDirection` responsible for finding single-step non-captures or single-jump captures in one direction. The mandatory capture rule (`getPossibleMovesForPieceInternal`) will still ensure captures are prioritized.

## 4. `StateUtils.determineCapturedPieceIdx`

**Problem:**

*   The current implementation of `determineCapturedPieceIdx(Side side, Integer start, Integer end)` iterates through *all* diagonals (`Checkerboard.getDiagonals()`) on the board, reverses one if necessary, and then checks if both `start` and `end` are in that diagonal to find the midpoint. This is inefficient. Given a `start` and `end` of a capture, the captured piece is always at the arithmetic mean of their coordinates, assuming a consistent numbering system that reflects adjacency.

**Proposal:**

*   **Direct Calculation:**
    *   The captured piece's square number is directly determinable from the `start` and `end` squares of a jump. Since jumps are always two steps along a diagonal, the captured piece is the one "in the middle".
    *   If the board numbering allows for it (e.g., squares are numbered 1-32, and diagonal adjacencies have consistent numerical differences), the captured piece's index can often be calculated directly, for example, as `(start + end) / 2`.
    *   **Prerequisites for Direct Calculation:** This relies on the board numbering system. The current system (1-32 for playable squares) should support this. For any two squares `start` and `end` that form a valid jump:
        *   They must be on the same diagonal.
        *   The difference `|start - end|` corresponds to two steps along that diagonal.
        *   The captured piece is at `start + (end - start) / 2`.
    *   A check might be needed to ensure `start` and `end` are actually valid jump coordinates (e.g., `abs(start - end)` corresponds to a known diagonal jump difference, and they are not on the edge in a way that makes `(start+end)/2` invalid). However, this method is called *after* a move is identified as a capture, implying `start` and `end` are already validated as such by `PossibleMoveProviderImpl`.

    ```java
    // In StateUtils.java
    private static Integer determineCapturedPieceIdx(Integer start, Integer end) {
        // Basic assumption: start and end are valid jump coordinates.
        // The captured piece is the arithmetic mean of the start and end squares.
        // This works because a jump skips exactly one square.
        int capturedPiece = (start + end) / 2;

        // Add validation if necessary:
        // 1. Ensure 'start' and 'end' are on a diagonal:
        //    - This is implicitly handled by how moves are generated.
        // 2. Ensure 'capturedPiece' is a valid square and is indeed between start and end on a diagonal.
        //    - This might involve checking that 'start' and 'capturedPiece' are one diagonal step apart,
        //      and 'capturedPiece' and 'end' are also one diagonal step apart in the same direction.
        //    - Checkerboard.getDiagonals() could be used for this validation if a more robust check is needed,
        //      but primarily for validation rather than discovery.

        // Example validation (conceptual):
        // if (!isValidDiagonalStep(start, capturedPiece) || !isValidDiagonalStep(capturedPiece, end)) {
        //     throw new IllegalStateException("Invalid capture path for determining captured piece.");
        // }
        return capturedPiece;
    }
    // The 'Side side' parameter might not be needed if this calculation is universal.
    // The call site in generateAfterMoveOrCaptureState would be:
    // light.remove(determineCapturedPieceIdx(start, dest)); or dark.remove(determineCapturedPieceIdx(start, dest));
    ```
    This simplifies the logic significantly by removing the iteration over all diagonals.

## 5. Code Duplication/Readability in `MoveServiceImpl.saveMove`

**Problem:**

*   **King Promotion Logic Location:** The king promotion logic is currently handled in `MoveServiceImpl.saveMove()` *after* `StateUtils.generateAfterMoveOrCaptureState()` has produced a `newState`. `newState.getKings()` is then mutated if promotion occurs. While `State` is a record (immutable by convention for its direct fields), the `Set<Integer> kings` it holds is mutable. This modification of a sub-component of the `State` object *after* its creation can be a bit subtle.
*   **State Validation (`validateMoveRequest`):**
    *   The `validateMoveRequest` method compares the server's current state with the state provided by the client. It does this by converting piece sets to sorted lists. This is a reasonable check against client-server desynchronization.
    *   However, its placement and the manual sorting could be refined. The comment `// TODO refactor` also indicates it's a known area for improvement.

**Proposal:**

*   **Integrate King Promotion into `StateUtils.generateAfterMoveOrCaptureState`:**
    *   The responsibility for determining the *complete* next state, including promotions, should ideally reside within `StateUtils.generateAfterMoveOrCaptureState`.
    *   Pass the `Side movingSide` and the `dest` square into this method.
    *   After updating piece positions and removing captured pieces, add a step within `generateAfterMoveOrCaptureState` to check for promotion using a similar logic to `isMoveResultsInKings` and update the `kings` set accordingly before returning the new `State`.
    *   This makes `StateUtils.generateAfterMoveOrCaptureState` the single source of truth for state transitions due to a move.
    *   `MoveServiceImpl.saveMove()` would then simply take the `newState` as is.

    ```java
    // In StateUtils.generateAfterMoveOrCaptureState(State state, MoveRequest moveRequest)
    // ... (after moving piece and handling capture)
    // Side movingSide = Side.valueOf(moveRequest.getSide()); // already have side
    if (shouldPromoteToKing(movingSide, dest, kings)) { // kings is the mutable copy
        kings.add(dest);
    }
    return new State(dark, light, kings); // dark, light, kings are the new sets

    // Add a helper in StateUtils or Checkerboard
    // public static boolean shouldPromoteToKing(Side side, int destinationSquare, Set<Integer> currentKings) {
    //    if (currentKings.contains(destinationSquare)) return false; // Already a king
    //    if (side == Side.DARK) {
    //        return List.of(29, 30, 31, 32).contains(destinationSquare);
    //    } else { // LIGHT
    //        return List.of(1, 2, 3, 4).contains(destinationSquare);
    //    }
    // }
    ```
*   **Refine `validateMoveRequest`:**
    *   The current comparison using sorted lists of piece positions is generally fine for ensuring sets are equal. The `State` record already implements `equals()` (which compares the sets `dark`, `light`, and `kings`).
    *   The validation could be simplified to:
        ```java
        // In MoveServiceImpl.java
        private void validateMoveRequest(State serverState, State clientState) {
            if (!serverState.equals(clientState)) {
                // Log the differences for debugging if desired
                // log.warn("Client state mismatch. Server: {}, Client: {}", serverState, clientState);
                throw new IllegalArgumentException("Client state does not match server state. Please refresh.");
            }
        }
        ```
    *   This relies on `State.equals()` being robust. The current `State.equals()` method correctly compares the sets, so this should be sufficient.

These proposals aim to make the codebase more robust, easier to understand, and more performant by leveraging better data structures and clearer separation of concerns.
