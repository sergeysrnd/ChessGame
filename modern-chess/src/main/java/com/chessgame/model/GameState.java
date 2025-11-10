package com.chessgame.model;

/**
 * Represents the different states of a chess game
 */
public enum GameState {
    ACTIVE("Active Game"),
    WHITE_CHECKMATE("White Checkmated"),
    BLACK_CHECKMATE("Black Checkmated"),
    STALEMATE("Stalemate"),
    DRAW_50_MOVE_RULE("Draw - 50 Move Rule"),
    DRAW_INSUFFICIENT_MATERIAL("Draw - Insufficient Material"),
    DRAW_AGREEMENT("Draw by Agreement"),
    RESIGNATION("Resignation"),
    TIME_FORFEIT("Time Forfeit");
    
    private final String description;
    
    GameState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if the game is in a terminal state (finished)
     */
    public boolean isTerminal() {
        return this != ACTIVE;
    }
    
    /**
     * Checks if the game resulted in a draw
     */
    public boolean isDraw() {
        return this == STALEMATE || 
               this == DRAW_50_MOVE_RULE || 
               this == DRAW_INSUFFICIENT_MATERIAL || 
               this == DRAW_AGREEMENT;
    }
    
    /**
     * Checks if white won the game
     */
    public boolean isWhiteWin() {
        return this == BLACK_CHECKMATE || this == RESIGNATION && false; // Would need game context
    }
    
    /**
     * Checks if black won the game
     */
    public boolean isBlackWin() {
        return this == WHITE_CHECKMATE || this == RESIGNATION && false; // Would need game context
    }
}