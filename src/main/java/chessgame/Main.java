package chessgame;

import chessgame.ui.ChessBoard;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Главный класс приложения шахматной игры.
 * Точка входа в программу с инициализацией JavaFX и созданием игрового интерфейса.
 */
public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private ChessBoard chessBoard;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Создаем главное окно шахматной доски
            chessBoard = new ChessBoard();
            Stage stage = chessBoard.createAndShowGUI();

            // Настраиваем главное окно
            primaryStage.setTitle("Шахматная игра - Bagatur AI");
            primaryStage.setResizable(false);
            primaryStage.setOnCloseRequest(event -> {
                logger.info("Закрытие приложения");
                System.exit(0);
            });


        } catch (Exception e) {
            logger.error("Ошибка при запуске приложения", e);
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        logger.info("Приложение завершает работу");
        if (chessBoard != null) {
            chessBoard.cleanup();
        }
    }

    /**
     * Главный метод для запуска приложения.
     * Поддерживает запуск с параметрами командной строки.
     */
    public static void main(String[] args) {
        logger.info("Запуск шахматной игры с AI Bagatur");

        // Проверяем версию Java
        String javaVersion = System.getProperty("java.version");
        logger.info("Версия Java: " + javaVersion);

        // Парсим аргументы командной строки (опционально)
        boolean playerIsWhite = true; // По умолчанию игрок играет белыми
        if (args.length > 0) {
            if (args[0].equals("--black") || args[0].equals("-b")) {
                playerIsWhite = false;
            } else if (args[0].equals("--white") || args[0].equals("-w")) {
                playerIsWhite = true;
            }
        }

        // Запускаем JavaFX приложение
        launch(args);
    }

    /**
     * Получает объект шахматной доски для взаимодействия с игровым интерфейсом.
     */
    public ChessBoard getChessBoard() {
        return chessBoard;
    }
}