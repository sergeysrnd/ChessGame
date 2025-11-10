package com.chessgame.factory;

import com.chessgame.model.*;
import com.chessgame.exception.ChessGameException;

/**
 * Factory for creating chess pieces using the Factory pattern
 */
public final class ChessPieceFactory {
    private ChessPieceFactory() {
        // Prevent instantiation
    }
    
    /**
     * Creates a chess piece of the specified type and color
     */
    public static ChessPiece createPiece(PieceType type, PlayerColor color, Position position) {
        switch (type) {
            case PAWN:
                return new Pawn(color, position);
            case KNIGHT:
                return new Knight(color, position);
            case BISHOP:
                return new Bishop(color, position);
            case ROOK:
                return new Rook(color, position);
            case QUEEN:
                return new Queen(color, position);
            case KING:
                return new King(color, position);
            default:
                throw new ChessGameException("Unknown piece type: " + type);
        }
    }
    
    /**
     * Creates a copy of an existing piece
     */
    public static ChessPiece copyPiece(ChessPiece original) {
        ChessPiece copy = createPiece(original.getType(), original.getColor(), original.getPosition());
        copy.hasMoved = original.hasMoved;
        copy.moveCount = original.moveCount;
        return copy;
    }
    
    // Concrete piece implementations
    
    private static class Pawn extends ChessPiece {
        public Pawn(PlayerColor color, Position position) {
            super(PieceType.PAWN, color, position);
        }
        
        @Override
        public java.util.List<Move> getPossibleMoves(ChessBoard board) {
            java.util.List<Move> moves = new java.util.ArrayList<>();
            Position currentPos = getPosition();
            int direction = getColor().getPawnDirection();
            int startRank = getColor().getPawnStartRank();
            
            // Forward move
            Position oneForward = currentPos.offset(0, direction);
            if (oneForward.isInBounds() && board.isEmpty(oneForward)) {
                moves.add(new Move(currentPos, oneForward, this));
                
                // Two squares forward from starting position
                if (currentPos.getRank() == startRank) {
                    Position twoForward = currentPos.offset(0, 2 * direction);
                    if (twoForward.isInBounds() && board.isEmpty(twoForward)) {
                        moves.add(new Move(currentPos, twoForward, this));
                    }
                }
            }
            
            // Diagonal captures
            for (int fileDelta : new int[]{-1, 1}) {
                Position capturePos = currentPos.offset(fileDelta, direction);
                if (capturePos.isInBounds()) {
                    ChessPiece targetPiece = board.getPieceAt(capturePos);
                    if (targetPiece != null && targetPiece.getColor() != this.getColor()) {
                        moves.add(new Move(currentPos, capturePos, this, targetPiece));
                    }
                }
            }
            
            return moves;
        }
        
        @Override
        public String getUnicodeSymbol() {
            return getColor() == PlayerColor.WHITE ? "♙" : "♟";
        }
        
        @Override
        public String getImageFileName() {
            return getColor().name().toLowerCase() + "_pawn.png";
        }
        
        @Override
        protected java.util.List<Position[]> getMovementPatterns() {
            return java.util.Collections.emptyList();
        }
    }
    
    private static class Knight extends ChessPiece {
        public Knight(PlayerColor color, Position position) {
            super(PieceType.KNIGHT, color, position);
        }
        
        @Override
        public java.util.List<Move> getPossibleMoves(ChessBoard board) {
            java.util.List<Move> moves = new java.util.ArrayList<>();
            Position currentPos = getPosition();
            
            int[][] knightMoves = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
            };
            
            for (int[] move : knightMoves) {
                Position targetPos = currentPos.offset(move[0], move[1]);
                if (targetPos.isInBounds()) {
                    ChessPiece targetPiece = board.getPieceAt(targetPos);
                    if (targetPiece == null || targetPiece.getColor() != this.getColor()) {
                        moves.add(new Move(currentPos, targetPos, this, targetPiece));
                    }
                }
            }
            
            return moves;
        }
        
        @Override
        public String getUnicodeSymbol() {
            return getColor() == PlayerColor.WHITE ? "♘" : "♞";
        }
        
        @Override
        public String getImageFileName() {
            return getColor().name().toLowerCase() + "_knight.png";
        }
        
        @Override
        protected java.util.List<Position[]> getMovementPatterns() {
            return java.util.Collections.emptyList();
        }
    }
    
    private static class Bishop extends ChessPiece {
        public Bishop(PlayerColor color, Position position) {
            super(PieceType.BISHOP, color, position);
        }
        
        @Override
        public java.util.List<Move> getPossibleMoves(ChessBoard board) {
            return getLinearMoves(board, new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
        }
        
        private java.util.List<Move> getLinearMoves(ChessBoard board, int[][] directions) {
            java.util.List<Move> moves = new java.util.ArrayList<>();
            Position currentPos = getPosition();
            
            for (int[] direction : directions) {
                for (int i = 1; i < 8; i++) {
                    Position targetPos = currentPos.offset(direction[0] * i, direction[1] * i);
                    if (!targetPos.isInBounds()) break;
                    
                    ChessPiece targetPiece = board.getPieceAt(targetPos);
                    if (targetPiece == null) {
                        moves.add(new Move(currentPos, targetPos, this));
                    } else {
                        if (targetPiece.getColor() != this.getColor()) {
                            moves.add(new Move(currentPos, targetPos, this, targetPiece));
                        }
                        break; // Blocked by any piece
                    }
                }
            }
            
            return moves;
        }
        
        @Override
        public String getUnicodeSymbol() {
            return getColor() == PlayerColor.WHITE ? "♗" : "♝";
        }
        
        @Override
        public String getImageFileName() {
            return getColor().name().toLowerCase() + "_bishop.png";
        }
        
        @Override
        protected java.util.List<Position[]> getMovementPatterns() {
            return java.util.Collections.emptyList();
        }
    }
    
    private static class Rook extends ChessPiece {
        public Rook(PlayerColor color, Position position) {
            super(PieceType.ROOK, color, position);
        }
        
        @Override
        public java.util.List<Move> getPossibleMoves(ChessBoard board) {
            return getLinearMoves(board, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
        }
        
        private java.util.List<Move> getLinearMoves(ChessBoard board, int[][] directions) {
            java.util.List<Move> moves = new java.util.ArrayList<>();
            Position currentPos = getPosition();
            
            for (int[] direction : directions) {
                for (int i = 1; i < 8; i++) {
                    Position targetPos = currentPos.offset(direction[0] * i, direction[1] * i);
                    if (!targetPos.isInBounds()) break;
                    
                    ChessPiece targetPiece = board.getPieceAt(targetPos);
                    if (targetPiece == null) {
                        moves.add(new Move(currentPos, targetPos, this));
                    } else {
                        if (targetPiece.getColor() != this.getColor()) {
                            moves.add(new Move(currentPos, targetPos, this, targetPiece));
                        }
                        break; // Blocked by any piece
                    }
                }
            }
            
            return moves;
        }
        
        @Override
        public String getUnicodeSymbol() {
            return getColor() == PlayerColor.WHITE ? "♖" : "♜";
        }
        
        @Override
        public String getImageFileName() {
            return getColor().name().toLowerCase() + "_rook.png";
        }
        
        @Override
        protected java.util.List<Position[]> getMovementPatterns() {
            return java.util.Collections.emptyList();
        }
    }
    
    private static class Queen extends ChessPiece {
        public Queen(PlayerColor color, Position position) {
            super(PieceType.QUEEN, color, position);
        }
        
        @Override
        public java.util.List<Move> getPossibleMoves(ChessBoard board) {
            return getLinearMoves(board, new int[][]{
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},  // Rook moves
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // Bishop moves
            });
        }
        
        private java.util.List<Move> getLinearMoves(ChessBoard board, int[][] directions) {
            java.util.List<Move> moves = new java.util.ArrayList<>();
            Position currentPos = getPosition();
            
            for (int[] direction : directions) {
                for (int i = 1; i < 8; i++) {
                    Position targetPos = currentPos.offset(direction[0] * i, direction[1] * i);
                    if (!targetPos.isInBounds()) break;
                    
                    ChessPiece targetPiece = board.getPieceAt(targetPos);
                    if (targetPiece == null) {
                        moves.add(new Move(currentPos, targetPos, this));
                    } else {
                        if (targetPiece.getColor() != this.getColor()) {
                            moves.add(new Move(currentPos, targetPos, this, targetPiece));
                        }
                        break; // Blocked by any piece
                    }
                }
            }
            
            return moves;
        }
        
        @Override
        public String getUnicodeSymbol() {
            return getColor() == PlayerColor.WHITE ? "♕" : "♛";
        }
        
        @Override
        public String getImageFileName() {
            return getColor().name().toLowerCase() + "_queen.png";
        }
        
        @Override
        protected java.util.List<Position[]> getMovementPatterns() {
            return java.util.Collections.emptyList();
        }
    }
    
    private static class King extends ChessPiece {
        public King(PlayerColor color, Position position) {
            super(PieceType.KING, color, position);
        }
        
        @Override
        public java.util.List<Move> getPossibleMoves(ChessBoard board) {
            java.util.List<Move> moves = new java.util.ArrayList<>();
            Position currentPos = getPosition();
            
            int[][] kingMoves = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},           {0, 1},
                {1, -1},  {1, 0},  {1, 1}
            };
            
            for (int[] move : kingMoves) {
                Position targetPos = currentPos.offset(move[0], move[1]);
                if (targetPos.isInBounds()) {
                    ChessPiece targetPiece = board.getPieceAt(targetPos);
                    if (targetPiece == null || targetPiece.getColor() != this.getColor()) {
                        moves.add(new Move(currentPos, targetPos, this, targetPiece));
                    }
                }
            }
            
            // TODO: Add castling logic
            if (!hasMoved()) {
                // Check for castling opportunities
                // This would involve checking if the rook hasn't moved and squares are clear
            }
            
            return moves;
        }
        
        @Override
        public String getUnicodeSymbol() {
            return getColor() == PlayerColor.WHITE ? "♔" : "♚";
        }
        
        @Override
        public String getImageFileName() {
            return getColor().name().toLowerCase() + "_king.png";
        }
        
        @Override
        protected java.util.List<Position[]> getMovementPatterns() {
            return java.util.Collections.emptyList();
        }
    }
}