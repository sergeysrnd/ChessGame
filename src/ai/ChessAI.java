package ai;

import main.GamePanel;
import main.Type;
import piece.Piece;
import java.util.*;

/**
 * ChessAI - Искусственный интеллект для игры в шахматы
 * Использует алгоритм Minimax с альфа-бета отсечением
 * Поддерживает разные уровни сложности
 */
public class ChessAI {
    private int difficulty;  // 1-5: от легкого к сложному
    private int maxDepth;    // Глубина поиска в зависимости от сложности
    
    // Веса фигур для оценки позиции
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final int KING_VALUE = 20000;
    
    public ChessAI(int difficulty) {
        this.difficulty = Math.max(1, Math.min(5, difficulty));
        this.maxDepth = difficulty;  // Глубина 1-5 в зависимости от сложности
    }
    
    /**
     * Находит лучший ход для компьютера
     * @return массив [fromCol, fromRow, toCol, toRow] или null если нет ходов
     */
    public int[] findBestMove(ArrayList<Piece> pieces, int aiColor) {
        List<Move> allMoves = generateAllLegalMoves(pieces, aiColor);
        
        System.out.println("AI: Found " + allMoves.size() + " legal moves");
        
        if (allMoves.isEmpty()) {
            return null;
        }
        
        // На низких уровнях сложности используем случайность
        if (difficulty <= 2) {
            Collections.shuffle(allMoves);
            Move bestMove = allMoves.get(0);
            System.out.println("AI (easy): Random move");
            return new int[]{bestMove.fromCol, bestMove.fromRow, bestMove.toCol, bestMove.toRow};
        }
        
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int maxDepth = difficulty;  // Глубина зависит от сложности
        
        // Для каждого возможного хода
        for (Move move : allMoves) {
            // Создаем копию доски и применяем ход
            ArrayList<Piece> testPieces = copyPieces(pieces);
            applyTestMove(testPieces, move);
            
            // Оцениваем позицию с помощью минимакса
            int score = minimax(testPieces, maxDepth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, 
                              false, aiColor);
            
            // Запоминаем лучший ход
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        if (bestMove != null) {
            System.out.println("AI (smart): Selected move with score " + bestScore);
            return new int[]{bestMove.fromCol, bestMove.fromRow, bestMove.toCol, bestMove.toRow};
        }
        
        // Если что-то пошло не так, делаем случайный ход
        Collections.shuffle(allMoves);
        bestMove = allMoves.get(0);
        System.out.println("AI (fallback): Random move");
        return new int[]{bestMove.fromCol, bestMove.fromRow, bestMove.toCol, bestMove.toRow};
    }
    
    /**
     * Применяет тестовый ход к доске
     */
    private void applyTestMove(ArrayList<Piece> pieces, Move move) {
        for (Piece piece : pieces) {
            if (piece.col == move.fromCol && piece.row == move.fromRow) {
                // Запоминаем, была ли фигура под боем
                Piece captured = null;
                for (Piece target : pieces) {
                    if (target.col == move.toCol && target.row == move.toRow) {
                        captured = target;
                        break;
                    }
                }
                
                // Делаем ход
                piece.col = move.toCol;
                piece.row = move.toRow;
                
                // Удаляем побитую фигуру
                if (captured != null) {
                    pieces.remove(captured);
                }
                break;
            }
        }
    }
    
    /**
     * Оценивает позицию на доске
     * Положительное значение - выгодно для AI, отрицательное - для противника
     */
    private int evaluatePosition(ArrayList<Piece> pieces, int aiColor) {
        int score = 0;
        
        // Бонус за положение
        final int CENTER_BONUS = 30;
        final int NEAR_CENTER_BONUS = 10;
        
        for (Piece piece : pieces) {
            int value = getPieceValue(piece.type);
            
            // Не оцениваем короля материально
            if (piece.type == Type.KING) {
                continue;
            }
            
            // Бонус за положение (центр лучше чем края)
            if ((piece.col == 3 || piece.col == 4) && (piece.row == 3 || piece.row == 4)) {
                value += CENTER_BONUS;  // Центральные клетки
            } else if (piece.col >= 2 && piece.col <= 5 && piece.row >= 2 && piece.row <= 5) {
                value += NEAR_CENTER_BONUS;  // Около центра
            }
            
            // Прибавляем или вычитаем из общего счета
            if (piece.color == aiColor) {
                score += value;
            } else {
                score -= value;
            }
        }
        
        return score;
    }
    
    /**
     * Minimax алгоритм с альфа-бета отсечением
     */
    private int minimax(ArrayList<Piece> pieces, int depth, int alpha, int beta, 
                       boolean isMaximizing, int aiColor) {
        // Базовый случай: достигли максимальной глубины
        if (depth == 0) {
            return evaluatePosition(pieces, aiColor);
        }
        
        int currentColor = isMaximizing ? aiColor : (aiColor == GamePanel.WHITE ? GamePanel.BLACK : GamePanel.WHITE);
        List<Move> moves = generateAllLegalMoves(pieces, currentColor);
        
        if (moves.isEmpty()) {
            // Нет доступных ходов - проверяем мат или пат
            if (isKingInCheck(pieces, currentColor)) {
                return isMaximizing ? Integer.MIN_VALUE + 1000 : Integer.MAX_VALUE - 1000;
            }
            return 0;  // Пат
        }
        
        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moves) {
                ArrayList<Piece> simPieces = copyPieces(pieces);
                applyMove(simPieces, move);
                int eval = minimax(simPieces, depth - 1, alpha, beta, false, aiColor);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;  // Альфа-бета отсечение
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : moves) {
                ArrayList<Piece> simPieces = copyPieces(pieces);
                applyMove(simPieces, move);
                int eval = minimax(simPieces, depth - 1, alpha, beta, true, aiColor);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;  // Альфа-бета отсечение
            }
            return minEval;
        }
    }
    
    /**
     * Возвращает стоимость фигуры
     */
    private int getPieceValue(Type type) {
        switch (type) {
            case PAWN: return PAWN_VALUE;
            case KNIGHT: return KNIGHT_VALUE;
            case BISHOP: return BISHOP_VALUE;
            case ROOK: return ROOK_VALUE;
            case QUEEN: return QUEEN_VALUE;
            case KING: return KING_VALUE;
            default: return 0;
        }
    }
    
    /**
     * Генерирует все легальные ходы для цвета
     */
    private List<Move> generateAllLegalMoves(ArrayList<Piece> pieces, int color) {
        List<Move> legalMoves = new ArrayList<>();
        
        for (Piece piece : pieces) {
            if (piece.color != color) continue;
            
            int startCol = piece.col;
            int startRow = piece.row;
            
            for (int targetCol = 0; targetCol < 8; targetCol++) {
                for (int targetRow = 0; targetRow < 8; targetRow++) {
                    if (startCol == targetCol && startRow == targetRow) continue;
                    
                    // Создаем копию для тестирования
                    ArrayList<Piece> testPieces = copyPieces(pieces);
                    
                    // Находим соответствующую фигуру в копии
                    Piece testPiece = null;
                    for (Piece p : testPieces) {
                        if (p.col == startCol && p.row == startRow && p.color == color && 
                            p.type == piece.type) {
                            testPiece = p;
                            break;
                        }
                    }
                    
                    if (testPiece != null && testPiece.canMove(targetCol, targetRow)) {
                        // Делаем ход
                        testPiece.col = targetCol;
                        testPiece.row = targetRow;
                        
                        // Удаляем захваченную фигуру
                        if (testPiece.hittingP != null) {
                            testPieces.remove(testPiece.hittingP);
                        }
                        
                        // Проверяем, не под шахом ли король
                        if (!isKingInCheck(testPieces, color)) {
                            legalMoves.add(new Move(startCol, startRow, targetCol, targetRow));
                        }
                    }
                }
            }
        }
        
        return legalMoves;
    }
    
    /**
     * Проверяет, находится ли король в шахе
     */
    private boolean isKingInCheck(ArrayList<Piece> pieces, int kingColor) {
        Piece king = null;
        for (Piece piece : pieces) {
            if (piece.type == Type.KING && piece.color == kingColor) {
                king = piece;
                break;
            }
        }
        
        if (king == null) return false;
        
        int opponentColor = kingColor == GamePanel.WHITE ? GamePanel.BLACK : GamePanel.WHITE;
        for (Piece piece : pieces) {
            if (piece.color == opponentColor && piece.canMove(king.col, king.row)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Применяет ход к копии доски
     */
    private void applyMove(ArrayList<Piece> pieces, Move move) {
        Piece movingPiece = null;
        for (Piece piece : pieces) {
            if (piece.col == move.fromCol && piece.row == move.fromRow) {
                movingPiece = piece;
                break;
            }
        }
        
        if (movingPiece != null) {
            final Piece finalMovingPiece = movingPiece;
            // Удаляем захватываемую фигуру
            pieces.removeIf(p -> p.col == move.toCol && p.row == move.toRow && p != finalMovingPiece);
            
            // Перемещаем фигуру
            movingPiece.col = move.toCol;
            movingPiece.row = move.toRow;
        }
    }
    
    /**
     * Копирует список фигур
     */
    private ArrayList<Piece> copyPieces(ArrayList<Piece> original) {
        ArrayList<Piece> copy = new ArrayList<>();
        for (Piece piece : original) {
            try {
                Piece newPiece = piece.getClass().getDeclaredConstructor(int.class, int.class, int.class)
                    .newInstance(piece.col, piece.row, piece.color);
                newPiece.moved = piece.moved;
                newPiece.twoStepped = piece.twoStepped;
                copy.add(newPiece);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return copy;
    }
    
    /**
     * Вспомогательный класс для представления хода
     */
    private static class Move {
        int fromCol, fromRow, toCol, toRow;
        
        Move(int fromCol, int fromRow, int toCol, int toRow) {
            this.fromCol = fromCol;
            this.fromRow = fromRow;
            this.toCol = toCol;
            this.toRow = toRow;
        }
    }
    
    public void setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(5, difficulty));
        this.maxDepth = difficulty;
    }
}
