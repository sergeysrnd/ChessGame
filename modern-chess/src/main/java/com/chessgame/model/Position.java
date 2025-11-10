package com.chessgame.model;

import com.chessgame.util.Constants;
import java.util.Objects;

/**
 * Represents a position on the chess board (0-7, 0-7)
 */
public final class Position {
    private final int file; // 0-7 (a-h)
    private final int rank; // 0-7 (1-8)
    
    public Position(int file, int rank) {
        if (!isValidPosition(file, rank)) {
            throw new IllegalArgumentException("Invalid position: (" + file + ", " + rank + ")");
        }
        this.file = file;
        this.rank = rank;
    }
    
    public static Position fromAlgebraic(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
        }
        
        char fileChar = algebraic.charAt(0);
        char rankChar = algebraic.charAt(1);
        
        int file = fileChar - 'a';
        int rank = rankChar - '1';
        
        return new Position(file, rank);
    }
    
    public static boolean isValidPosition(int file, int rank) {
        return file >= 0 && file < Constants.BOARD_SIZE && 
               rank >= 0 && rank < Constants.BOARD_SIZE;
    }
    
    public int getFile() {
        return file;
    }
    
    public int getRank() {
        return rank;
    }
    
    public String toAlgebraic() {
        return String.valueOf(Constants.FILE_NOTATION[file]) + 
               String.valueOf(Constants.RANK_NOTATION[rank]);
    }
    
    public Position offset(int fileDelta, int rankDelta) {
        return new Position(file + fileDelta, rank + rankDelta);
    }
    
    public boolean isInBounds() {
        return isValidPosition(file, rank);
    }
    
    public boolean isLightSquare() {
        return (file + rank) % 2 == 0;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return file == position.file && rank == position.rank;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(file, rank);
    }
    
    @Override
    public String toString() {
        return toAlgebraic();
    }
}