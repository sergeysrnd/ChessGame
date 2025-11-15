package chessgame.engine;

import chessgame.logic.ChessGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Обертка для шахматного движка Bagatur.
 * Обеспечивает взаимодействие с AI для получения лучших ходов.*/
public class BagaturEngine {

    private static final Logger logger = LoggerFactory.getLogger(BagaturEngine.class);

    // Константы для шахматной позиции
    private static final String INITIAL_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    // Ссылка на игровую логику для валидации ходов
    private ChessGame chessGame;

    // Текущая позиция в FEN формате
    private String currentPosition;

    // Флаг инициализации движка
    private boolean isInitialized;

    // Время поиска по умолчанию (в миллисекундах)
    private static final long SEARCH_TIME_MS = 2000;

    /**
     * Конструктор движка Bagatur.
     */
    public BagaturEngine(ChessGame chessGame) {
        this.chessGame = chessGame;
        this.currentPosition = INITIAL_POSITION;
        this.isInitialized = false;
    }

    /**
     * Инициализирует шахматный движок Bagatur.
     */
    public void initialize() {
        try {
            logger.info("Инициализация движка Bagatur...");

            // Имитируем инициализацию движка Bagatur
            simulateEngineInitialization();

            this.isInitialized = true;
            logger.info("Движок Bagatur успешно инициализирован");

        } catch (Exception e) {
            logger.error("Ошибка при инициализации движка Bagatur", e);
            throw new RuntimeException("Не удалось инициализировать движок Bagatur", e);
        }
    }

    /**
     * Симуляция инициализации движка для демонстрации.
     */
    private void simulateEngineInitialization() {
        // Имитация загрузки движка
        try {
            Thread.sleep(500); // Имитируем время загрузки
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Получает лучший ход для текущей позиции.
     *
     * @param fenPosition текущая позиция в формате FEN
     * @param playerColor цвет игрока ('w' для белых, 'b' для черных)
     * @return лучший ход или null если ход не найден
     */
    public ChessMove getBestMove(String fenPosition, char playerColor) {
        if (!isInitialized) {
            throw new IllegalStateException("Движок не инициализирован");
        }

        try {
            logger.info("Поиск лучшего хода для позиции: {}", fenPosition.substring(0, Math.min(50, fenPosition.length())));

            // Обновляем текущую позицию
            this.currentPosition = fenPosition;

            // Асинхронно ищем лучший ход
            CompletableFuture<ChessMove> futureMove = CompletableFuture.supplyAsync(() -> {
                try {
                    // Имитируем время поиска
                    Thread.sleep(Math.min(SEARCH_TIME_MS, 100));
                    return findMove(fenPosition, playerColor);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Поиск хода был прерван", e);
                    return null;
                }
            });

            // Ожидаем результат
            ChessMove bestMove = futureMove.get(SEARCH_TIME_MS, TimeUnit.MILLISECONDS);

            if (bestMove != null) {
                logger.info("Найден лучший ход: {}", bestMove);
            } else {
                logger.warn("Движок не смог найти ход");
            }

            return bestMove;

        } catch (Exception e) {
            logger.error("Ошибка при поиске лучшего хода", e);
            return null;
        }
    }

    /**
     * Симуляция поиска хода для демонстрации.
     */
    private ChessMove findMove(String fenPosition, char playerColor) {
        // Простая логика выбора хода для демонстрации
        List<ChessMove> possibleMoves = generatePossibleMoves(fenPosition, playerColor);

        if (possibleMoves.isEmpty()) {
            logger.warn("No possible moves found for color: {}", playerColor);
            return null;
        }

        // Выбираем случайный ход из возможных (в реальном движке здесь был бы анализ)
        Random random = new Random();
        ChessMove selectedMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
        
        // Validate the selected move one more time before returning
        boolean isWhite = playerColor == 'w';
        if (!chessGame.isValidMove(selectedMove.getFrom(), selectedMove.getTo(), isWhite)) {
            logger.warn("Selected move {} is invalid, trying to find alternative", selectedMove);
            // Try to find a valid move
            for (ChessMove move : possibleMoves) {
                if (chessGame.isValidMove(move.getFrom(), move.getTo(), isWhite)) {
                    logger.info("Found valid alternative move: {}", move);
                    return move;
                }
            }
            logger.error("No valid moves found in possible moves list!");
            return null;
        }
        
        return selectedMove;
    }

    /**
     * Генерирует список возможных ходов для демонстрации.
     */
    private List<ChessMove> generatePossibleMoves(String fenPosition, char playerColor) {
        List<ChessMove> moves = new ArrayList<>();

        // Генерируем все возможные ходы для фигур игрока
        boolean isWhite = playerColor == 'w';
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String square = (char)('a' + col) + "" + (char)('8' - row);
                List<String> possibleMovesFromSquare = chessGame.getPossibleMoves(square, isWhite);
                for (String to : possibleMovesFromSquare) {
                    ChessMove move = new ChessMove(square, to, "piece");
                    moves.add(move);
                    logger.debug("Generated move: {} -> {} for color {}", square, to, playerColor);
                }
            }
        }

        if (moves.isEmpty()) {
            logger.warn("No possible moves generated for color: {}", playerColor);
        }

        return moves;
    }

    /**
     * Проверяет, принадлежит ли ход указанному цвету.
     */
    private boolean isMoveForPlayerColor(String from, String to, char playerColor) {
        // Простая проверка: для белых используем верхнюю часть доски, для черных - нижнюю
        if (playerColor == 'w') {
            return from.charAt(1) <= '4' || to.charAt(1) <= '4';
        } else {
            return from.charAt(1) >= '5' || to.charAt(1) >= '5';
        }
    }

    /**
     * Генерирует запасные ходы, если основные не подходят.
     */
    private List<ChessMove> generateFallbackMoves(char playerColor) {
        List<ChessMove> moves = new ArrayList<>();
        String[] files = {"a", "b", "c", "d", "e", "f", "g", "h"};
        String[] ranks = playerColor == 'w' ? new String[]{"3", "4"} : new String[]{"5", "6"};
        
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            String from = files[random.nextInt(8)] + ranks[random.nextInt(2)];
            String to = files[random.nextInt(8)] + ranks[random.nextInt(2)];
            if (!from.equals(to)) {
                moves.add(new ChessMove(from, to, "piece"));
            }
        }
        return moves;
    }

    /**
     * Оценивает текущую позицию.
     *
     * @param fenPosition позиция в формате FEN
     * @return оценка позиции (положительная для белых, отрицательная для черных)
     */
    public double evaluatePosition(String fenPosition) {
        // Простая оценка для демонстрации
        // В реальном движке здесь был бы сложный анализ
        Random random = new Random();
        return random.nextDouble() * 2 - 1; // Возвращает значение от -1 до 1
    }

    /**
     * Проверяет, является ли ход легальным.
     *
     * @param move ход для проверки
     * @param fenPosition текущая позиция
     * @return true если ход легален
     */
    public boolean isLegalMove(ChessMove move, String fenPosition) {
        // Простая проверка легальности
        if (move == null || move.getFrom() == null || move.getTo() == null) {
            return false;
        }

        // Проверяем координаты
        return isValidSquare(move.getFrom()) && isValidSquare(move.getTo());
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
     * Возвращает текущую позицию в формате FEN.
     */
    public String getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Устанавливает новую позицию.
     */
    public void setPosition(String fenPosition) {
        this.currentPosition = fenPosition;
    }

    /**
     * Проверяет, инициализирован ли движок.
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Очищает ресурсы движка.
     */
    public void cleanup() {
        if (isInitialized) {
            logger.info("Очистка ресурсов движка Bagatur");
            isInitialized = false;
        }
    }

    /**
     * Класс для представления шахматного хода.
     */
    public static class ChessMove {
        private final String from;
        private final String to;
        private final String pieceType;

        public ChessMove(String from, String to, String pieceType) {
            this.from = from;
            this.to = to;
            this.pieceType = pieceType;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public String getPieceType() {
            return pieceType;
        }

        @Override
        public String toString() {
            return String.format("%s%s (%s)", from, to, pieceType);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChessMove chessMove = (ChessMove) o;
            return Objects.equals(from, chessMove.from) &&
                   Objects.equals(to, chessMove.to) &&
                   Objects.equals(pieceType, chessMove.pieceType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to, pieceType);
        }
    }
}