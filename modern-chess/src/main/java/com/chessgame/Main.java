package com.chessgame;

import com.chessgame.config.GameConfig;
import com.chessgame.view.JavaFXChessView;
import com.chessgame.controller.GameController;
import com.chessgame.model.ChessBoard;
import com.chessgame.factory.ChessPieceFactory;
import com.chessgame.observer.GameEventManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for the modern chess game
 */
public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting Modern Chess Game Application");
            
            // Initialize game configuration
            GameConfig config = new GameConfig();
            config.loadConfiguration();
            
            // Create core game components
            ChessBoard board = new ChessBoard();
            GameEventManager eventManager = GameEventManager.getInstance();
            ChessPieceFactory pieceFactory = new ChessPieceFactory(); // Will be used by board
            
            // Create MVC components
            JavaFXChessView view = new JavaFXChessView();
            GameController controller = new GameController(board, view, eventManager);
            
            // Initialize the view
            view.initialize(primaryStage, controller);
            
            // Connect view to controller
            view.setController(controller);
            
            // Start the game
            controller.startNewGame();
            
            // Show the stage
            primaryStage.show();
            
            logger.info("Chess game application started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start chess game application", e);
            System.exit(1);
        }
    }
    
    @Override
    public void stop() {
        logger.info("Shutting down chess game application");
        GameEventManager.getInstance().clearListeners();
    }
    
    public static void main(String[] args) {
        logger.info("Launching Modern Chess Game");
        launch(args);
    }
}