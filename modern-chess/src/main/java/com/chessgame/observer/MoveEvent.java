package com.chessgame.observer;

import com.chessgame.model.Move;
import java.util.Objects;

/**
 * Event representing a move in the game
 */
public class MoveEvent extends GameEvent {
    private final Move move;
    private final boolean isValid;
    private final String errorMessage;
    
    public MoveEvent(String eventId, Move move, boolean isValid) {
        super(GameEventType.MOVE_MADE, eventId);
        this.move = Objects.requireNonNull(move, "Move cannot be null");
        this.isValid = isValid;
        this.errorMessage = null;
    }
    
    public MoveEvent(String eventId, Move move, String errorMessage) {
        super(GameEventType.ILLEGAL_MOVE_ATTEMPT, eventId);
        this.move = Objects.requireNonNull(move, "Move cannot be null");
        this.isValid = false;
        this.errorMessage = Objects.requireNonNull(errorMessage, "Error message cannot be null");
    }
    
    public Move getMove() {
        return move;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public String toString() {
        if (isValid) {
            return "Valid move: " + move.toAlgebraicNotation();
        } else {
            return "Invalid move: " + move.toAlgebraicNotation() + " - " + errorMessage;
        }
    }
}