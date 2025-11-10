package com.chessgame.model;

import com.chessgame.util.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Represents the chess board and manages all pieces and game state
 */
public final class ChessBoard {
    private final Map<Position, ChessPiece> pieces;
    private PlayerColor currentPlayer;
    private GameState gameState;
    private int halfMoveClock;
    private int fullMoveNumber;
    private MoveHistory moveHistory;
    
    public ChessBoard() {
        this.pieces = new ConcurrentHashMap<>();
        this.currentPlayer = PlayerColor.WHITE;
        this.gameState = GameState.ACTIVE;
        this.halfMoveClock = 0;
        this.fullMoveNumber = 1;
        this.moveHistory = new MoveHistory();
        initializeBoard();
    }
    
    private ChessBoard(Map<Position, ChessPiece> pieces, PlayerColor currentPlayer, 
                      GameState gameState, int halfMoveClock, int fullMoveNumber, 
                      MoveHistory moveHistory) {
        this.pieces = new ConcurrentHashMap<>(pieces);
        this.currentPlayer = currentPlayer;
        this.gameState = gameState;
        this.halfMoveClock = halfMoveClock;
        this.fullMoveNumber = fullMoveNumber;
        this.moveHistory = moveHistory;
    }
    
    /**
     * Initializes the board with standard starting position
     */
    private void initializeBoard() {
        // Place white pieces
        placePiece(new Position(0, 7), PieceType.ROOK, PlayerColor.WHITE);
        placePiece(new Position(1, 7), PieceType.KNIGHT, PlayerColor.WHITE);
        placePiece(new Position(2, 7), PieceType.BISHOP, PlayerColor.WHITE);
        placePiece(new Position(3, 7), PieceType.QUEEN, PlayerColor.WHITE);
        placePiece(new Position(4, 7), PieceType.KING, PlayerColor.WHITE);
        placePiece(new Position(5, 7), PieceType.BISHOP, PlayerColor.WHITE);
        placePiece(new Position(6, 7), PieceType.KNIGHT, PlayerColor.WHITE);
        placePiece(new Position(7, 7), PieceType.ROOK, PlayerColor.WHITE);
        
        // Place white pawns
        for (int file = 0; file < 8; file++) {
            placePiece(new Position(file, 6), PieceType.PAWN, PlayerColor.WHITE);
        }
        
        // Place black pieces
        placePiece(new Position(0, 0), PieceType.ROOK, PlayerColor.BLACK);
        placePiece(new Position(1, 0), PieceType.KNIGHT, PlayerColor.BLACK);
        placePiece(new Position(2, 0), PieceType.BISHOP, PlayerColor.BLACK);
        placePiece(new Position(3, 0), PieceType.QUEEN, PlayerColor.BLACK);
        placePiece(new Position(4, 0), PieceType.KING, PlayerColor.BLACK);
        placePiece(new Position(5, 0), PieceType.BISHOP, PlayerColor.BLACK);
        placePiece(new Position(6, 0), PieceType.KNIGHT, PlayerColor.BLACK);
        placePiece(new Position(7, 0), PieceType.ROOK, PlayerColor.BLACK);
        
        // Place black pawns
        for (int file = 0; file < 8; file++) {
            placePiece(new Position(file, 1), PieceType.PAWN, PlayerColor.BLACK);
        }
    }
    
    private void placePiece(Position position, PieceType type, PlayerColor color) {
        ChessPiece piece = ChessPieceFactory.createPiece(type, color, position);
        pieces.put(position, piece);
    }
    
    /**
     * Gets the piece at a specific position
     */
    public ChessPiece getPieceAt(Position position) {
        return pieces.get(position);
    }
    
    /**
     * Sets a piece at a specific position
     */
    public void setPieceAt(Position position, ChessPiece piece) {
        if (piece == null) {
            pieces.remove(position);
        } else {
            piece.setPosition(position);
            pieces.put(position, piece);
        }
    }
    
    /**
     * Checks if a position is empty
     */
    public boolean isEmpty(Position position) {
        return !pieces.containsKey(position);
    }
    
    /**
     * Gets all pieces of a specific color
     */
    public List<ChessPiece> getPiecesByColor(PlayerColor color) {
        return pieces.values().stream()
                .filter(piece -> piece.getColor() == color)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all pieces of a specific type
     */
    public List<ChessPiece> getPiecesByType(PieceType type) {
        return pieces.values().stream()
                .filter(piece -> piece.getType() == type)
                .collect(Collectors.toList());
    }
    
    /**
     * Finds the king of a specific color
     */
    public Optional<ChessPiece> findKing(PlayerColor color) {
        return pieces.values().stream()
                .filter(piece -> piece.getType() == PieceType.KING && piece.getColor() == color)
                .findFirst();
    }
    
    /**
     * Checks if a player is in check
     */
    public boolean isInCheck(PlayerColor color) {
        Optional<ChessPiece> king = findKing(color);
        if (king.isEmpty()) {
            return false; // King not found (shouldn't happen in normal play)
        }
        
        Position kingPosition = king.get().getPosition();
        
        // Check if any opponent piece can attack the king
        return pieces.values().stream()
                .filter(piece -> piece.getColor() != color)
                .anyMatch(attackingPiece -> canPieceAttack(attackingPiece, kingPosition));
    }
    
    /**
     * Checks if a piece can attack a specific position
     */
    private boolean canPieceAttack(ChessPiece piece, Position targetPosition) {
        // This is a simplified check - in a full implementation, 
        // this would use the piece's movement logic
        List<Move> possibleMoves = piece.getPossibleMoves(this);
        return possibleMoves.stream()
                .anyMatch(move -> move.getToPosition().equals(targetPosition));
    }
    
    /**
     * Makes a move if it's valid
     */
    public MoveResult makeMove(Move move) {
        if (gameState != GameState.ACTIVE) {
            return MoveResult.failure("Game is not active");
        }
        
        if (move.getPlayer() != currentPlayer) {
            return MoveResult.failure("Not " + currentPlayer.getName() + "'s turn");
        }
        
        ChessPiece piece = getPieceAt(move.getFromPosition());
        if (piece == null) {
            return MoveResult.failure("No piece at " + move.getFromPosition());
        }
        
        if (piece.getColor() != currentPlayer) {
            return MoveResult.failure("Cannot move opponent's piece");
        }
        
        // Check if the move is in the piece's valid moves
        List<Move> validMoves = piece.getValidMoves(this);
        if (!validMoves.contains(move)) {
            return MoveResult.failure("Invalid move: " + move);
        }
        
        // Execute the move
        executeMove(move);
        
        return MoveResult.success(move);
    }
    
    /**
     * Executes a move without validation (used for internal calculations)
     */
    public void makeMoveWithoutValidation(Move move) {
        executeMove(move);
    }
    
    private void executeMove(Move move) {
        ChessPiece piece = getPieceAt(move.getFromPosition());
        ChessPiece capturedPiece = getPieceAt(move.getToPosition());
        
        // Update half-move clock
        if (piece.getType().isPawn() || capturedPiece != null) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }
        
        // Make the move
        setPieceAt(move.getToPosition(), piece);
        setPieceAt(move.getFromPosition(), null);
        piece.markAsMoved();
        
        // Handle special moves
        if (move.isCastling()) {
            handleCastling(move);
        } else if (move.isEnPassant()) {
            handleEnPassant(move);
        } else if (move.isPromotion()) {
            handlePromotion(move);
        }
        
        // Add to move history
        moveHistory.addMove(move);
        
        // Switch players
        currentPlayer = currentPlayer.opposite();
        if (currentPlayer == PlayerColor.WHITE) {
            fullMoveNumber++;
        }
        
        // Check game state
        updateGameState();
    }
    
    private void handleCastling(Move move) {
        // Implementation for castling would go here
        // This would involve moving the rook as well
    }
    
    private void handleEnPassant(Move move) {
        // Implementation for en passant would go here
        // This would involve removing the captured pawn
    }
    
    private void handlePromotion(Move move) {
        // Replace pawn with promoted piece
        ChessPiece promotedPiece = ChessPieceFactory.createPiece(
            move.getPromotion(), move.getPlayer(), move.getToPosition());
        setPieceAt(move.getToPosition(), promotedPiece);
    }
    
    private void updateGameState() {
        // Check for checkmate
        if (isInCheck(currentPlayer)) {
            // Check if current player has any valid moves
            if (!hasValidMoves(currentPlayer)) {
                gameState = currentPlayer == PlayerColor.WHITE ? 
                           GameState.BLACK_CHECKMATE : GameState.WHITE_CHECKMATE;
                return;
            }
        }
        
        // Check for stalemate
        if (!hasValidMoves(currentPlayer)) {
            gameState = GameState.STALEMATE;
            return;
        }
        
        // Check for 50-move rule
        if (halfMoveClock >= Constants.DRAW_MOVE_COUNT) {
            gameState = GameState.DRAW_50_MOVE_RULE;
            return;
        }
        
        // Check for insufficient material
        if (hasInsufficientMaterial()) {
            gameState = GameState.DRAW_INSUFFICIENT_MATERIAL;
        }
    }
    
    private boolean hasValidMoves(PlayerColor color) {
        return getPiecesByColor(color).stream()
                .anyMatch(piece -> !piece.getValidMoves(this).isEmpty());
    }
    
    private boolean hasInsufficientMaterial() {
        // Simplified insufficient material check
        List<ChessPiece> pieces = getAllPieces();
        return pieces.size() <= 4; // Just king vs king or king + knight/bishop
    }
    
    public List<ChessPiece> getAllPieces() {
        return new ArrayList<>(pieces.values());
    }
    
    // Getters
    public PlayerColor getCurrentPlayer() {
        return currentPlayer;
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public int getHalfMoveClock() {
        return halfMoveClock;
    }
    
    public int getFullMoveNumber() {
        return fullMoveNumber;
    }
    
    public MoveHistory getMoveHistory() {
        return moveHistory;
    }
    
    /**
     * Creates a copy of the board
     */
    public ChessBoard copy() {
        Map<Position, ChessPiece> copiedPieces = new ConcurrentHashMap<>();
        pieces.forEach((pos, piece) -> {
            ChessPiece copy = ChessPieceFactory.createPiece(
                piece.getType(), piece.getColor(), piece.getPosition());
            copy.hasMoved = piece.hasMoved;
            copy.moveCount = piece.moveCount;
            copiedPieces.put(pos, copy);
        });
        
        return new ChessBoard(copiedPieces, currentPlayer, gameState, 
                            halfMoveClock, fullMoveNumber, moveHistory.copy());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  a b c d e f g h\n");
        
        for (int rank = 7; rank >= 0; rank--) {
            sb.append(rank + 1).append(" ");
            for (int file = 0; file < 8; file++) {
                Position pos = new Position(file, rank);
                ChessPiece piece = getPieceAt(pos);
                sb.append(piece != null ? piece.getType().getNotation() : ".");
                sb.append(" ");
            }
            sb.append(rank + 1).append("\n");
        }
        
        sb.append("  a b c d e f g h\n");
        return sb.toString();
    }
}