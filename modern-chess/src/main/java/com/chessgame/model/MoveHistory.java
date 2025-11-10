package com.chessgame.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Manages the history of moves in a chess game
 */
public final class MoveHistory {
    private final List<MoveEntry> moves;
    private LocalDateTime gameStartTime;
    
    public MoveHistory() {
        this.moves = new ArrayList<>();
        this.gameStartTime = LocalDateTime.now();
    }
    
    private MoveHistory(List<MoveEntry> moves, LocalDateTime gameStartTime) {
        this.moves = new ArrayList<>(moves);
        this.gameStartTime = gameStartTime;
    }
    
    /**
     * Adds a move to the history
     */
    public void addMove(Move move) {
        if (move == null) {
            throw new IllegalArgumentException("Move cannot be null");
        }
        moves.add(new MoveEntry(moves.size() + 1, move));
    }
    
    /**
     * Gets the move at the specified index
     */
    public MoveEntry getMove(int index) {
        if (index < 1 || index > moves.size()) {
            throw new IndexOutOfBoundsException("Move index out of range: " + index);
        }
        return moves.get(index - 1); // Convert to 0-based index
    }
    
    /**
     * Gets the total number of moves
     */
    public int getMoveCount() {
        return moves.size();
    }
    
    /**
     * Gets the last move made
     */
    public MoveEntry getLastMove() {
        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(moves.size() - 1);
    }
    
    /**
     * Gets all moves
     */
    public List<MoveEntry> getAllMoves() {
        return Collections.unmodifiableList(moves);
    }
    
    /**
     * Returns moves in standard chess notation format
     */
    public String getStandardNotation() {
        StringBuilder notation = new StringBuilder();
        
        for (int i = 0; i < moves.size(); i++) {
            MoveEntry entry = moves.get(i);
            int moveNumber = (i / 2) + 1;
            
            if (i % 2 == 0) {
                notation.append(moveNumber).append(". ");
            }
            
            notation.append(entry.getMove().toAlgebraicNotation());
            
            if (i % 2 == 1 || i == moves.size() - 1) {
                notation.append("\n");
            } else {
                notation.append(" ");
            }
        }
        
        return notation.toString();
    }
    
    /**
     * Returns moves in PGN (Portable Game Notation) format
     */
    public String getPGNFormat() {
        return getStandardNotation().trim();
    }
    
    /**
     * Gets the game start time
     */
    public LocalDateTime getGameStartTime() {
        return gameStartTime;
    }
    
    /**
     * Creates a copy of this move history
     */
    public MoveHistory copy() {
        return new MoveHistory(moves, gameStartTime);
    }
    
    /**
     * Clears all moves from history
     */
    public void clear() {
        moves.clear();
        gameStartTime = LocalDateTime.now();
    }
    
    /**
     * Represents a single move entry with metadata
     */
    public static final class MoveEntry {
        private final int moveNumber;
        private final Move move;
        private final LocalDateTime timestamp;
        private long thinkingTime; // Time spent thinking in milliseconds
        
        public MoveEntry(int moveNumber, Move move) {
            this.moveNumber = moveNumber;
            this.move = Objects.requireNonNull(move, "Move cannot be null");
            this.timestamp = LocalDateTime.now();
            this.thinkingTime = 0;
        }
        
        private MoveEntry(int moveNumber, Move move, LocalDateTime timestamp, long thinkingTime) {
            this.moveNumber = moveNumber;
            this.move = move;
            this.timestamp = timestamp;
            this.thinkingTime = thinkingTime;
        }
        
        public int getMoveNumber() {
            return moveNumber;
        }
        
        public Move getMove() {
            return move;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public long getThinkingTime() {
            return thinkingTime;
        }
        
        public void setThinkingTime(long thinkingTime) {
            this.thinkingTime = thinkingTime;
        }
        
        @Override
        public String toString() {
            return moveNumber + ". " + move.toAlgebraicNotation() +
                   (thinkingTime > 0 ? " (" + thinkingTime + "ms)" : "");
        }
    }
    
    @Override
    public String toString() {
        return "MoveHistory{moveCount=" + moves.size() + ", startTime=" + gameStartTime + "}";
    }
}