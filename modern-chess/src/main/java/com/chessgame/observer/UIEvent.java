package com.chessgame.observer;

import com.chessgame.model.Position;
import java.util.Objects;

/**
 * Event representing a UI-related event
 */
public class UIEvent extends GameEvent {
    private final Position position;
    private final boolean isSelected;
    private final Object data;
    
    public UIEvent(String eventId, Position position, boolean isSelected) {
        super(isSelected ? GameEventType.PIECE_SELECTED : GameEventType.PIECE_DESELECTED, eventId);
        this.position = Objects.requireNonNull(position, "Position cannot be null");
        this.isSelected = isSelected;
        this.data = null;
    }
    
    public UIEvent(String eventId, Position position, Object data) {
        super(GameEventType.VALID_MOVE_HIGHLIGHTED, eventId);
        this.position = Objects.requireNonNull(position, "Position cannot be null");
        this.isSelected = false;
        this.data = data;
    }
    
    public UIEvent(String eventId, String theme) {
        super(GameEventType.THEME_CHANGED, eventId);
        this.position = null;
        this.isSelected = false;
        this.data = theme;
    }
    
    public Position getPosition() {
        return position;
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public Object getData() {
        return data;
    }
    
    public String getTheme() {
        return data instanceof String ? (String) data : null;
    }
    
    @Override
    public String toString() {
        if (position != null) {
            if (isSelected) {
                return "Piece selected at " + position;
            } else {
                return "Piece deselected at " + position;
            }
        } else if (data instanceof String) {
            return "Theme changed to: " + data;
        } else {
            return "UI event: " + getType().getDescription();
        }
    }
}