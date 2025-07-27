package com.example.checkers.core;

public record MoveCommand(GameId gameId, PlayerId playerId, String move) {
}
