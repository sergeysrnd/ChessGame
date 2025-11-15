package chessgame.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Основная логика шахматной игры.
 * Управляет состоянием доски, ходами и проверкой правил.
 */
public class ChessGame {

    private static final Logger logger = LoggerFactory.getLogger(ChessGame.class);

    // Текущее состояние доски (64 клетки)
    private String[] board = new String[64];

    // История ходов для отмены
    private Stack<MoveRecord> moveHistory = new Stack<>();

    // Флаг, чей ход (true - белые, false - черные)
    private boolean whiteToMove = true;

    // Состояние игры
    private String gameStatus = "playing"; // playing, check, checkmate, stalemate, draw

    // Права на рокировку
    private boolean whiteKingSideCastle = true;
    private boolean whiteQueenSideCastle = true;
    private boolean blackKingSideCastle = true;
    private boolean blackQueenSideCastle = true;

    // Поле для взятия на проходе
    private String enPassantTarget = "-";

    // Счетчик полуходов без взятия или хода пешки
    private int halfMoveClock = 0;

    // История позиций для проверки повторений
    private List<String> positionHistory = new ArrayList<>();

    // Константы для шахматных фигур
    private static final String EMPTY = "empty";

    /**
     * Конструктор шахматной игры.
     */
    public ChessGame() {
        initializeBoard();
    }

    /**
     * Инициализирует шахматную доску в начальную позицию.
     */
    private void initializeBoard() {
        clearBoard();
        setupInitialPosition();
    }

    /**
     * Очищает доску.
     */
    private void clearBoard() {
        Arrays.fill(board, EMPTY);
        moveHistory.clear();
        whiteToMove = true;
        gameStatus = "playing";
        whiteKingSideCastle = true;
        whiteQueenSideCastle = true;
        blackKingSideCastle = true;
        blackQueenSideCastle = true;
        enPassantTarget = "-";
        halfMoveClock = 0;
        positionHistory.clear();
    }

    /**
     * Устанавливает начальную позицию.
     */
    private void setupInitialPosition() {
        // Черные фигуры (8-я линия)
        board[0] = "br"; board[1] = "bn"; board[2] = "bb"; board[3] = "bq";
        board[4] = "bk"; board[5] = "bb"; board[6] = "bn"; board[7] = "br";

        // Пешки черных
        for (int i = 8; i < 16; i++) {
            board[i] = "bp";
        }

        // Белые фигуры (1-я линия)
        board[56] = "wr"; board[57] = "wn"; board[58] = "wb"; board[59] = "wq";
        board[60] = "wk"; board[61] = "wb"; board[62] = "wn"; board[63] = "wr";

        // Пешки белых
        for (int i = 48; i < 56; i++) {
            board[i] = "wp";
        }
    }

    /**
     * Получает фигуру на указанной клетке.
     */
    public String getPieceAt(String square) {
        int index = squareToIndex(square);
        return index >= 0 ? board[index] : EMPTY;
    }

    /**
     * Проверяет, является ли ход валидным.
     */
    public boolean isValidMove(String from, String to, boolean isWhiteMove) {
        if (!isValidSquare(from) || !isValidSquare(to)) {
            return false;
        }

        int fromIndex = squareToIndex(from);
        int toIndex = squareToIndex(to);

        String piece = board[fromIndex];

        // Проверяем, что на исходной клетке есть фигура
        if (piece.equals(EMPTY)) {
            return false;
        }

        // Проверяем, что фигура принадлежит текущему игроку
        boolean isWhitePiece = piece.charAt(0) == 'w';
        if (isWhitePiece != isWhiteMove) {
            return false;
        }

        // Проверяем, что ход не бьет свою фигуру
        String targetPiece = board[toIndex];
        if (!targetPiece.equals(EMPTY) &&
            (targetPiece.charAt(0) == piece.charAt(0))) {
            return false;
        }

        // Проверяем специфичные правила для каждой фигуры
        String pieceType = piece.substring(1);
        boolean basicValid = isValidPieceMove(fromIndex, toIndex, pieceType);

        if (!basicValid) {
            return false;
        }

        // Проверяем, что ход не оставляет короля под шахом
        return !wouldLeaveKingInCheck(from, to, isWhiteMove);
    }

    /**
     * Проверяет валидность хода для конкретной фигуры.
     */
    private boolean isValidPieceMove(int fromIndex, int toIndex, String pieceType) {
        int fromRow = fromIndex / 8;
        int fromCol = fromIndex % 8;
        int toRow = toIndex / 8;
        int toCol = toIndex % 8;

        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        switch (pieceType) {
            case "p": // Пешка
                return isValidPawnMove(fromIndex, toIndex);
            case "n": // Конь
                return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
            case "b": // Слон
                return rowDiff == colDiff && rowDiff != 0 && isPathClear(fromIndex, toIndex);
            case "r": // Ладья
                return ((rowDiff == 0 && colDiff > 0) || (colDiff == 0 && rowDiff > 0)) && isPathClear(fromIndex, toIndex);
            case "q": // Ферзь
                return ((rowDiff == colDiff && rowDiff != 0) ||
                        (rowDiff == 0 && colDiff > 0) || (colDiff == 0 && rowDiff > 0)) && isPathClear(fromIndex, toIndex);
            case "k": // Король
                if (rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff) > 0) {
                    return true;
                }
                // Рокировка
                if (rowDiff == 0 && (colDiff == 2 || colDiff == -2)) {
                    return isValidCastling(fromIndex, toIndex);
                }
                return false;
            default:
                return false;
        }
    }

    /**
     * Проверяет валидность хода пешки.
     */
    private boolean isValidPawnMove(int fromIndex, int toIndex) {
        int fromRow = fromIndex / 8;
        int fromCol = fromIndex % 8;
        int toRow = toIndex / 8;
        int toCol = toIndex % 8;

        int rowDiff = toRow - fromRow;
        int colDiff = Math.abs(toCol - fromCol);

        String piece = board[fromIndex];
        boolean isWhitePawn = piece.charAt(0) == 'w';

        String targetPiece = board[toIndex];

        // Белые пешки идут вверх (уменьшение номера строки)
        if (isWhitePawn) {
            // Обычный ход вперед
            if (rowDiff == -1 && colDiff == 0) {
                return targetPiece.equals(EMPTY);
            }
            // Двойной ход
            if (rowDiff == -2 && colDiff == 0 && fromRow == 6) {
                // Проверяем, что путь свободен
                int middleIndex = (fromRow - 1) * 8 + fromCol;
                return board[middleIndex].equals(EMPTY) && targetPiece.equals(EMPTY);
            }
            // Взятие
            if (rowDiff == -1 && colDiff == 1) {
                // Обычное взятие или взятие на проходе
                boolean normalCapture = !targetPiece.equals(EMPTY) && targetPiece.charAt(0) == 'b';
                boolean enPassant = indexToSquare(toIndex).equals(enPassantTarget);
                return normalCapture || enPassant;
            }
        } else { // Черные пешки идут вниз
            // Обычный ход вперед
            if (rowDiff == 1 && colDiff == 0) {
                return targetPiece.equals(EMPTY);
            }
            // Двойной ход
            if (rowDiff == 2 && colDiff == 0 && fromRow == 1) {
                // Проверяем, что путь свободен
                int middleIndex = (fromRow + 1) * 8 + fromCol;
                return board[middleIndex].equals(EMPTY) && targetPiece.equals(EMPTY);
            }
            // Взятие
            if (rowDiff == 1 && colDiff == 1) {
                // Обычное взятие или взятие на проходе
                boolean normalCapture = !targetPiece.equals(EMPTY) && targetPiece.charAt(0) == 'w';
                boolean enPassant = indexToSquare(toIndex).equals(enPassantTarget);
                return normalCapture || enPassant;
            }
        }

        return false;
    }

    /**
     * Проверяет, оставляет ли ход короля под шахом.
     */
    private boolean wouldLeaveKingInCheck(String from, String to, boolean isWhiteMove) {
        int fromIndex = squareToIndex(from);
        int toIndex = squareToIndex(to);

        String piece = board[fromIndex];
        String capturedPiece = board[toIndex];

        // Имитируем ход
        board[toIndex] = piece;
        board[fromIndex] = EMPTY;

        // Обрабатываем специальные ходы
        String pieceType = piece.substring(1);
        if (pieceType.equals("p") && indexToSquare(toIndex).equals(enPassantTarget)) {
            // Взятие на проходе
            int capturedPawnRow = toIndex / 8 + (isWhiteMove ? 1 : -1);
            int capturedPawnIndex = capturedPawnRow * 8 + (toIndex % 8);
            String tempCaptured = board[capturedPawnIndex];
            board[capturedPawnIndex] = EMPTY;
            boolean inCheck = isInCheck(isWhiteMove);
            board[capturedPawnIndex] = tempCaptured;
            // Восстанавливаем
            board[fromIndex] = piece;
            board[toIndex] = capturedPiece;
            return inCheck;
        } else if (pieceType.equals("k") && Math.abs((toIndex % 8) - (fromIndex % 8)) == 2) {
            // Рокировка - король уже перемещен, ладья тоже
            int rookFrom, rookTo;
            int fromCol = fromIndex % 8;
            int toCol = toIndex % 8;
            if (toCol == 6) {
                rookFrom = fromIndex + 3;
                rookTo = fromIndex + 1;
            } else {
                rookFrom = fromIndex - 4;
                rookTo = fromIndex - 1;
            }
            String rookPiece = board[rookFrom];
            board[rookTo] = rookPiece;
            board[rookFrom] = EMPTY;
            boolean inCheck = isInCheck(isWhiteMove);
            // Восстанавливаем
            board[rookFrom] = rookPiece;
            board[rookTo] = EMPTY;
            board[fromIndex] = piece;
            board[toIndex] = capturedPiece;
            return inCheck;
        }

        boolean inCheck = isInCheck(isWhiteMove);

        // Восстанавливаем
        board[fromIndex] = piece;
        board[toIndex] = capturedPiece;

        return inCheck;
    }

    /**
     * Проверяет, свободен ли путь между двумя клетками для скользящих фигур.
     */
    private boolean isPathClear(int fromIndex, int toIndex) {
        int fromRow = fromIndex / 8;
        int fromCol = fromIndex % 8;
        int toRow = toIndex / 8;
        int toCol = toIndex % 8;

        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);

        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;

        while (currentRow != toRow || currentCol != toCol) {
            int currentIndex = currentRow * 8 + currentCol;
            if (!board[currentIndex].equals(EMPTY)) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return true;
    }

    /**
     * Проверяет валидность рокировки.
     */
    private boolean isValidCastling(int fromIndex, int toIndex) {
        int fromCol = fromIndex % 8;
        int toCol = toIndex % 8;
        boolean isWhite = board[fromIndex].charAt(0) == 'w';

        // Король не должен быть под шахом
        if (isInCheck(isWhite)) {
            return false;
        }

        boolean isKingSide = toCol > fromCol;

        // Проверяем права на рокировку
        if (isWhite) {
            if (isKingSide && !whiteKingSideCastle) return false;
            if (!isKingSide && !whiteQueenSideCastle) return false;
        } else {
            if (isKingSide && !blackKingSideCastle) return false;
            if (!isKingSide && !blackQueenSideCastle) return false;
        }

        // Проверяем, что путь свободен
        int rookCol = isKingSide ? 7 : 0;
        int kingPathStart = Math.min(fromCol, rookCol) + 1;
        int kingPathEnd = Math.max(fromCol, rookCol) - 1;
        int row = fromIndex / 8;

        for (int col = kingPathStart; col <= kingPathEnd; col++) {
            if (!board[row * 8 + col].equals(EMPTY)) {
                return false;
            }
        }

        // Проверяем, что клетки, через которые проходит король, не атакованы
        int kingMove1 = row * 8 + (fromCol + (isKingSide ? 1 : -1));
        int kingMove2 = row * 8 + (fromCol + (isKingSide ? 2 : -2));

        // Имитируем ход короля на промежуточную клетку
        String originalKing = board[fromIndex];
        board[fromIndex] = EMPTY;
        board[kingMove1] = originalKing;

        boolean attacked1 = isSquareAttacked(kingMove1, !isWhite);

        board[kingMove1] = EMPTY;
        board[kingMove2] = originalKing;

        boolean attacked2 = isSquareAttacked(kingMove2, !isWhite);

        // Восстанавливаем
        board[kingMove2] = EMPTY;
        board[fromIndex] = originalKing;

        return !attacked1 && !attacked2;
    }

    /**
     * Проверяет, атакована ли клетка противником.
     */
    private boolean isSquareAttacked(int index, boolean byWhite) {
        String enemyColor = byWhite ? "w" : "b";

        for (int i = 0; i < 64; i++) {
            if (board[i].charAt(0) == enemyColor.charAt(0)) {
                if (canPieceAttackSquare(i, index)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Выполняет ход.
     */
    public void makeMove(String from, String to) {
        int fromIndex = squareToIndex(from);
        int toIndex = squareToIndex(to);

        String piece = board[fromIndex];
        String capturedPiece = board[toIndex];

        // Сохраняем ход в историю для возможной отмены
        MoveRecord moveRecord = new MoveRecord(from, to, piece, capturedPiece, whiteToMove,
                                               whiteKingSideCastle, whiteQueenSideCastle,
                                               blackKingSideCastle, blackQueenSideCastle,
                                               enPassantTarget);
        moveHistory.push(moveRecord);

        // Обрабатываем взятие на проходе
        if (piece.substring(1).equals("p") && indexToSquare(toIndex).equals(enPassantTarget)) {
            // Удаляем пешку, взятую на проходе
            int capturedPawnRow = toIndex / 8 + (whiteToMove ? 1 : -1);
            int capturedPawnIndex = capturedPawnRow * 8 + (toIndex % 8);
            capturedPiece = board[capturedPawnIndex];
            board[capturedPawnIndex] = EMPTY;
        }

        // Выполняем ход
        board[toIndex] = piece;
        board[fromIndex] = EMPTY;

        // Обрабатываем рокировку
        String pieceType = piece.substring(1);
        if (pieceType.equals("k")) {
            int fromCol = fromIndex % 8;
            int toCol = toIndex % 8;
            if (Math.abs(toCol - fromCol) == 2) {
                // Рокировка
                int rookFrom, rookTo;
                if (toCol == 6) { // Короткая рокировка
                    rookFrom = fromIndex + 3; // h
                    rookTo = fromIndex + 1; // f
                } else { // Длинная рокировка
                    rookFrom = fromIndex - 4; // a
                    rookTo = fromIndex - 1; // d
                }
                board[rookTo] = board[rookFrom];
                board[rookFrom] = EMPTY;
            }
        }

        // Обрабатываем превращение пешки
        if (piece.substring(1).equals("p")) {
            int toRow = toIndex / 8;
            if ((whiteToMove && toRow == 0) || (!whiteToMove && toRow == 7)) {
                // Превращаем в ферзя
                board[toIndex] = (whiteToMove ? "w" : "b") + "q";
            }
        }

        // Обновляем права на рокировку
        updateCastlingRights(piece, fromIndex);

        // Устанавливаем поле для взятия на проходе
        enPassantTarget = "-";
        if (piece.substring(1).equals("p")) {
            int fromRow = fromIndex / 8;
            int toRow = toIndex / 8;
            int rowDiff = Math.abs(toRow - fromRow);
            if (rowDiff == 2) {
                // Двойной ход пешки
                int enPassantRow = (fromRow + toRow) / 2;
                enPassantTarget = indexToSquare(enPassantRow * 8 + (fromIndex % 8));
            }
        }

        // Обновляем счетчик полуходов
        if (!capturedPiece.equals(EMPTY) || pieceType.equals("p")) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }

        // Добавляем позицию в историю
        String currentFen = getCurrentPosition();
        positionHistory.add(currentFen);

        // Переключаем ход
        whiteToMove = !whiteToMove;

        // Обновляем состояние игры
        updateGameStatus();

        logger.info("Ход выполнен: {} -> {} ({})", from, to, piece);
    }

    /**
     * Обновляет права на рокировку после хода.
     */
    private void updateCastlingRights(String piece, int fromIndex) {
        String pieceType = piece.substring(1);
        boolean isWhite = piece.charAt(0) == 'w';

        if (pieceType.equals("k")) {
            // Король ходил - теряем оба права
            if (isWhite) {
                whiteKingSideCastle = false;
                whiteQueenSideCastle = false;
            } else {
                blackKingSideCastle = false;
                blackQueenSideCastle = false;
            }
        } else if (pieceType.equals("r")) {
            // Ладья ходила
            if (isWhite) {
                if (fromIndex == 63) { // h1
                    whiteKingSideCastle = false;
                } else if (fromIndex == 56) { // a1
                    whiteQueenSideCastle = false;
                }
            } else {
                if (fromIndex == 7) { // h8
                    blackKingSideCastle = false;
                } else if (fromIndex == 0) { // a8
                    blackQueenSideCastle = false;
                }
            }
        }
    }

    /**
     * Отменяет последний ход.
     */
    public boolean undoMove() {
        if (moveHistory.isEmpty()) {
            return false;
        }

        MoveRecord lastMove = moveHistory.pop();

        // Восстанавливаем позицию
        int fromIndex = squareToIndex(lastMove.getFrom());
        int toIndex = squareToIndex(lastMove.getTo());
        board[fromIndex] = lastMove.getPiece();
        board[toIndex] = lastMove.getCapturedPiece();

        // Обрабатываем отмену рокировки
        String pieceType = lastMove.getPiece().substring(1);
        if (pieceType.equals("k")) {
            int fromCol = fromIndex % 8;
            int toCol = toIndex % 8;
            if (Math.abs(toCol - fromCol) == 2) {
                // Отмена рокировки
                int rookFrom, rookTo;
                if (toCol == 6) { // Короткая
                    rookFrom = fromIndex + 1; // f
                    rookTo = fromIndex + 3; // h
                } else { // Длинная
                    rookFrom = fromIndex - 1; // d
                    rookTo = fromIndex - 4; // a
                }
                board[rookTo] = board[rookFrom];
                board[rookFrom] = EMPTY;
            }
        }

        // Восстанавливаем права на рокировку
        whiteKingSideCastle = lastMove.getWhiteKingSideCastle();
        whiteQueenSideCastle = lastMove.getWhiteQueenSideCastle();
        blackKingSideCastle = lastMove.getBlackKingSideCastle();
        blackQueenSideCastle = lastMove.getBlackQueenSideCastle();

        // Восстанавливаем поле для взятия на проходе
        enPassantTarget = lastMove.getEnPassantTarget();

        // Восстанавливаем очередность хода
        whiteToMove = lastMove.isWhiteToMove();

        // Обновляем состояние игры
        updateGameStatus();

        logger.info("Ход отменен: {} <- {}", lastMove.getFrom(), lastMove.getTo());
        return true;
    }

    /**
     * Обновляет состояние игры (шах, мат, ничья).
     */
    private void updateGameStatus() {
        // Проверяем шах для игрока, который только что сделал ход
        boolean isInCheck = isInCheck(!whiteToMove);

        if (isInCheck) {
            gameStatus = "check";

            // Проверяем мат
            if (!hasLegalMoves(!whiteToMove)) {
                gameStatus = "checkmate";
            }
        } else {
            gameStatus = "playing";

            // Проверяем пат
            if (!hasLegalMoves(!whiteToMove)) {
                gameStatus = "stalemate";
            } else {
                // Проверяем ничью по правилам
                if (halfMoveClock >= 100) {
                    gameStatus = "draw";
                } else if (isThreefoldRepetition()) {
                    gameStatus = "draw";
                } else if (isDeadPosition()) {
                    gameStatus = "draw";
                }
            }
        }
    }

    /**
     * Проверяет трехкратное повторение позиции.
     */
    private boolean isThreefoldRepetition() {
        String currentFen = getCurrentPosition();
        int count = 0;
        for (String fen : positionHistory) {
            if (fen.equals(currentFen)) {
                count++;
            }
        }
        return count >= 3;
    }

    /**
     * Проверяет мертвую позицию (недостаточный материал).
     */
    private boolean isDeadPosition() {
        int whitePieces = 0, blackPieces = 0;
        boolean whiteHasBishopOrKnight = false, blackHasBishopOrKnight = false;
        boolean whiteHasBishop = false, blackHasBishop = false;

        for (String piece : board) {
            if (!piece.equals(EMPTY)) {
                if (piece.charAt(0) == 'w') {
                    whitePieces++;
                    if (piece.charAt(1) == 'b' || piece.charAt(1) == 'n') {
                        whiteHasBishopOrKnight = true;
                        if (piece.charAt(1) == 'b') whiteHasBishop = true;
                    }
                } else {
                    blackPieces++;
                    if (piece.charAt(1) == 'b' || piece.charAt(1) == 'n') {
                        blackHasBishopOrKnight = true;
                        if (piece.charAt(1) == 'b') blackHasBishop = true;
                    }
                }
            }
        }

        // Только короли
        if (whitePieces == 1 && blackPieces == 1) return true;

        // Король и слон/конь против короля
        if ((whitePieces == 1 && blackPieces == 2 && blackHasBishopOrKnight) ||
            (whitePieces == 2 && blackPieces == 1 && whiteHasBishopOrKnight)) return true;

        // Король и слон против короля и слона (на полях одного цвета)
        if (whitePieces == 2 && blackPieces == 2 && whiteHasBishop && blackHasBishop) {
            // Проверяем цвет полей слонов (упрощено)
            return true; // Для простоты считаем мертвой
        }

        return false;
    }

    /**
     * Проверяет, находится ли король под шахом.
     */
    public boolean isInCheck(boolean isWhite) {
        // Находим короля
        String kingPiece = isWhite ? "wk" : "bk";
        int kingIndex = -1;

        for (int i = 0; i < 64; i++) {
            if (board[i].equals(kingPiece)) {
                kingIndex = i;
                break;
            }
        }

        if (kingIndex == -1) {
            return false;
        }

        // Проверяем, может ли любая вражеская фигура атаковать короля
        String enemyColor = isWhite ? "b" : "w";

        for (int i = 0; i < 64; i++) {
            if (board[i].charAt(0) == enemyColor.charAt(0)) {
                if (canPieceAttackSquare(i, kingIndex)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Возвращает позицию короля для указанного цвета.
     */
    public String getKingSquare(boolean isWhite) {
        String kingPiece = isWhite ? "wk" : "bk";
        for (int i = 0; i < 64; i++) {
            if (board[i].equals(kingPiece)) {
                return indexToSquare(i);
            }
        }
        return null;
    }

    /**
     * Проверяет, может ли фигура атаковать указанную клетку.
     */
    private boolean canPieceAttackSquare(int pieceIndex, int targetIndex) {
        String piece = board[pieceIndex];
        String pieceType = piece.substring(1);

        // Для пешки проверяем специально
        if (pieceType.equals("p")) {
            return canPawnAttackSquare(pieceIndex, targetIndex);
        }

        // Проверяем базовую логику хода
        return isValidPieceMove(pieceIndex, targetIndex, pieceType);
    }

    /**
     * Проверяет, может ли пешка атаковать клетку.
     */
    private boolean canPawnAttackSquare(int pawnIndex, int targetIndex) {
        int pawnRow = pawnIndex / 8;
        int pawnCol = pawnIndex % 8;
        int targetRow = targetIndex / 8;
        int targetCol = targetIndex % 8;

        int rowDiff = targetRow - pawnRow;
        int colDiff = Math.abs(targetCol - pawnCol);

        if (colDiff != 1 || rowDiff == 0) {
            return false;
        }

        String piece = board[pawnIndex];
        boolean isWhitePawn = piece.charAt(0) == 'w';

        return isWhitePawn ? rowDiff == -1 : rowDiff == 1;
    }

    /**
     * Проверяет, есть ли легальные ходы для игрока.
     */
    public boolean hasLegalMoves(boolean isWhite) {
        for (int from = 0; from < 64; from++) {
            String piece = board[from];
            if (!piece.equals(EMPTY) && piece.charAt(0) == (isWhite ? 'w' : 'b')) {
                for (int to = 0; to < 64; to++) {
                    if (isValidMove(indexToSquare(from), indexToSquare(to), isWhite)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Получает возможные ходы для фигуры.
     */
    public List<String> getPossibleMoves(String square, boolean isWhite) {
        List<String> moves = new ArrayList<>();
        int fromIndex = squareToIndex(square);

        if (fromIndex < 0) {
            return moves;
        }

        String piece = board[fromIndex];
        if (piece.equals(EMPTY) || piece.charAt(0) != (isWhite ? 'w' : 'b')) {
            return moves;
        }

        for (int to = 0; to < 64; to++) {
            if (isValidMove(square, indexToSquare(to), isWhite)) {
                moves.add(indexToSquare(to));
            }
        }

        return moves;
    }

    /**
     * Возвращает текущий статус игры.
     */
    public String getGameStatus() {
        return gameStatus;
    }

    /**
     * Возвращает позицию в формате FEN.
     */
    public String getCurrentPosition() {
        StringBuilder fen = new StringBuilder();

        // Часть 1: Позиция фигур
        for (int row = 7; row >= 0; row--) {
            int emptyCount = 0;
            for (int col = 0; col < 8; col++) {
                String piece = board[row * 8 + col];
                if (piece.equals(EMPTY)) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    // Конвертируем внутреннее представление в FEN нотацию
                    fen.append(convertPieceToFEN(piece));
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row > 0) {
                fen.append('/');
            }
        }

        // Часть 2: Очередность хода
        fen.append(whiteToMove ? " w " : " b ");

        // Часть 3: Право на рокировку
        String castling = "";
        if (whiteKingSideCastle) castling += "K";
        if (whiteQueenSideCastle) castling += "Q";
        if (blackKingSideCastle) castling += "k";
        if (blackQueenSideCastle) castling += "q";
        if (castling.isEmpty()) castling = "-";
        fen.append(castling).append(" ");

        // Часть 4: Поле для взятия на проходе
        fen.append(enPassantTarget).append(" ");

        // Части 5 и 6: Ходы
        fen.append(halfMoveClock).append(" ");
        fen.append((positionHistory.size() / 2) + 1);

        return fen.toString();
    }

    /**
     * Конвертирует внутреннее представление фигуры в FEN нотацию.
     */
    private String convertPieceToFEN(String piece) {
        if (piece.equals(EMPTY)) {
            return "";
        }
        
        boolean isWhite = piece.charAt(0) == 'w';
        char pieceType = piece.charAt(1);
        
        // Конвертируем в FEN: белые - заглавные, черные - строчные
        char fenChar = pieceType;
        return isWhite ? String.valueOf(Character.toUpperCase(fenChar)) : String.valueOf(fenChar);
    }

    /**
     * Проверяет, чей сейчас ход.
     */
    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    /**
     * Проверяет валидность шахматной координаты.
     */
    private boolean isValidSquare(String square) {
        if (square == null || square.length() != 2) {
            return false;
        }

        char file = square.charAt(0);
        char rank = square.charAt(1);

        return file >= 'a' && file <= 'h' && rank >= '1' && rank <= '8';
    }

    /**
     * Преобразует координаты в индекс массива.
     */
    private int squareToIndex(String square) {
        if (!isValidSquare(square)) {
            return -1;
        }

        char file = square.charAt(0);
        char rank = square.charAt(1);

        int col = file - 'a';
        int row = '8' - rank;

        return row * 8 + col;
    }

    /**
     * Преобразует индекс массива в координаты.
     */
    private String indexToSquare(int index) {
        int row = index / 8;
        int col = index % 8;

        char file = (char) ('a' + col);
        char rank = (char) ('8' - row);

        return String.valueOf(file) + rank;
    }

    /**
     * Класс для записи истории ходов.
     */
    private static class MoveRecord {
        private final String from;
        private final String to;
        private final String piece;
        private final String capturedPiece;
        private final boolean whiteToMove;
        private final boolean whiteKingSideCastle;
        private final boolean whiteQueenSideCastle;
        private final boolean blackKingSideCastle;
        private final boolean blackQueenSideCastle;
        private final String enPassantTarget;

        public MoveRecord(String from, String to, String piece, String capturedPiece, boolean whiteToMove,
                         boolean whiteKingSideCastle, boolean whiteQueenSideCastle,
                         boolean blackKingSideCastle, boolean blackQueenSideCastle,
                         String enPassantTarget) {
            this.from = from;
            this.to = to;
            this.piece = piece;
            this.capturedPiece = capturedPiece;
            this.whiteToMove = whiteToMove;
            this.whiteKingSideCastle = whiteKingSideCastle;
            this.whiteQueenSideCastle = whiteQueenSideCastle;
            this.blackKingSideCastle = blackKingSideCastle;
            this.blackQueenSideCastle = blackQueenSideCastle;
            this.enPassantTarget = enPassantTarget;
        }

        public String getFrom() { return from; }
        public String getTo() { return to; }
        public String getPiece() { return piece; }
        public String getCapturedPiece() { return capturedPiece; }
        public boolean isWhiteToMove() { return whiteToMove; }
        public boolean getWhiteKingSideCastle() { return whiteKingSideCastle; }
        public boolean getWhiteQueenSideCastle() { return whiteQueenSideCastle; }
        public boolean getBlackKingSideCastle() { return blackKingSideCastle; }
        public boolean getBlackQueenSideCastle() { return blackQueenSideCastle; }
        public String getEnPassantTarget() { return enPassantTarget; }
    }
}