package com.chessgame.view;

import com.chessgame.config.GameConfig;
import com.chessgame.controller.GameController;
import com.chessgame.util.FontConfig;
import com.chessgame.util.Constants;
import com.chessgame.model.PlayerColor;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX-based view for the chess game with comprehensive font smoothing
 * Provides modern UI with optimized text rendering across all components
 */
public class JavaFXChessView {
    private static final Logger logger = LoggerFactory.getLogger(JavaFXChessView.class);
    
    private Stage primaryStage;
    private GameController controller;
    private final FontConfig fontConfig;
    private final TextRenderer textRenderer;
    private final GameConfig config;
    
    // UI Components
    private Canvas boardCanvas;
    private GraphicsContext boardGraphicsContext;
    private TextArea moveHistoryArea;
    private Label whitePlayerLabel;
    private Label blackPlayerLabel;
    private Label whiteTimerLabel;
    private Label blackTimerLabel;
    private Label gameStatusLabel;
    private MenuBar menuBar;
    
    // Layout containers
    private BorderPane rootLayout;
    private VBox leftPanel;
    private VBox rightPanel;
    private HBox topPanel;
    private HBox bottomPanel;
    
    public JavaFXChessView() {
        this.fontConfig = FontConfig.getInstance();
        this.textRenderer = new TextRenderer();
        this.config = new GameConfig();
        config.loadConfiguration();
    }
    
    /**
     * Initializes the JavaFX view with the main stage
     */
    public void initialize(Stage stage, GameController controller) {
        this.primaryStage = stage;
        this.controller = controller;
        
        configureStage();
        createLayout();
        createMenus();
        createBoardCanvas();
        createPlayerInfoPanels();
        createMoveHistoryPanel();
        createStatusBar();
        applyFontSmoothing();
        setupEventHandlers();
        
        logger.info("JavaFX view initialized with comprehensive font smoothing");
    }
    
    /**
     * Configures the main application stage
     */
    private void configureStage() {
        primaryStage.setTitle("Modern Chess Game");
        primaryStage.setMinWidth(config.getInt("ui.minWidth", (int) Constants.MIN_WINDOW_WIDTH));
        primaryStage.setMinHeight(config.getInt("ui.minHeight", (int) Constants.MIN_WINDOW_HEIGHT));
        primaryStage.setWidth(config.getInt("ui.preferredWidth", (int) Constants.PREFERRED_WINDOW_WIDTH));
        primaryStage.setHeight(config.getInt("ui.preferredHeight", (int) Constants.PREFERRED_WINDOW_HEIGHT));
        primaryStage.setResizable(true);
        
        // Apply font smoothing CSS
        Scene scene = new Scene(createMainLayout());
        scene.getStylesheets().add(getClass().getResource("/styles/fonts.css").toExternalForm());
        primaryStage.setScene(scene);
    }
    
    /**
     * Creates the main application layout
     */
    private Pane createMainLayout() {
        rootLayout = new BorderPane();
        rootLayout.setStyle("""
            -fx-background-color: #F8F9FA;
            -fx-font-smoothing-type: lcd;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            -fx-font-ligatures: true;
            """);
        
        return rootLayout;
    }
    
    /**
     * Creates the main layout with all panels
     */
    private void createLayout() {
        // Top panel with menu and status
        topPanel = createTopPanel();
        rootLayout.setTop(topPanel);
        
        // Left panel with player information
        leftPanel = createLeftPanel();
        rootLayout.setLeft(leftPanel);
        
        // Center panel with board
        StackPane boardContainer = new StackPane();
        boardContainer.setPadding(new Insets(10));
        rootLayout.setCenter(boardContainer);
        
        // Right panel with move history
        rightPanel = createRightPanel();
        rootLayout.setRight(rightPanel);
        
        // Bottom panel with controls
        bottomPanel = createBottomPanel();
        rootLayout.setBottom(bottomPanel);
    }
    
    /**
     * Creates the top panel with menu bar and game status
     */
    private HBox createTopPanel() {
        HBox topPanel = new HBox();
        topPanel.setStyle("""
            -fx-background-color: #ECF0F1;
            -fx-border-color: #BDC3C7;
            -fx-border-width: 0 0 1 0;
            -fx-padding: 5;
            """);
        topPanel.setAlignment(Pos.CENTER_LEFT);
        topPanel.setSpacing(10);
        
        // Menu bar
        topPanel.getChildren().add(menuBar);
        
        // Spacer
        HBox.setHgrow(menuBar, Priority.ALWAYS);
        
        // Game status label
        gameStatusLabel = new Label("Ready to play");
        gameStatusLabel.getStyleClass().add("game-status");
        gameStatusLabel.setStyle("""
            -fx-font-family: "Georgia", serif;
            -fx-font-size: 18px;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            -fx-letter-spacing: 0.5px;
            """);
        topPanel.getChildren().add(gameStatusLabel);
        
        return topPanel;
    }
    
    /**
     * Creates the left panel with player information
     */
    private VBox createLeftPanel() {
        VBox leftPanel = new VBox();
        leftPanel.setPrefWidth(200);
        leftPanel.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-border-color: #BDC3C7;
            -fx-border-width: 0 1 0 0;
            -fx-padding: 10;
            -fx-spacing: 15;
            """);
        
        // White player section
        VBox whitePlayerBox = createPlayerInfoBox(PlayerColor.WHITE);
        leftPanel.getChildren().add(whitePlayerBox);
        
        // Spacer
        leftPanel.getChildren().add(new Separator());
        
        // Black player section
        VBox blackPlayerBox = createPlayerInfoBox(PlayerColor.BLACK);
        leftPanel.getChildren().add(blackPlayerBox);
        
        return leftPanel;
    }
    
    /**
     * Creates player information box with enhanced fonts
     */
    private VBox createPlayerInfoBox(PlayerColor color) {
        VBox playerBox = new VBox();
        playerBox.setStyle("""
            -fx-background-color: #F8F9FA;
            -fx-border-color: #DEE2E6;
            -fx-border-width: 1;
            -fx-border-radius: 5;
            -fx-padding: 10;
            -fx-spacing: 5;
            """);
        
        // Player name label
        Label playerLabel = new Label(color == PlayerColor.WHITE ? "White Player" : "Black Player");
        playerLabel.getStyleClass().add("player-info");
        playerLabel.setStyle(String.format("""
            -fx-font-family: "Segoe UI", Arial, sans-serif;
            -fx-font-size: 16px;
            -fx-font-weight: %s;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: %s;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            -fx-letter-spacing: 0.0px;
            -fx-line-height: 1.2;
            """,
            color == PlayerColor.WHITE ? "normal" : "normal",
            color == PlayerColor.WHITE ? "#2C3E50" : "#2C3E50"
        ));
        
        // Timer label
        Label timerLabel = new Label("10:00");
        timerLabel.getStyleClass().add("timer-display");
        timerLabel.setStyle(String.format("""
            -fx-font-family: "Consolas", "Monaco", "Courier New", monospace;
            -fx-font-size: 24px;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: %s;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            -fx-letter-spacing: 1.0px;
            -fx-line-height: 1.0;
            -fx-text-alignment: center;
            """,
            color == PlayerColor.WHITE ? "#2C3E50" : "#2C3E50"
        ));
        
        if (color == PlayerColor.WHITE) {
            whitePlayerLabel = playerLabel;
            whiteTimerLabel = timerLabel;
        } else {
            blackPlayerLabel = playerLabel;
            blackTimerLabel = timerLabel;
        }
        
        playerBox.getChildren().addAll(playerLabel, timerLabel);
        return playerBox;
    }
    
    /**
     * Creates the board canvas with coordinate display
     */
    private void createBoardCanvas() {
        boardCanvas = new Canvas(Constants.BOARD_PIXEL_SIZE, Constants.BOARD_PIXEL_SIZE);
        boardGraphicsContext = boardCanvas.getGraphicsContext2D();
        
        // Setup canvas for optimal text rendering
        textRenderer.setupCanvasForText(boardCanvas);
        
        // Apply CSS classes for font smoothing
        boardCanvas.getStyleClass().add("gpu-accelerated");
        
        // Add coordinate overlay
        StackPane boardContainer = new StackPane(boardCanvas);
        boardContainer.setAlignment(Pos.CENTER);
        rootLayout.setCenter(boardContainer);
        
        // Draw initial board
        drawBoard();
    }
    
    /**
     * Creates the right panel with move history
     */
    private VBox createRightPanel() {
        VBox rightPanel = new VBox();
        rightPanel.setPrefWidth(300);
        rightPanel.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-border-color: #BDC3C7;
            -fx-border-width: 0 0 0 1;
            -fx-padding: 10;
            -fx-spacing: 10;
            """);
        
        // Move history label
        Label moveHistoryLabel = new Label("Move History");
        moveHistoryLabel.getStyleClass().add("text-primary");
        moveHistoryLabel.setStyle("""
            -fx-font-family: "Segoe UI", Arial, sans-serif;
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            -fx-letter-spacing: 0.0px;
            -fx-line-height: 1.2;
            """);
        
        // Move history text area
        moveHistoryArea = new TextArea();
        moveHistoryArea.getStyleClass().add("move-notation");
        moveHistoryArea.setStyle("""
            -fx-font-family: "Consolas", "Monaco", "Courier New", monospace;
            -fx-font-size: 14px;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-prompt-text-fill: #7F8C8D;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            -fx-font-ligatures: true;
            -fx-letter-spacing: 0.0px;
            -fx-line-height: 1.4;
            -fx-background-color: #F8F9FA;
            -fx-border-color: #DEE2E6;
            -fx-border-width: 1;
            -fx-border-radius: 3;
            -fx-focus-traversable: false;
            """);
        moveHistoryArea.setEditable(false);
        moveHistoryArea.setPrefRowCount(20);
        moveHistoryArea.setWrapText(true);
        
        rightPanel.getChildren().addAll(moveHistoryLabel, moveHistoryArea);
        return rightPanel;
    }
    
    /**
     * Creates the bottom panel with game controls
     */
    private HBox createBottomPanel() {
        HBox bottomPanel = new HBox();
        bottomPanel.setStyle("""
            -fx-background-color: #ECF0F1;
            -fx-border-color: #BDC3C7;
            -fx-border-width: 1 0 0 0;
            -fx-padding: 10;
            """);
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.setSpacing(10);
        
        // New game button
        Button newGameButton = new Button("New Game");
        newGameButton.getStyleClass().add("button");
        newGameButton.setStyle("""
            -fx-font-family: "Segoe UI", Arial, sans-serif;
            -fx-font-size: 16px;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            -fx-letter-spacing: 0.0px;
            -fx-text-alignment: center;
            -fx-background-color: #FFFFFF;
            -fx-border-color: #BDC3C7;
            -fx-border-width: 1;
            -fx-border-radius: 3;
            -fx-padding: 8 16;
            """);
        newGameButton.setOnAction(e -> controller.newGame());
        
        // Undo button
        Button undoButton = new Button("Undo");
        undoButton.getStyleClass().add("button");
        undoButton.setStyle("""
            -fx-font-family: "Segoe UI", Arial, sans-serif;
            -fx-font-size: 16px;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            -fx-letter-spacing: 0.0px;
            -fx-text-alignment: center;
            -fx-background-color: #FFFFFF;
            -fx-border-color: #BDC3C7;
            -fx-border-width: 1;
            -fx-border-radius: 3;
            -fx-padding: 8 16;
            """);
        undoButton.setOnAction(e -> controller.undoMove());
        
        bottomPanel.getChildren().addAll(newGameButton, undoButton);
        return bottomPanel;
    }
    
    /**
     * Creates the application menu bar
     */
    private void createMenus() {
        menuBar = new MenuBar();
        menuBar.getStyleClass().add("menu-bar");
        menuBar.setStyle("""
            -fx-font-family: "Segoe UI", Arial, sans-serif;
            -fx-font-size: 14px;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            -fx-background-color: #ECF0F1;
            -fx-border-color: #BDC3C7;
            -fx-border-width: 0 0 1 0;
            -fx-padding: 2 5;
            """);
        
        // Game menu
        Menu gameMenu = new Menu("Game");
        gameMenu.getStyleClass().add("menu");
        gameMenu.setStyle("""
            -fx-font-family: "Segoe UI", Arial, sans-serif;
            -fx-font-size: 14px;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            """);
        
        MenuItem newGameItem = new MenuItem("New Game");
        newGameItem.setOnAction(e -> controller.newGame());
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> Platform.exit());
        
        gameMenu.getItems().addAll(newGameItem, new SeparatorMenuItem(), exitItem);
        
        // View menu
        Menu viewMenu = new Menu("View");
        viewMenu.getStyleClass().add("menu");
        viewMenu.setStyle("""
            -fx-font-family: "Segoe UI", Arial, sans-serif;
            -fx-font-size: 14px;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            """);
        
        MenuItem showCoordinatesItem = new MenuItem("Show Coordinates");
        showCoordinatesItem.setOnAction(e -> toggleCoordinates());
        
        viewMenu.getItems().add(showCoordinatesItem);
        
        // Help menu
        Menu helpMenu = new Menu("Help");
        helpMenu.getStyleClass().add("menu");
        helpMenu.setStyle("""
            -fx-font-family: "Segoe UI", Arial, sans-serif;
            -fx-font-size: 14px;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            """);
        
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(aboutItem);
        
        menuBar.getMenus().addAll(gameMenu, viewMenu, helpMenu);
    }
    
    /**
     * Applies comprehensive font smoothing to all components
     */
    private void applyFontSmoothing() {
        // Preload common fonts
        fontConfig.preloadCommonFonts();
        textRenderer.preloadCommonText();
        
        // Apply root font smoothing
        if (primaryStage.getScene() != null) {
            primaryStage.getScene().getRoot().setStyle("""
                -fx-font-smoothing-type: lcd;
                -fx-font-smoothing-lcd: true;
                -fx-font-kerning: true;
                -fx-font-ligatures: true;
                """);
        }
        
        logger.info("Font smoothing applied to all UI components");
    }
    
    /**
     * Sets up event handlers for user interactions
     */
    private void setupEventHandlers() {
        // Board click handling
        boardCanvas.setOnMouseClicked(e -> {
            double x = e.getX();
            double y = e.getY();
            controller.handleBoardClick(x, y);
        });
    }
    
    /**
     * Draws the chess board with coordinates
     */
    private void drawBoard() {
        // Clear canvas
        boardGraphicsContext.clearRect(0, 0, boardCanvas.getWidth(), boardCanvas.getHeight());
        
        // Draw board squares
        for (int row = 0; row < Constants.BOARD_SIZE; row++) {
            for (int col = 0; col < Constants.BOARD_SIZE; col++) {
                double x = col * Constants.SQUARE_SIZE;
                double y = row * Constants.SQUARE_SIZE;
                
                // Determine square color
                Color squareColor = (row + col) % 2 == 0 
                    ? Color.web(Constants.LIGHT_SQUARE_COLOR)
                    : Color.web(Constants.DARK_SQUARE_COLOR);
                
                boardGraphicsContext.setFill(squareColor);
                boardGraphicsContext.fillRect(x, y, Constants.SQUARE_SIZE, Constants.SQUARE_SIZE);
            }
        }
        
        // Draw coordinates with smooth fonts
        textRenderer.renderBoardCoordinates(boardGraphicsContext, boardCanvas);
    }
    
    /**
     * Updates the move history display
     */
    public void updateMoveHistory(String moves) {
        moveHistoryArea.setText(moves);
        moveHistoryArea.setScrollTop(Double.MAX_VALUE);
    }
    
    /**
     * Updates player information display
     */
    public void updatePlayerInfo(PlayerColor color, String name, String status) {
        if (color == PlayerColor.WHITE && whitePlayerLabel != null) {
            whitePlayerLabel.setText(name + " - " + status);
        } else if (color == PlayerColor.BLACK && blackPlayerLabel != null) {
            blackPlayerLabel.setText(name + " - " + status);
        }
    }
    
    /**
     * Updates timer display
     */
    public void updateTimer(PlayerColor color, String timeString, boolean isActive) {
        Label timerLabel = color == PlayerColor.WHITE ? whiteTimerLabel : blackTimerLabel;
        if (timerLabel != null) {
            timerLabel.setText(timeString);
            
            // Apply different styles based on timer state
            String style = timerLabel.getStyle();
            if (isActive) {
                timerLabel.getStyleClass().add("active");
                timerLabel.setStyle(style + """
                    -fx-text-fill: #E74C3C;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 2, 0, 0, 1);
                    """);
            } else {
                timerLabel.getStyleClass().remove("active");
                timerLabel.setStyle(style.replace("-fx-text-fill: #E74C3C;", "-fx-text-fill: #2C3E50;")
                    .replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 2, 0, 0, 1);", ""));
            }
        }
    }
    
    /**
     * Updates the game status display
     */
    public void updateGameStatus(String status) {
        gameStatusLabel.setText(status);
    }
    
    /**
     * Sets the controller for this view
     */
    public void setController(GameController controller) {
        this.controller = controller;
    }
    
    /**
     * Toggles coordinate display
     */
    private void toggleCoordinates() {
        // Implementation for toggling coordinate visibility
        drawBoard();
    }
    
    /**
     * Shows the about dialog
     */
    private void showAboutDialog() {
        Alert aboutDialog = new Alert(Alert.AlertType.INFORMATION);
        aboutDialog.setTitle("About Modern Chess Game");
        aboutDialog.setHeaderText("Modern Chess Game v1.0");
        aboutDialog.setContentText("""
            A modern chess game built with JavaFX.
            
            Features:
            • Comprehensive font smoothing
            • Subpixel anti-aliasing
            • Optimized text rendering
            • Modern UI design
            • Player vs Player gameplay
            
            Developed with best practices in mind.
            """);
        
        // Style the dialog for optimal font rendering
        aboutDialog.getDialogPane().setStyle("""
            -fx-font-family: "Segoe UI", Arial, sans-serif;
            -fx-font-size: 14px;
            -fx-font-smoothing-type: lcd;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            -fx-text-fill: #2C3E50;
            """);
        
        aboutDialog.showAndWait();
    }
}