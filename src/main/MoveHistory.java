package main;

import piece.Piece;
import main.Type;
import java.util.ArrayList;

/**
 * MoveHistory - класс для хранения и управления протоколом ходов в шахматной партии
 * Содержит все сделанные ходы в стандартной шахматной нотации
 */
public class MoveHistory {
    private ArrayList<String> moves;
    private int moveCount;  // Номер хода (для нумерации)

    public MoveHistory() {
        this.moves = new ArrayList<>();
        this.moveCount = 1;
    }

    /**
     * Добавляет ход в протокол
     * @param fromCol столбец откуда
     * @param fromRow строка откуда
     * @param toCol столбец куда
     * @param toRow строка куда
     * @param piece фигура, которая движется
     * @param isCapture был ли захват
     * @param isCheckmate шах/мат
     */
    public void addMove(int fromCol, int fromRow, int toCol, int toRow, 
                       Piece piece, boolean isCapture, String specialMove) {
        String move = convertToNotation(fromCol, fromRow, toCol, toRow, piece, isCapture, specialMove);
        moves.add(move);
    }

    /**
     * Преобразует координаты в шахматную нотацию
     */
    private String convertToNotation(int fromCol, int fromRow, int toCol, int toRow,
                                     Piece piece, boolean isCapture, String specialMove) {
        String[] files = {"a", "b", "c", "d", "e", "f", "g", "h"};
        String[] ranks = {"8", "7", "6", "5", "4", "3", "2", "1"};

        String notation = "";

        // Специальные ходы
        if (specialMove != null) {
            if (specialMove.equals("castling-king")) {
                notation = "O-O";
            } else if (specialMove.equals("castling-queen")) {
                notation = "O-O-O";
            } else if (specialMove.equals("en-passant")) {
                notation = files[fromCol] + "x" + files[toCol] + ranks[toRow];
            } else if (specialMove.contains("promotion")) {
                String promotionPiece = specialMove.replace("promotion-", "");
                notation = files[fromCol] + ranks[fromRow] + "-" + files[toCol] + ranks[toRow] + "=" + promotionPiece;
            }
        } else {
            // Обычные ходы
            String pieceName = "";
            if (piece.type != piece.type.PAWN) {
                pieceName = getPieceLetter(piece.type);
            }

            String fromSquare = files[fromCol] + ranks[fromRow];
            String toSquare = files[toCol] + ranks[toRow];
            
            if (isCapture) {
                if (piece.type == piece.type.PAWN) {
                    notation = files[fromCol] + "x" + toSquare;
                } else {
                    notation = pieceName + "x" + toSquare;
                }
            } else {
                if (piece.type == piece.type.PAWN) {
                    notation = toSquare;
                } else {
                    notation = pieceName + toSquare;
                }
            }
        }

        return notation;
    }

    /**
     * Получает букву фигуры для нотации
     */
    private String getPieceLetter(Type type) {
        switch (type) {
            case KING: return "K";
            case QUEEN: return "Q";
            case ROOK: return "R";
            case BISHOP: return "B";
            case KNIGHT: return "N";
            case PAWN: return "";
            default: return "";
        }
    }

    /**
     * Возвращает последний ход
     */
    public String getLastMove() {
        if (moves.isEmpty()) return "";
        return moves.get(moves.size() - 1);
    }

    /**
     * Возвращает все ходы
     */
    public ArrayList<String> getMoves() {
        return new ArrayList<>(moves);
    }

    /**
     * Возвращает количество ходов
     */
    public int getMoveCount() {
        return moves.size();
    }

    /**
     * Возвращает форматированный протокол для отображения
     */
    public String getFormattedNotation() {
        StringBuilder sb = new StringBuilder();
        int moveNumber = 1;
        
        for (int i = 0; i < moves.size(); i++) {
            if (i % 2 == 0) {
                if (i > 0) sb.append("\n");
                sb.append(moveNumber).append(". ");
                moveNumber++;
            } else {
                sb.append(" ");
            }
            sb.append(moves.get(i));
        }
        
        return sb.toString();
    }

    /**
     * Очищает историю
     */
    public void clear() {
        moves.clear();
        moveCount = 1;
    }
}
