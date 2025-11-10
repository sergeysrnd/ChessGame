package com.chessgame.observer;

/**
 * Enumeration of different game event types
 */
public enum GameEventType {
    GAME_STARTED("Game Started"),
    MOVE_MADE("Move Made"),
    MOVE_UNDONE("Move Undone"),
    CHECK_DETECTED("Check Detected"),
    CHECKMATE_DETECTED("Checkmate Detected"),
    STALEMATE_DETECTED("Stalemate Detected"),
    GAME_ENDED("Game Ended"),
    PLAYER_SWITCHED("Player Switched"),
    PIECE_PROMOTED("Piece Promoted"),
    CASTLING_PERFORMED("Castling Performed"),
    EN_PASSANT_PERFORMED("En Passant Performed"),
    GAME_STATE_CHANGED("Game State Changed"),
    TIMER_STARTED("Timer Started"),
    TIMER_PAUSED("Timer Paused"),
    TIMER_RESUMED("Timer Resumed"),
    TIMER_EXPIRED("Timer Expired"),
    ILLEGAL_MOVE_ATTEMPT("Illegal Move Attempt"),
    VALID_MOVE_HIGHLIGHTED("Valid Move Highlighted"),
    PIECE_SELECTED("Piece Selected"),
    PIECE_DESELECTED("Piece Deselected"),
    THEME_CHANGED("Theme Changed"),
    SETTINGS_UPDATED("Settings Updated");
    
    private final String description;
    
    GameEventType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this is a game state event
     */
    public boolean isGameStateEvent() {
        return this == GAME_STARTED || this == GAME_ENDED || 
               this == CHECKMATE_DETECTED || this == STALEMATE_DETECTED;
    }
    
    /**
     * Checks if this is a move-related event
     */
    public boolean isMoveEvent() {
        return this == MOVE_MADE || this == MOVE_UNDONE || this == ILLEGAL_MOVE_ATTEMPT;
    }
    
    /**
     * Checks if this is a timer event
     */
    public boolean isTimerEvent() {
        return this == TIMER_STARTED || this == TIMER_PAUSED || 
               this == TIMER_RESUMED || this == TIMER_EXPIRED;
    }
    
    /**
     * Checks if this is a UI event
     */
    public boolean isUIEvent() {
        return this == PIECE_SELECTED || this == PIECE_DESELECTED || 
               this == VALID_MOVE_HIGHLIGHTED || this == THEME_CHANGED || 
               this == SETTINGS_UPDATED;
    }
}