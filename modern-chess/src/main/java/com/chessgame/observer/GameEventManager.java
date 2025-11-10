package com.chessgame.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Manages game events and notifies all registered listeners
 */
public final class GameEventManager {
    private static final Logger logger = LoggerFactory.getLogger(GameEventManager.class);
    
    private static GameEventManager instance;
    private final List<GameListener> listeners;
    private final String instanceId;
    
    private GameEventManager() {
        this.listeners = new CopyOnWriteArrayList<>();
        this.instanceId = UUID.randomUUID().toString();
        logger.info("GameEventManager initialized with ID: {}", instanceId);
    }
    
    /**
     * Gets the singleton instance of GameEventManager
     */
    public static synchronized GameEventManager getInstance() {
        if (instance == null) {
            instance = new GameEventManager();
        }
        return instance;
    }
    
    /**
     * Adds a listener to receive game events
     */
    public void addListener(GameListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            logger.debug("Added listener: {}", listener.getClass().getSimpleName());
        }
    }
    
    /**
     * Removes a listener from receiving game events
     */
    public void removeListener(GameListener listener) {
        if (listener == null) {
            return;
        }
        
        if (listeners.remove(listener)) {
            logger.debug("Removed listener: {}", listener.getClass().getSimpleName());
        }
    }
    
    /**
     * Removes all listeners
     */
    public void clearListeners() {
        listeners.clear();
        logger.debug("Cleared all listeners");
    }
    
    /**
     * Fires a generic game event
     */
    public void fireEvent(GameEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        logger.debug("Firing event: {} with ID: {}", event.getType(), event.getEventId());
        
        for (GameListener listener : listeners) {
            try {
                listener.onGameEvent(event);
                listener.handleEvent(event); // Backward compatibility
                
                // Fire specific event types
                if (event instanceof MoveEvent) {
                    listener.onMoveMade((MoveEvent) event);
                } else if (event instanceof GameStateChangedEvent) {
                    listener.onGameStateChanged((GameStateChangedEvent) event);
                } else if (event instanceof TimerEvent) {
                    listener.onTimerEvent((TimerEvent) event);
                } else if (event instanceof UIEvent) {
                    listener.onUIEvent((UIEvent) event);
                } else if (event instanceof CheckEvent) {
                    listener.onCheckDetected((CheckEvent) event);
                } else if (event instanceof GameEndEvent) {
                    listener.onGameEnd((GameEndEvent) event);
                }
                
            } catch (Exception e) {
                logger.error("Error notifying listener: {}", listener.getClass().getSimpleName(), e);
            }
        }
    }
    
    /**
     * Fires a move event
     */
    public void fireMoveEvent(Move move, boolean isValid) {
        String eventId = "move-" + System.currentTimeMillis();
        if (isValid) {
            fireEvent(new MoveEvent(eventId, move, true));
        } else {
            fireEvent(new MoveEvent(eventId, move, "Invalid move"));
        }
    }
    
    /**
     * Fires a move event with error message
     */
    public void fireMoveEvent(Move move, String errorMessage) {
        String eventId = "move-error-" + System.currentTimeMillis();
        fireEvent(new MoveEvent(eventId, move, errorMessage));
    }
    
    /**
     * Fires a game state change event
     */
    public void fireGameStateChangedEvent(Object oldState, Object newState, Object currentPlayer) {
        String eventId = "state-change-" + System.currentTimeMillis();
        // This would need proper types, but for now using Object
        // In real implementation, this would use the actual GameState and PlayerColor types
    }
    
    /**
     * Fires a check event
     */
    public void fireCheckEvent(Object playerInCheck, Object kingPosition, boolean isCheckmate) {
        String eventId = isCheckmate ? "checkmate-" : "check-" + System.currentTimeMillis();
        // This would need proper types
    }
    
    /**
     * Gets the number of registered listeners
     */
    public int getListenerCount() {
        return listeners.size();
    }
    
    /**
     * Gets the instance ID
     */
    public String getInstanceId() {
        return instanceId;
    }
    
    @Override
    public String toString() {
        return "GameEventManager{instanceId='" + instanceId + "', listenerCount=" + listeners.size() + "}";
    }
}