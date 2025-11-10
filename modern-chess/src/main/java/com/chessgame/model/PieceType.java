package com.chessgame.model;

/**
 * Enumeration of chess piece types
 */
public enum PieceType {
    PAWN("P", 100),
    KNIGHT("N", 320),
    BISHOP("B", 330),
    ROOK("R", 500),
    QUEEN("Q", 900),
    KING("K", 20000);
    
    private final String notation;
    private final int value;
    
    PieceType(String notation, int value) {
        this.notation = notation;
        this.value = value;
    }
    
    public String getNotation() {
        return notation;
    }
    
    public int getValue() {
        return value;
    }
    
    public boolean canMoveLongDistance() {
        return this == BISHOP || this == ROOK || this == QUEEN;
    }
    
    public boolean isKing() {
        return this == KING;
    }
    
    public boolean isPawn() {
        return this == PAWN;
    }
    
    public boolean isKnight() {
        return this == KNIGHT;
    }
}