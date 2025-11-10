package com.chessgame.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for all chess pieces
 */
public abstract class ChessPiece {
    protected final PieceType type;
    protected final PlayerColor color;
    protected Position position;
    protected boolean hasMoved;
    protected int moveCount;
    
    public ChessPiece(PieceType type, PlayerColor color, Position position) {
        this.type = Objects.requireNonNull(type, "Piece type cannot be null");
        this.color = Objects.requireNonNull(color, "Player color cannot be null");
        this.position = Objects.requireNonNull(position, "Position cannot be null");
        this.hasMoved = false;
        this.moveCount = 0;
    }
    
    /**
     * Returns all possible moves for this piece from its current position
     * This method should be implemented by each specific piece type
     */
    public abstract List<Move> getPossibleMoves(ChessBoard board);
    
    /**
     * Returns all valid moves for this piece (filtered by game rules)
     */
    public List<Move> getValidMoves(ChessBoard board) {
        List<Move> validMoves = new ArrayList<>();
        List<Move> possibleMoves = getPossibleMoves(board);
        
        for (Move move : possibleMoves) {
            if (isValidMove(board, move)) {
                validMoves.add(move);
            }
        }
        
        return validMoves;
    }
    
    /**
     * Checks if a specific move is valid according to chess rules
     */
    protected boolean isValidMove(ChessBoard board, Move move) {
        // Check if move stays within board bounds
        if (!move.getToPosition().isInBounds()) {
            return false;
        }
        
        // Check if destination has a piece of the same color
        ChessPiece destinationPiece = board.getPieceAt(move.getToPosition());
        if (destinationPiece != null && destinationPiece.getColor() == this.color) {
            return false;
        }
        
        // Check if move would leave the king in check
        return !wouldLeaveKingInCheck(board, move);
    }
    
    /**
     * Checks if making this move would leave the current player's king in check
     */
    protected boolean wouldLeaveKingInCheck(ChessBoard board, Move move) {
        // Create a copy of the board and simulate the move
        ChessBoard testBoard = board.copy();
        testBoard.makeMoveWithoutValidation(move);
        
        // Check if the current player's king is in check
        PlayerColor kingColor = this.color;
        return testBoard.isInCheck(kingColor);
    }
    
    /**
     * Returns the Unicode character representing this piece
     */
    public abstract String getUnicodeSymbol();
    
    /**
     * Returns the file name for the piece's image
     */
    public abstract String getImageFileName();
    
    /**
     * Returns the movement pattern for this piece type
     */
    protected abstract List<Position[]> getMovementPatterns();
    
    /**
     * Gets piece type
     */
    public PieceType getType() {
        return type;
    }
    
    /**
     * Gets piece color
     */
    public PlayerColor getColor() {
        return color;
    }
    
    /**
     * Gets piece position
     */
    public Position getPosition() {
        return position;
    }
    
    /**
     * Sets piece position
     */
    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "Position cannot be null");
    }
    
    /**
     * Checks if piece has moved
     */
    public boolean hasMoved() {
        return hasMoved;
    }
    
    /**
     * Marks piece as moved
     */
    public void markAsMoved() {
        this.hasMoved = true;
        this.moveCount++;
    }
    
    /**
     * Gets move count
     */
    public int getMoveCount() {
        return moveCount;
    }
    
    /**
     * Checks if piece can move to position (basic check without validation)
     */
    public boolean canMoveTo(Position position) {
        return position != null && 
               (position.getFile() != this.position.getFile() || 
                position.getRank() != this.position.getRank());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChessPiece that = (ChessPiece) obj;
        return type == that.type && 
               color == that.color && 
               Objects.equals(position, that.position);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, color, position);
    }
    
    @Override
    public String toString() {
        return color.getName() + " " + type.name().toLowerCase() + " at " + position;
    }
}