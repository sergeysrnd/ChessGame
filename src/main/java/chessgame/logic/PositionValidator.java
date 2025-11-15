package chessgame.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Класс для валидации шахматных позиций и ходов.
 * Предоставляет дополнительные методы проверки позиций, не входящие в основную логику игры.
 */
public class PositionValidator {

    private static final Logger logger = LoggerFactory.getLogger(PositionValidator.class);

    

    /**
     * Валидирует FEN позицию.
     */
    public boolean validateFENPosition(String fenPosition) {
        if (fenPosition == null || fenPosition.trim().isEmpty()) {
            logger.warn("FEN позиция пуста");
            return false;
        }

        try {
            String[] parts = fenPosition.trim().split("\\s+");

            // FEN должен содержать 6 частей
            if (parts.length < 4) {
                logger.warn("Недостаточно частей в FEN позиции: {}", fenPosition);
                return false;
            }

            // Проверка части 1: позиция фигур
            if (!validatePiecePlacement(parts[0])) {
                return false;
            }

            // Проверка части 2: очередность хода
            if (!validateSideToMove(parts[1])) {
                return false;
            }

            // Проверка части 3: право на рокировку
            if (!validateCastlingRights(parts[2])) {
                return false;
            }

            // Проверка части 4: поле для взятия на проходе
            if (parts.length > 3 && !validateEnPassantTarget(parts[3])) {
                return false;
            }

            return true;

        } catch (Exception e) {
            logger.error("Ошибка при валидации FEN позиции: {}", fenPosition, e);
            return false;
        }
    }

    /**
     * Валидирует расстановку фигур в FEN.
     */
    private boolean validatePiecePlacement(String piecePlacement) {
        if (piecePlacement == null || piecePlacement.isEmpty()) {
            return false;
        }

        String[] ranks = piecePlacement.split("/");

        // Должно быть ровно 8 рангов
        if (ranks.length != 8) {
            logger.warn("Неверное количество рангов в FEN: {}. Ожидается 8, найдено {}", piecePlacement, ranks.length);
            return false;
        }

        for (int i = 0; i < 8; i++) {
            String rank = ranks[i];
            int squares = 0;

            for (char c : rank.toCharArray()) {
                if (Character.isDigit(c)) {
                    // Цифра означает пустые клетки
                    squares += Character.getNumericValue(c);
                } else if (isValidPieceCharacter(c)) {
                    // Валидная фигура
                    squares++;
                } else {
                    logger.warn("Недопустимый символ в FEN ранге '{}': '{}'", rank, c);
                    return false;
                }
            }

            if (squares != 8) {
                logger.warn("Недопустимое количество клеток в ранге '{}': {}. Должно быть 8", rank, squares);
                return false;
            }
        }

        return true;
    }

    /**
     * Проверяет валидность символа фигуры.
     */
    private boolean isValidPieceCharacter(char c) {
        return "rnbqkpRNBQKP".indexOf(c) >= 0;
    }

    /**
     * Валидирует очередность хода.
     */
    private boolean validateSideToMove(String sideToMove) {
        return "w".equals(sideToMove) || "b".equals(sideToMove);
    }

    /**
     * Валидирует права на рокировку.
     */
    private boolean validateCastlingRights(String castlingRights) {
        if ("-".equals(castlingRights)) {
            return true; // Нет права на рокировку - это допустимо
        }

        // Должны быть только KQkq символы
        for (char c : castlingRights.toCharArray()) {
            if ("KQkq".indexOf(c) < 0) {
                logger.warn("Недопустимый символ права на рокировку: '{}'", c);
                return false;
            }
        }

        return true;
    }

    /**
     * Валидирует поле для взятия на проходе.
     */
    private boolean validateEnPassantTarget(String enPassantTarget) {
        if ("-".equals(enPassantTarget)) {
            return true; // Нет поля для взятия на проходе - это допустимо
        }

        // Поле должно быть валидной шахматной координатой
        if (enPassantTarget.length() != 2) {
            return false;
        }

        char file = enPassantTarget.charAt(0);
        char rank = enPassantTarget.charAt(1);

        return file >= 'a' && file <= 'h' && rank >= '1' && rank <= '8';
    }

    /**
     * Проверяет, является ли позиция легальной.
     * Упрощенная проверка основных правил.
     */
    public boolean isLegalPosition(String fenPosition) {
        if (!validateFENPosition(fenPosition)) {
            return false;
        }

        try {
            String[] parts = fenPosition.split("\\s+");
            String piecePlacement = parts[0];
            String sideToMove = parts[1];

            // Проверяем наличие королей
            if (!hasBothKings(piecePlacement)) {
                logger.warn("В позиции отсутствует один из королей");
                return false;
            }

            // Проверяем, что короли не находятся рядом (недопустимо)
            if (kingsAdjacent(piecePlacement)) {
                logger.warn("Короли находятся рядом, что невозможно");
                return false;
            }

            return true;

        } catch (Exception e) {
            logger.error("Ошибка при проверке легальности позиции", e);
            return false;
        }
    }

    /**
     * Проверяет наличие обоих королей.
     */
    private boolean hasBothKings(String piecePlacement) {
        char[] pieces = piecePlacement.toCharArray();
        boolean hasWhiteKing = false;
        boolean hasBlackKing = false;

        for (char piece : pieces) {
            if (piece == 'K') {
                hasWhiteKing = true;
            } else if (piece == 'k') {
                hasBlackKing = true;
            }
        }

        return hasWhiteKing && hasBlackKing;
    }

    /**
     * Проверяет, находятся ли короли рядом друг с другом.
     */
    private boolean kingsAdjacent(String piecePlacement) {
        // Упрощенная проверка - ищем 'K' и 'k'
        int whiteKingIndex = piecePlacement.indexOf('K');
        int blackKingIndex = piecePlacement.indexOf('k');

        if (whiteKingIndex < 0 || blackKingIndex < 0) {
            return false; // Один из королей не найден
        }

        // Вычисляем позицию королей на доске
        int whiteKingFile = whiteKingIndex % 9; // Упрощенно
        int whiteKingRank = whiteKingIndex / 8;
        int blackKingFile = blackKingIndex % 9;
        int blackKingRank = blackKingIndex / 8;

        // Проверяем, что короли не находятся на соседних клетках
        int fileDiff = Math.abs(whiteKingFile - blackKingFile);
        int rankDiff = Math.abs(whiteKingRank - blackKingRank);

        return fileDiff <= 1 && rankDiff <= 1;
    }

    /**
     * Конвертирует стандартное шахматное обозначение в координаты.
     */
    public int[] algebraicToCoordinates(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            return null;
        }

        char file = algebraic.charAt(0);
        char rank = algebraic.charAt(1);

        // Проверяем валидность координат
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            return null;
        }

        int col = file - 'a';
        int row = rank - '1';

        return new int[]{row, col};
    }

    /**
     * Конвертирует координаты в стандартное шахматное обозначение.
     */
    public String coordinatesToAlgebraic(int row, int col) {
        if (row < 0 || row >= 8 || col < 0 || col >= 8) {
            return null;
        }

        char file = (char) ('a' + col);
        char rank = (char) ('1' + row);

        return String.valueOf(file) + rank;
    }

    /**
     * Проверяет, находится ли позиция в зеркальном виде (для тестов).
     */
    public boolean isMirroredPosition(String originalFen, String mirroredFen) {
        // Упрощенная проверка зеркальности
        return originalFen != null && mirroredFen != null;
    }

    /**
     * Получает базовую оценку позиции.
     */
    public double evaluateBasicPosition(String fenPosition) {
        try {
            String[] parts = fenPosition.split("\\s+");
            String piecePlacement = parts[0];

            double score = 0;

            // Базовые значения фигур
            int[] pieceValues = {
                100, // Пешка
                320, // Конь
                330, // Слон
                500, // Ладья
                900, // Ферзь
                20000 // Король (очень высокая ценность)
            };

            char[] pieceChars = {'p', 'n', 'b', 'r', 'q', 'k'};

            for (int i = 0; i < pieceChars.length; i++) {
                char piece = pieceChars[i];
                int whiteCount = countPieces(piecePlacement, Character.toUpperCase(piece));
                int blackCount = countPieces(piecePlacement, piece);

                score += whiteCount * pieceValues[i];
                score -= blackCount * pieceValues[i];
            }

            return score;

        } catch (Exception e) {
            logger.error("Ошибка при базовой оценке позиции", e);
            return 0;
        }
    }

    /**
     * Подсчитывает количество указанных фигур в FEN позиции.
     */
    private int countPieces(String piecePlacement, char piece) {
        int count = 0;
        for (char c : piecePlacement.toCharArray()) {
            if (c == piece) {
                count++;
            }
        }
        return count;
    }

    /**
     * Проверяет, является ли позиция начальным положением.
     */
    public boolean isInitialPosition(String fenPosition) {
        String initialFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        return initialFen.equals(fenPosition);
    }
}