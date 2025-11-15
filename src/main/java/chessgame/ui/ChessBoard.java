package chessgame.ui;

import chessgame.engine.BagaturEngine;
import chessgame.logic.ChessGame;
import chessgame.sound.SoundManager;
import chessgame.sound.SoundSettingsPanel;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * Главный игровой интерфейс шахматной доски.
 * Отвечает за визуальное отображение, взаимодействие с игроком и AI.
 */
public class ChessBoard {

    private static final Logger logger = LoggerFactory.getLogger(ChessBoard.class);

    // Константы для доски
    private static final int BOARD_SIZE = 8;
    private static final double SQUARE_SIZE = 80.0;
    private static final Color LIGHT_SQUARE_COLOR = Color.LIGHTGRAY;
    private static final Color DARK_SQUARE_COLOR = Color.DARKGRAY;
    private static final Color SELECTED_COLOR = Color.YELLOW;
    private static final Color POSSIBLE_MOVE_COLOR = Color.LIGHTGREEN;

    // Шахматные координаты
    private static final String[] FILES = {"a", "b", "c", "d", "e", "f", "g", "h"};
    private static final String[] RANKS = {"8", "7", "6", "5", "4", "3", "2", "1"};

    // Основные компоненты UI
    private Stage primaryStage;
    private GridPane boardGrid;
    private BorderPane rootLayout;
    private VBox controlPanel;
    private HBox colorSelectionPanel;
    private Text statusText;
    private ProgressBar aiThinkingProgress;
    private Label scoreLabel;
    private Label lastMoveTimeLabel;
    private TextArea moveHistoryTextArea;
    private int moveNumber = 1;
    private StringBuilder currentMoveText = new StringBuilder();

    // Игровые компоненты
    private ChessGame chessGame;
    private BagaturEngine engine;
    private SoundManager soundManager;

    // Состояние игры
    private boolean isPlayerWhite = true;
    private boolean isPlayerTurn = true;
    private boolean gameInProgress = false;
    private boolean isAIThinking = false;
    private String selectedSquare = null;
    private List<String> possibleMoves = new ArrayList<>();
    private int whiteWins = 0;
    private int blackWins = 0;
    private int draws = 0;
    private LocalDateTime lastMoveTime;

    // Изображения фигур
    private Map<String, Image> pieceImages = new HashMap<>();

    // Наборы фигур
    private String currentPieceSet = "tournament";
    private List<String> availableSets = new ArrayList<>();
    private Map<String, String> soundFiles = new HashMap<>();

    // Наборы звуков
    private String currentSoundSet = "marble";
    private List<String> availableSoundSets = new ArrayList<>();

    // Сложность AI
    private String aiDifficulty = "medium";
    private Map<String, Integer> difficultyTimeLimits = new HashMap<>();

    // Исполнитель для асинхронных задач
    private final java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Конструктор шахматной доски.
     */
    public ChessBoard() {
        this.chessGame = new ChessGame();
        this.engine = new BagaturEngine(chessGame);
        this.soundManager = new SoundManager();
        

        try {
            this.engine.initialize();
            logger.info("Шахматная доска инициализирована");
        } catch (Exception e) {
            logger.error("Ошибка при инициализации доски", e);
            showErrorDialog("Ошибка инициализации", "Не удалось инициализировать AI движок");
        }

        loadPieceImages();
        scanSets();
        
        // Инициализируем новую систему управления звуками
        soundManager.scanAndLoadSoundSets("/sounds");
        
        // Настройка лимитов времени для сложности
        difficultyTimeLimits.put("easy", 1000); // 1 секунда
        difficultyTimeLimits.put("medium", 5000); // 5 секунд
        difficultyTimeLimits.put("hard", 30000); // 30 секунд
    }

    /**
     * Создает и отображает главный GUI.
     */
    public Stage createAndShowGUI() {
        Platform.runLater(() -> {
            try {
                createMainWindow();
                createBoard();
                createColorSelectionPanel();
                createControlPanel();
                layoutWindow();

                primaryStage.show();
                logger.info("GUI отображен");

                // Показываем панель выбора цвета при запуске
                colorSelectionPanel.setVisible(true);
                colorSelectionPanel.setManaged(true);
                updateStatus("Выберите цвет ваших фигур.");

            } catch (Exception e) {
                logger.error("Ошибка при создании GUI", e);
                showErrorDialog("Ошибка GUI", "Не удалось создать графический интерфейс");
            }
        });

        return primaryStage;
    }

    /**
     * Создает главное окно.
     */
    private void createMainWindow() {
        primaryStage = new Stage();
        primaryStage.setTitle("Шахматная игра - Bagatur AI");
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.DECORATED);

        // Устанавливаем иконку если есть
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/chess.png")));
        } catch (Exception e) {
            logger.warn("Не удалось загрузить иконку приложения", e);
        }
    }

    /**
     * Создает шахматную доску.
     */
    private void createBoard() {
        boardGrid = new GridPane();
        boardGrid.setStyle("-fx-border-color: black; -fx-border-width: 2;");

        // Создаем клетки доски
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                StackPane square = createSquare(row, col);
                boardGrid.add(square, col, row);
            }
        }

        // Добавляем координаты
        addCoordinateLabels();

        // Обновляем отображение доски
        updateBoardDisplay();
    }

    /**
     * Создает отдельную клетку доски.
     */
    private StackPane createSquare(int row, int col) {
        StackPane square = new StackPane();

        // Определяем цвет клетки
        Color color = (row + col) % 2 == 0 ? LIGHT_SQUARE_COLOR : DARK_SQUARE_COLOR;
        square.setStyle(String.format("-fx-background-color: #%02X%02X%02X;",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255)));

        square.setMinSize(SQUARE_SIZE, SQUARE_SIZE);
        square.setMaxSize(SQUARE_SIZE, SQUARE_SIZE);

        // Добавляем обработчик кликов
        square.setOnMouseClicked(event -> handleSquareClick(event, row, col));

        // Добавляем ImageView для фигур
        ImageView pieceView = new ImageView();
        pieceView.setFitWidth(SQUARE_SIZE * 0.8);
        pieceView.setFitHeight(SQUARE_SIZE * 0.8);
        square.getChildren().add(pieceView);

        // Сохраняем ссылку на ImageView
        square.setUserData(pieceView);

        return square;
    }

    /**
     * Обрабатывает клики по клеткам доски.
     */
    private void handleSquareClick(MouseEvent event, int row, int col) {
        if (!gameInProgress || !isPlayerTurn || isAIThinking) {
            return;
        }

        String square = getSquareCoordinates(row, col);
        logger.info("Клик по клетке: {}", square);

        // Получаем позицию фигуры
        String piece = chessGame.getPieceAt(square);

        if (selectedSquare == null) {
            // Выбираем фигуру
            if (piece != null && isPlayersPiece(piece)) {
                selectSquare(square);
            }
        } else {
            // Делаем ход
            if (square.equals(selectedSquare)) {
                // Отменяем выбор
                clearSelection();
            } else if (possibleMoves.contains(square)) {
                // Выполняем ход
                makePlayerMove(selectedSquare, square);
            } else if (piece != null && isPlayersPiece(piece)) {
                // Выбираем другую фигуру
                selectSquare(square);
            } else {
                // Отменяем выбор
                clearSelection();
            }
        }
    }

    /**
     * Выбирает клетку и показывает возможные ходы.
     */
    private void selectSquare(String square) {
        clearSelection();

        this.selectedSquare = square;
        this.possibleMoves = chessGame.getPossibleMoves(square, isPlayerWhite);

        // Подсвечиваем выбранную клетку
        highlightSquare(square, SELECTED_COLOR);

        // Подсвечиваем возможные ходы
        for (String move : possibleMoves) {
            highlightSquare(move, POSSIBLE_MOVE_COLOR);
        }

        updateStatus("Выбрана фигура: " + chessGame.getPieceAt(square) + " на " + square);
    }

    /**
     * Очищает выделение.
     */
    private void clearSelection() {
        selectedSquare = null;
        possibleMoves.clear();
        clearHighlights();
        updateBoardDisplay();
    }

    /**
     * Очищает подсветку всех клеток.
     */
    private void clearHighlights() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                StackPane square = (StackPane) boardGrid.getChildren().get(row * BOARD_SIZE + col);
                // Сбрасываем стиль к стандартному фону
                Color color = (row + col) % 2 == 0 ? LIGHT_SQUARE_COLOR : DARK_SQUARE_COLOR;
                square.setStyle(String.format("-fx-background-color: #%02X%02X%02X;",
                        (int)(color.getRed() * 255),
                        (int)(color.getGreen() * 255),
                        (int)(color.getBlue() * 255)));
            }
        }
    }

    /**
     * Подсвечивает клетку определенным цветом.
     */
    private void highlightSquare(String square, Color color) {
        int[] coords = getSquareCoords(square);
        if (coords != null) {
            StackPane squarePane = (StackPane) boardGrid.getChildren()
                .get(coords[0] * BOARD_SIZE + coords[1]);
            squarePane.setStyle(squarePane.getStyle() + String.format(" -fx-border-color: #%02X%02X%02X; -fx-border-width: 3;",
                    (int)(color.getRed() * 255),
                    (int)(color.getGreen() * 255),
                    (int)(color.getBlue() * 255)));
        }
    }

    /**
     * Выполняет ход игрока.
     */
    private void makePlayerMove(String from, String to) {
        try {
            // Валидируем ход
            if (chessGame.isValidMove(from, to, isPlayerWhite)) {
                String capturedPiece = chessGame.getPieceAt(to);
                // Выполняем ход
                chessGame.makeMove(from, to);

                // Проигрываем звук
                if (capturedPiece != null && !capturedPiece.equals("empty")) {
                    playSound("capture");
                } else {
                    playSound("move");
                }

                // Добавляем ход в историю
                currentMoveText.append(moveNumber).append(". ").append(from).append("-").append(to).append(" ");
                moveHistoryTextArea.setText(currentMoveText.toString());

                // Очищаем выделение
                clearSelection();

                // Обновляем отображение
                updateBoardDisplay();

                // Обновляем время последнего хода
                lastMoveTime = LocalDateTime.now();
                updateLastMoveTime();

                // Обновляем статус
                updateStatus("Ход выполнен: " + from + "-" + to);

                // Проверяем конец игры
                if (checkGameEnd()) {
                    return;
                }

                // Переключаем ход
                isPlayerTurn = false;
                updateStatus("AI думает...");

                // Ход AI
                makeAIMove();

            } else {
                showWarningDialog("Недопустимый ход", "Ход " + from + "-" + to + " невозможен");
                clearSelection();
            }
        } catch (Exception e) {
            logger.error("Ошибка при выполнении хода", e);
            showErrorDialog("Ошибка хода", "Произошла ошибка при выполнении хода");
        }
    }

    /**
     * Выполняет ход AI.
     */
    private void makeAIMove() {
        isAIThinking = true;
        CompletableFuture.supplyAsync(() -> {
            try {
                aiThinkingProgress.setVisible(true);

                // Получаем текущую позицию
                String fenPosition = chessGame.getCurrentPosition();

                // Запрашиваем ход у AI
                BagaturEngine.ChessMove aiMove = engine.getBestMove(fenPosition, isPlayerWhite ? 'b' : 'w');

                return aiMove;
            } catch (Exception e) {
                logger.error("Ошибка при получении хода AI", e);
                return null;
            }
        }, executor).orTimeout(difficultyTimeLimits.get(aiDifficulty), java.util.concurrent.TimeUnit.MILLISECONDS).thenAccept(aiMove -> {
            Platform.runLater(() -> {
                aiThinkingProgress.setVisible(false);
                isAIThinking = false;

                if (aiMove != null) {
                    // Выполняем ход AI
                    String from = aiMove.getFrom();
                    String to = aiMove.getTo();

                    if (chessGame.isValidMove(from, to, !isPlayerWhite)) {
                        chessGame.makeMove(from, to);

                        // Добавляем ход AI в историю
                        currentMoveText.append(from).append("-").append(to).append("\n");
                        moveHistoryTextArea.setText(currentMoveText.toString());
                        moveNumber++;

                        updateBoardDisplay();

                        // Обновляем время последнего хода
                        lastMoveTime = LocalDateTime.now();
                        updateLastMoveTime();

                        updateStatus("AI сделал ход: " + from + "-" + to);

                        // Проверяем конец игры
                        if (checkGameEnd()) {
                            return;
                        }

                        // Переключаем ход обратно игроку
                        isPlayerTurn = true;
                        updateStatus("Ваш ход");
                    } else {
                        logger.warn("AI попытался сделать недопустимый ход: " + from + "-" + to);
                        isPlayerTurn = true;
                        updateStatus("Ваш ход");
                    }
                } else {
                    logger.warn("AI не смог найти ход");
                    // Проверяем, есть ли легальные ходы для AI
                    if (!chessGame.hasLegalMoves(!isPlayerWhite)) {
                        if (chessGame.isInCheck(!isPlayerWhite)) {
                            // Мат
                            String winner = isPlayerWhite ? "Белые" : "Черные";
                            showGameEndDialog("Мат!", winner + " победили");

                            // Обновляем счет
                            if (winner.equals("Белые")) {
                                whiteWins++;
                            } else {
                                blackWins++;
                            }
                            updateScore();

                            gameInProgress = false;
                        } else {
                            // Пат
                            showGameEndDialog("Ничья", "Игра завершилась вничью");
                            draws++;
                            updateScore();
                            gameInProgress = false;
                        }
                    } else {
                        // AI не нашел ход, но ходы есть - ошибка движка
                        isPlayerTurn = true;
                        updateStatus("Ваш ход");
                    }
                }
            });
        });
    }

    /**
     * Проверяет окончание игры.
     */
    private boolean checkGameEnd() {
        String gameStatus = chessGame.getGameStatus();

        if ("checkmate".equalsIgnoreCase(gameStatus)) {
            String winner = chessGame.isWhiteToMove() ? "Черные" : "Белые";
            showGameEndDialog("Мат!", winner + " победили");

            // Обновляем счет
            if (winner.equals("Белые")) {
                whiteWins++;
            } else {
                blackWins++;
            }
            updateScore();

            gameInProgress = false;
            return true;
        } else if ("stalemate".equalsIgnoreCase(gameStatus) || "draw".equalsIgnoreCase(gameStatus)) {
            showGameEndDialog("Ничья", "Игра завершилась вничью");
            draws++;
            updateScore();
            gameInProgress = false;
            return true;
        } else if ("check".equalsIgnoreCase(gameStatus)) {
            String inCheck = chessGame.isWhiteToMove() ? "белых" : "черных";
            updateStatus("Шах " + inCheck);
            // Подсвечиваем короля под шахом
            highlightKingInCheck(!chessGame.isWhiteToMove());
            // Проигрываем звук шаха
            playSound("check");
        } else {
            // Убираем подсветку короля, если шаха нет
            clearKingHighlight();
        }

        return false;
    }

    /**
     * Создает панель выбора цвета.
     */
    private void createColorSelectionPanel() {
        colorSelectionPanel = new HBox(20);
        colorSelectionPanel.setPadding(new Insets(10));
        colorSelectionPanel.setAlignment(Pos.CENTER);
        colorSelectionPanel.setStyle("-fx-background-color: #e8e8e8; -fx-border-color: black; -fx-border-width: 1;");

        Text title = new Text("Выберите цвет ваших фигур:");
        title.setFont(new Font("Arial", 14));
        title.setStyle("-fx-font-weight: bold;");

        Button whiteButton = new Button("Белые");
        whiteButton.setPrefSize(100, 30);
        whiteButton.setStyle("-fx-font-size: 12px; -fx-background-color: #ffffff; -fx-border-color: #333;");
        whiteButton.setOnAction(e -> selectPlayerColor(true));

        Button blackButton = new Button("Черные");
        blackButton.setPrefSize(100, 30);
        blackButton.setStyle("-fx-font-size: 12px; -fx-background-color: #333333; -fx-text-fill: white; -fx-border-color: #666;");
        blackButton.setOnAction(e -> selectPlayerColor(false));

        colorSelectionPanel.getChildren().addAll(title, whiteButton, blackButton);
    }

    /**
     * Обрабатывает выбор цвета игрока.
     */
    private void selectPlayerColor(boolean isWhite) {
        isPlayerWhite = isWhite;
        isPlayerTurn = isWhite;
        gameInProgress = true;
        clearSelection();
        updateBoardDisplay();

        if (isWhite) {
            updateStatus("Игра началась. Ваш ход (белые).");
        } else {
            updateStatus("Игра началась. AI думает (белые)...");
            // Для первого хода AI добавляем номер
            currentMoveText.append(moveNumber).append(". ");
            makeAIMove();
        }

        // Скрываем панель выбора цвета
        colorSelectionPanel.setVisible(false);
        colorSelectionPanel.setManaged(false);
    }

    /**
     * Создает панель управления.
     */
    private void createControlPanel() {
        controlPanel = new VBox(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setStyle("-fx-border-color: black; -fx-border-width: 1;");
        controlPanel.setPrefWidth(280);

        // Заголовок
        Text title = new Text("Шахматная игра");
        title.setFont(new Font("Arial", 16));
        title.setStyle("-fx-font-weight: bold;");

        // Статус игры
        statusText = new Text("Нажмите 'Новая партия' для начала");
        statusText.setFont(new Font("Arial", 12));

        // Счет
        scoreLabel = new Label("Счет: Белые 0 - Черные 0 - Ничьи 0");
        scoreLabel.setFont(new Font("Arial", 12));

        // Время последнего хода
        lastMoveTimeLabel = new Label("Последний ход: --:--:--");
        lastMoveTimeLabel.setFont(new Font("Arial", 10));

        // Кнопки управления
        Button newGameBtn = new Button("Новая партия");
        newGameBtn.setOnAction(event -> newGame());

        Button undoBtn = new Button("Отменить ход");
        undoBtn.setOnAction(event -> undoMove());
        undoBtn.setDisable(true);

        Button changePiecesBtn = new Button("Change Pieces");
        changePiecesBtn.setOnAction(event -> showPieceSetDialog());

        Button changeSoundsBtn = new Button("Change Sounds");
        changeSoundsBtn.setOnAction(event -> showSoundSetDialog());

        Label difficultyLabel = new Label("AI Difficulty:");
        ComboBox<String> difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll("easy", "medium", "hard");
        difficultyCombo.setValue(aiDifficulty);
        difficultyCombo.setOnAction(e -> aiDifficulty = difficultyCombo.getValue());

        Button exitBtn = new Button("Выход");
        exitBtn.setOnAction(event -> exitGame());

        // Индикатор работы AI
        aiThinkingProgress = new ProgressBar();
        aiThinkingProgress.setVisible(false);
        aiThinkingProgress.setPrefWidth(200);

        // История ходов
        Text moveHistoryLabel = new Text("История ходов:");
        moveHistoryLabel.setFont(new Font("Arial", 12));

        moveHistoryTextArea = new TextArea();
        moveHistoryTextArea.setEditable(false);
        moveHistoryTextArea.setPrefRowCount(8);
        moveHistoryTextArea.setPrefWidth(270);

        // Добавляем компоненты
        controlPanel.getChildren().addAll(
            title,
            statusText,
            scoreLabel,
            lastMoveTimeLabel,
            new Separator(),
            newGameBtn,
            changePiecesBtn,
            changeSoundsBtn,
            difficultyLabel,
            difficultyCombo,
            undoBtn,
            new Label(""),
            aiThinkingProgress,
            new Separator(),
            moveHistoryLabel,
            moveHistoryTextArea,
            new Label(""),
            exitBtn
        );
    }

    /**
     * Компонует главное окно.
     */
    private void layoutWindow() {
        rootLayout = new BorderPane();
        rootLayout.setTop(colorSelectionPanel);
        rootLayout.setCenter(boardGrid);
        rootLayout.setRight(controlPanel);

        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(BOARD_SIZE * SQUARE_SIZE + 320);
        primaryStage.setMinHeight(BOARD_SIZE * SQUARE_SIZE + 100);
    }

    /**
     * Загружает изображения фигур.
     */
    private void loadPieceImages() {
        String[] pieceTypes = {"p", "n", "b", "r", "q", "k"};
        String[] colors = {"w", "b"};

        for (String color : colors) {
            for (String piece : pieceTypes) {
                try {
                    String path = "/pieces/" + currentPieceSet + "/" + color + piece + ".png";
                    try {
                        pieceImages.put(color + piece, new Image(getClass().getResourceAsStream(path)));
                        logger.debug("Загружено изображение: {}", path);
                    } catch (Exception e) {
                        logger.warn("Не удалось загрузить изображение: {}", path, e);
                        // Fallback to default
                        loadFallbackImage(color + piece);
                    }
                } catch (Exception e) {
                    logger.warn("Не удалось загрузить изображение для " + color + piece, e);
                }
            }
        }
    }

    /**
     * Загружает изображение из запасного набора.
     */
    private void loadFallbackImage(String pieceKey) {
        String[] fallbackThemes = {"tournament", "wood"};
        for (String theme : fallbackThemes) {
            if (!theme.equals(currentPieceSet)) {
                String path = "/pieces/" + theme + "/" + pieceKey.charAt(0) + pieceKey.charAt(1) + ".png";
                try {
                    pieceImages.put(pieceKey, new Image(getClass().getResourceAsStream(path)));
                    logger.debug("Загружено запасное изображение: {}", path);
                    return;
                } catch (Exception e) {
                    // continue
                }
            }
        }
    }

    /**
     * Сканирует доступные наборы фигур.
     */
    private void scanSets() {
        try {
            URL resUrl = getClass().getResource("/pieces");
            if (resUrl != null) {
                java.io.File piecesDir = new java.io.File(resUrl.toURI());
                if (piecesDir.isDirectory()) {
                    java.io.File[] subdirs = piecesDir.listFiles(java.io.File::isDirectory);
                    if (subdirs != null) {
                        for (java.io.File subdir : subdirs) {
                            availableSets.add(subdir.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка при сканировании наборов фигур", e);
        }
    }

    /**
     * Сканирует доступные наборы звуков.
     */
    private void scanSoundSets() {
        try {
            URL resUrl = getClass().getResource("/sounds");
            if (resUrl != null) {
                java.io.File soundsDir = new java.io.File(resUrl.toURI());
                if (soundsDir.isDirectory()) {
                    java.io.File[] subdirs = soundsDir.listFiles(java.io.File::isDirectory);
                    if (subdirs != null) {
                        for (java.io.File subdir : subdirs) {
                            availableSoundSets.add(subdir.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка при сканировании наборов звуков", e);
        }
    }

    /**
     * Загружает звуковые файлы.
     */
    private void loadSounds() {
        String[] soundTypes = {"move", "capture", "check", "checkmate"};
        for (String sound : soundTypes) {
            try {
                String path = "/sounds/" + currentSoundSet + "/" + sound + ".mp3";
                URL soundUrl = getClass().getResource(path);
                if (soundUrl != null) {
                    soundFiles.put(sound, path);
                    logger.debug("Загружен звук: {}", path);
                } else {
                    logger.warn("Звук не найден: {}", path);
                }
            } catch (Exception e) {
                logger.warn("Не удалось загрузить звук: {}", sound, e);
            }
        }
    }

    /**
     * Показывает диалог выбора набора фигур.
     */
    private void showPieceSetDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Choose Piece Set");
        dialog.setHeaderText("Select a set of chess pieces:");

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(availableSets);
        comboBox.setValue(currentPieceSet);

        dialog.getDialogPane().setContent(comboBox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return comboBox.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(set -> {
            currentPieceSet = set;
            loadPieceImages();
            loadSounds();
            updateBoardDisplay();
            logger.info("Выбран набор фигур: {}", set);
        });
    }

    /**
     * Показывает диалог выбора набора звуков.
     */
    private void showSoundSetDialog() {
        Stage soundSettingsStage = new Stage();
        soundSettingsStage.setTitle("Управление звуками");
        soundSettingsStage.setResizable(true);
        soundSettingsStage.initModality(Modality.APPLICATION_MODAL);
        soundSettingsStage.initOwner(primaryStage);
        
        SoundSettingsPanel soundPanel = new SoundSettingsPanel(soundManager);
        ScrollPane scrollPane = new ScrollPane(soundPanel);
        scrollPane.setFitToWidth(true);
        
        Scene scene = new Scene(scrollPane, 500, 600);
        soundSettingsStage.setScene(scene);
        soundSettingsStage.show();
        
        logger.info("Открыто окно управления звуками");
    }

    /**
     * Подсвечивает короля под шахом.
     */
    private void highlightKingInCheck(boolean isWhiteKing) {
        String kingSquare = chessGame.getKingSquare(isWhiteKing);
        if (kingSquare != null) {
            highlightSquare(kingSquare, Color.RED); // Красный цвет для шаха
        }
    }

    /**
     * Убирает подсветку короля.
     */
    private void clearKingHighlight() {
        // Поскольку мы не знаем, какой король был подсвечен, перерисовываем доску
        updateBoardDisplay();
    }

    /**
     * Проигрывает звук через SoundManager.
     */
    private void playSound(String soundType) {
        if (soundManager != null) {
            soundManager.playSound(soundType);
        }
    }


    /**
     * Обновляет отображение доски.
     */
    private void updateBoardDisplay() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                updateSquareDisplay(row, col);
            }
        }
    }

    /**
     * Обновляет отображение отдельной клетки.
     */
    private void updateSquareDisplay(int row, int col) {
        StackPane square = (StackPane) boardGrid.getChildren()
            .get(row * BOARD_SIZE + col);
        ImageView pieceView = (ImageView) square.getUserData();

        String squareCoord = getSquareCoordinates(row, col);
        String piece = chessGame.getPieceAt(squareCoord);

        if (piece != null && !piece.equals("empty")) {
            Image pieceImage = pieceImages.get(piece);
            if (pieceImage != null) {
                pieceView.setImage(pieceImage);
            } else {
                pieceView.setImage(null);
            }
        } else {
            pieceView.setImage(null);
        }
    }

    /**
     * Добавляет координатные метки.
     */
    private void addCoordinateLabels() {
        // Файлы (буквы снизу)
        for (int col = 0; col < BOARD_SIZE; col++) {
            Text fileLabel = new Text(FILES[col]);
            fileLabel.setFont(new Font("Arial", 12));
            fileLabel.setStyle("-fx-font-weight: bold;");

            StackPane labelPane = new StackPane(fileLabel);
            labelPane.setMinSize(SQUARE_SIZE, 20);
            labelPane.setMaxSize(SQUARE_SIZE, 20);
            labelPane.setAlignment(Pos.TOP_CENTER);

            boardGrid.add(labelPane, col, BOARD_SIZE);
        }

        // Ранги (цифры слева)
        for (int row = 0; row < BOARD_SIZE; row++) {
            Text rankLabel = new Text(RANKS[row]);
            rankLabel.setFont(new Font("Arial", 12));
            rankLabel.setStyle("-fx-font-weight: bold;");

            StackPane labelPane = new StackPane(rankLabel);
            labelPane.setMinSize(20, SQUARE_SIZE);
            labelPane.setMaxSize(20, SQUARE_SIZE);
            labelPane.setAlignment(Pos.CENTER_LEFT);

            boardGrid.add(labelPane, 0, row);
        }
    }

    /**
     * Начинает новую игру.
     */
    public void newGame() {
        chessGame = new ChessGame();
        gameInProgress = false;
        isAIThinking = false;
        clearSelection();
        updateBoardDisplay();
        // Сбрасываем историю ходов
        moveNumber = 1;
        currentMoveText.setLength(0);
        moveHistoryTextArea.setText("");
        // Показываем панель выбора цвета
        colorSelectionPanel.setVisible(true);
        colorSelectionPanel.setManaged(true);
        updateStatus("Выберите цвет ваших фигур.");
    }

    /**
     * Отменяет последний ход.
     */
    private void undoMove() {
        if (chessGame.undoMove()) {
            updateBoardDisplay();
            updateStatus("Ход отменен");
            clearSelection();
        }
    }

    /**
     * Завершает игру.
     */
    private void exitGame() {
        if (gameInProgress) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Выход");
            alert.setHeaderText("Завершить игру?");
            alert.setContentText("Вы действительно хотите выйти?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                cleanup();
                System.exit(0);
            }
        } else {
            cleanup();
            System.exit(0);
        }
    }

    /**
     * Показывает диалог выбора цвета фигур под игровым окном.
     */
    public void showColorSelectionDialog() {
        // Создаем кастомный диалог под главным окном
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.NONE); // Non-modal so it doesn't block the game
        dialog.setTitle("Выбор цвета фигур");
        dialog.setResizable(false);
        dialog.initStyle(StageStyle.UTILITY);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #333; -fx-border-width: 2;");

        // Заголовок
        Text title = new Text("Выберите цвет ваших фигур:");
        title.setFont(Font.font(18));
        title.setStyle("-fx-fill: #333;");

        // Информационный текст
        Text info = new Text("За какие фигуры вы хотите играть?");
        info.setFont(Font.font(14));
        info.setStyle("-fx-fill: #666;");

        // Кнопки
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button whiteButton = new Button("Белые");
        whiteButton.setPrefSize(120, 40);
        whiteButton.setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff; -fx-border-color: #333; -fx-border-width: 1;");
        whiteButton.setOnAction(e -> {
            isPlayerWhite = true;
            isPlayerTurn = true;
            gameInProgress = true;
            updateStatus("Игра началась. Ваш ход.");
            dialog.close();
        });

        Button blackButton = new Button("Черные");
        blackButton.setPrefSize(120, 40);
        blackButton.setStyle("-fx-font-size: 14px; -fx-background-color: #333333; -fx-text-fill: white; -fx-border-color: #666; -fx-border-width: 1;");
        blackButton.setOnAction(e -> {
            isPlayerWhite = false;
            isPlayerTurn = false;
            gameInProgress = true;
            updateStatus("Игра началась. AI думает...");
            dialog.close();
            makeAIMove();
        });

        Button cancelButton = new Button("Отмена");
        cancelButton.setPrefSize(120, 40);
        cancelButton.setStyle("-fx-font-size: 14px; -fx-background-color: #ff6b6b; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> {
            dialog.close();
            System.exit(0);
        });

        buttonBox.getChildren().addAll(whiteButton, blackButton, cancelButton);

        // Добавляем все компоненты
        root.getChildren().addAll(title, info, buttonBox);

        // Создаем сцену
        Scene scene = new Scene(root, 400, 250);
        dialog.setScene(scene);

        // Позиционируем диалог под главным окном
        if (primaryStage != null) {
            double primaryStageX = primaryStage.getX();
            double primaryStageY = primaryStage.getY();
            double primaryStageHeight = primaryStage.getHeight();
            
            // Размещаем диалог под главным окном с небольшим отступом
            dialog.setX(primaryStageX + 50); // Смещение влево на 50 пикселей
            dialog.setY(primaryStageY + primaryStageHeight + 20); // 20 пикселей под главным окном
        }

        // Обработчик закрытия окна
        dialog.setOnCloseRequest(event -> {
            System.exit(0);
        });

        // Показываем диалог (не блокирующий)
        dialog.show();
    }

    /**
     * Обновляет счет игры.
     */
    private void updateScore() {
        scoreLabel.setText(String.format("Счет: Белые %d - Черные %d - Ничьи %d",
            whiteWins, blackWins, draws));

        // Адаптивная сложность: если игрок выигрывает часто, повышать
        int totalGames = whiteWins + blackWins + draws;
        if (totalGames >= 3) {
            int playerWins = isPlayerWhite ? whiteWins : blackWins;
            int aiWins = isPlayerWhite ? blackWins : whiteWins;
            if (playerWins > aiWins + 1) {
                if (!aiDifficulty.equals("hard")) {
                    aiDifficulty = "hard";
                    updateStatus("Сложность повышена до hard");
                }
            } else if (aiWins > playerWins + 1) {
                if (!aiDifficulty.equals("easy")) {
                    aiDifficulty = "easy";
                    updateStatus("Сложность понижена до easy");
                }
            }
        }
    }

    /**
     * Обновляет время последнего хода.
     */
    private void updateLastMoveTime() {
        if (lastMoveTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            lastMoveTimeLabel.setText("Последний ход: " + lastMoveTime.format(formatter));
        }
    }

    /**
     * Показывает диалог ошибки.
     */
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Показывает диалог предупреждения.
     */
    private void showWarningDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Показывает диалог завершения игры.
     */
    private void showGameEndDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        updateStatus(title + ": " + message);
        gameInProgress = false;
    }

    /**
     * Обновляет статус игры.
     */
    private void updateStatus(String message) {
        statusText.setText(message);
    }

    /**
     * Проверяет, является ли фигура фигурой игрока.
     */
    private boolean isPlayersPiece(String piece) {
        if (piece == null || piece.equals("empty")) {
            return false;
        }

        char pieceColor = piece.charAt(0);
        return (isPlayerWhite && pieceColor == 'w') || (!isPlayerWhite && pieceColor == 'b');
    }

    /**
     * Получает координаты клетки по row/col.
     */
    private String getSquareCoordinates(int row, int col) {
        return FILES[col] + RANKS[row];
    }

    /**
     * Получает row/col по координатам клетки.
     */
    private int[] getSquareCoords(String square) {
        if (square.length() != 2) {
            return null;
        }

        char file = square.charAt(0);
        char rank = square.charAt(1);

        int col = file - 'a';
        int row = '8' - rank;

        if (col >= 0 && col < BOARD_SIZE && row >= 0 && row < BOARD_SIZE) {
            return new int[]{row, col};
        }

        return null;
    }

    /**
     * Очищает ресурсы при закрытии.
     */
    public void cleanup() {
        if (engine != null) {
            engine.cleanup();
        }
        if (soundManager != null) {
            soundManager.shutdown();
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}