package com.chessgame.model;

import java.util.Objects;

/**
 * Represents a chess move
 */
public final class Move {
    private final Position from;
    private final Position to;
    private final ChessPiece piece;
    private final ChessPiece capturedPiece;
    private final boolean isCastling;
    private final boolean isEnPassant;
    private final PieceType promotion;
    private final PlayerColor player;
    
    public Move(Position from, Position to, ChessPiece piece) {
        this(from, to, piece, null);
    }
    
    public Move(Position from, Position to, ChessPiece piece, ChessPiece capturedPiece) {
        this(from, to, piece, capturedPiece, false, false, null, piece.getColor());
    }
    
    public Move(Position from, Position to, ChessPiece piece, ChessPiece capturedPiece,
                boolean isCastling, boolean isEnPassant, PieceType promotion, PlayerColor player) {
        this.from = Objects.requireNonNull(from, "From position cannot be null");
        this.to = Objects.requireNonNull(to, "To position cannot be null");
        this.piece = Objects.requireNonNull(piece, "Piece cannot be null");
        this.capturedPiece = capturedPiece;
        this.isCastling = isCastling;
        this.isEnPassant = isEnPassant;
        this.promotion = promotion;
        this.player = Objects.requireNonNull(player, "Player cannot be null");
        
        if (from.equals(to)) {
            throw new IllegalArgumentException("From and to positions cannot be the same");
        }
    }
    
    /**
     * Creates a castling move
     */
    public static Move createCastlingMove(Position kingFrom, Position kingTo, ChessPiece king,
                                        Position rookFrom, Position rookTo, ChessPiece rook) {
        Move kingMove = new Move(kingFrom, kingTo, king, null, true, false, null, king.getColor());
        // Store rook information in the move for later processing
        return kingMove;
    }
    
    /**
     * Creates an en passant move
     */
    public static Move createEnPassantMove(Position from, Position to, ChessPiece pawn,
                                         Position capturedPawnPosition, ChessPiece capturedPawn) {
        return new Move(from, to, pawn, capturedPawn, false, true, null, pawn.getColor());
    }
    
    /**
     * Creates a promotion move
     */
    public static Move createPromotionMove(Position from, Position to, ChessPiece pawn,
                                         PieceType promotionType) {
        return new Move(from, to, pawn, null, false, false, promotionType, pawn.getColor());
    }
    
    public Position getFromPosition() {
        return from;
    }
    
    public Position getToPosition() {
        return to;
    }
    
    public ChessPiece getPiece() {
        return piece;
    }
    
    public ChessPiece getCapturedPiece() {
        return capturedPiece;
    }
    
    public boolean isCastling() {
        return isCastling;
    }
    
    public boolean isEnPassant() {
        return isEnPassant;
    }
    
    public boolean isPromotion() {
        return promotion != null;
    }
    
    public PieceType getPromotion() {
        return promotion;
    }
    
    public PlayerColor getPlayer() {
        return player;
    }
    
    public boolean isCapture() {
        return capturedPiece != null;
    }
    
    public String toAlgebraicNotation() {
        StringBuilder notation = new StringBuilder();
        
        // Castling notation
        if (isCastling) {
            if (to.getFile() == 6) {
                return "O-O"; // Kingside castling
            } else if (to.getFile() == 2) {
                return "O-O-O"; // Queenside castling
            }
        }
        
        // Piece symbol (except for pawns)
        if (!piece.getType().isPawn()) {
            notation.append(piece.getType().getNotation());
        }
        
        // Capture notation
        if (isCapture()) {
            if (piece.getType().isPawn()) {
                notation.append(from.toAlgebraic().charAt(0));
            }
            notation.append("x");
        }
        
        // Destination square
        notation.append(to.toAlgebraic());
        
        // Promotion notation
        if (isPromotion()) {
            notation.append("=").append(promotion.getNotation());
        }
        
        return notation.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Move move = (Move) obj;
        return Objects.equals(from, move.from) &&
               Objects.equals(to, move.to) &&
               Objects.equals(piece, move.piece) &&
               isCastling == move.isCastling &&
               isEnPassant == move.isEnPassant &&
               promotion == move.promotion;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(from, to, piece, isCastling, isEnPassant, promotion);
    }
    
    @Override
    public String toString() {
        return piece.getColor().getName() + " " + piece.getType().name().toLowerCase() + 
               ": " + from.toAlgebraic() + " -> " + to.toAlgebraic() +
               (isCapture() ? " (captures " + capturedPiece + ")" : "") +
               (isCastling ? " (castling)" : "") +
               (isEnPassant ? " (en passant)" : "") +
               (isPromotion() ? " (promotes to " + promotion + ")" : "");
    }
}