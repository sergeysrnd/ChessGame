package com.chessgame.observer;

import com.chessgame.model.GameState;
import com.chessgame.model.PlayerColor;
import java.util.Objects;

/**
 * Event representing a change in game state
 */
public class GameStateChangedEvent extends GameEvent {
    private final GameState oldState;
    private final GameState newState;
    private final PlayerColor currentPlayer;
    
    public GameStateChangedEvent(String eventId, GameState oldState, GameState newState, PlayerColor currentPlayer) {
        super(GameEventType.GAME_STATE_CHANGED, eventId);
        this.oldState = Objects.requireNonNull(oldState, "Old state cannot be null");
        this.newState = Objects.requireNonNull(newState, "New state cannot be null");
        this.currentPlayer = Objects.requireNonNull(currentPlayer, "Current player cannot be null");
    }
    
    public GameState getOldState() {
        return oldState;
    }
    
    public GameState getNewState() {
        return newState;
    }
    
    public PlayerColor getCurrentPlayer() {
        return currentPlayer;
    }
    
    @Override
    public String toString() {
        return "Game state changed from " + oldState + " to " + newState + " (Current player: " + currentPlayer + ")";
    }
}