package com.chessgame.model;

/**
 * Enumeration of player colors
 */
public enum PlayerColor {
    WHITE("White", "♔♕♖♗♘♙"),
    BLACK("Black", "♚♛♜♝♞♟");
    
    private final String name;
    private final String unicodePieces;
    
    PlayerColor(String name, String unicodePieces) {
        this.name = name;
        this.unicodePieces = unicodePieces;
    }
    
    public String getName() {
        return name;
    }
    
    public String getUnicodePieces() {
        return unicodePieces;
    }
    
    /**
     * Returns the opposite color
     */
    public PlayerColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
    
    /**
     * Returns the direction of pawn movement for this color
     * WHITE: -1 (up the board), BLACK: +1 (down the board)
     */
    public int getPawnDirection() {
        return this == WHITE ? -1 : 1;
    }
    
    /**
     * Returns the starting rank for pawns of this color
     * WHITE: rank 6, BLACK: rank 1
     */
    public int getPawnStartRank() {
        return this == WHITE ? 6 : 1;
    }
    
    /**
     * Returns the promotion rank for pawns of this color
     * WHITE: rank 0, BLACK: rank 7
     */
    public int getPromotionRank() {
        return this == WHITE ? 0 : 7;
    }
}