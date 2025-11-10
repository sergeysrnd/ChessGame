package com.chessgame.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents the result of a move attempt
 */
public final class MoveResult {
    private final boolean success;
    private final Move move;
    private final String errorMessage;
    
    private MoveResult(boolean success, Move move, String errorMessage) {
        this.success = success;
        this.move = move;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Creates a successful move result
     */
    public static MoveResult success(Move move) {
        return new MoveResult(true, move, null);
    }
    
    /**
     * Creates a failed move result
     */
    public static MoveResult failure(String errorMessage) {
        return new MoveResult(false, null, Objects.requireNonNull(errorMessage, "Error message cannot be null"));
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public boolean isFailure() {
        return !success;
    }
    
    public Optional<Move> getMove() {
        return Optional.ofNullable(move);
    }
    
    public String getErrorMessage() {
        if (success) {
            throw new IllegalStateException("Cannot get error message from successful move");
        }
        return errorMessage;
    }
    
    @Override
    public String toString() {
        if (success) {
            return "Move successful: " + move;
        } else {
            return "Move failed: " + errorMessage;
        }
    }
}