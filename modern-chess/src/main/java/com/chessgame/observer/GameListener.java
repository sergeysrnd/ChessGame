package com.chessgame.observer;

/**
 * Interface for objects that want to listen to game events
 */
public interface GameListener {
    /**
     * Called when a game event occurs
     */
    void onGameEvent(GameEvent event);
    
    /**
     * Called when a move is made
     */
    void onMoveMade(MoveEvent event);
    
    /**
     * Called when the game state changes
     */
    void onGameStateChanged(GameStateChangedEvent event);
    
    /**
     * Called when a timer event occurs
     */
    void onTimerEvent(TimerEvent event);
    
    /**
     * Called when a UI event occurs
     */
    void onUIEvent(UIEvent event);
    
    /**
     * Called when check is detected
     */
    void onCheckDetected(CheckEvent event);
    
    /**
     * Called when checkmate or stalemate is detected
     */
    void onGameEnd(GameEndEvent event);
    
    /**
     * Default implementation for backward compatibility
     */
    default void handleEvent(GameEvent event) {
        // Default implementation does nothing
    }
}