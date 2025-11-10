package com.chessgame.observer;

import com.chessgame.model.Move;
import com.chessgame.model.PlayerColor;
import com.chessgame.model.GameState;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a game event that can be observed
 */
public abstract class GameEvent {
    private final LocalDateTime timestamp;
    private final String eventId;
    private final GameEventType type;
    
    public GameEvent(GameEventType type, String eventId) {
        this.type = Objects.requireNonNull(type, "Event type cannot be null");
        this.eventId = Objects.requireNonNull(eventId, "Event ID cannot be null");
        this.timestamp = LocalDateTime.now();
    }
    
    public GameEventType getType() {
        return type;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    @Override
    public abstract String toString();
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GameEvent that = (GameEvent) obj;
        return Objects.equals(eventId, that.eventId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
}