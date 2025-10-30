package main;

import javax.swing.*;

/**
 * Main класс - точка входа в шахматную игру
 * Инициализирует графический интерфейс и запускает игровой процесс
 */
public class Main {

// TODO: Раскомментировать после добавления иконки
//    static ImageIcon logo = new ImageIcon(Main.class.getClassLoader().
//            getResource("res/chess.png"));

    public static void main(String[] args) {
        // Создаем главное окно игры
        JFrame window = new JFrame("Chess Game");
        
        // Настраиваем параметры окна
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Закрытие окна завершает программу
        window.setResizable(true);                             // Разрешаем изменение размера окна
        window.setMinimumSize(new java.awt.Dimension(850, 600)); // Минимальный размер окна
//      window.getIconImage(logo.getImage());                  // TODO: Добавить иконку окна

        // Показываем диалог выбора режима игры
        GameModeSelector modeSelector = new GameModeSelector(window);
        modeSelector.setVisible(true);
        
        int gameMode = modeSelector.getSelectedMode();
        int difficulty = modeSelector.getSelectedDifficulty();
        
        // Если пользователь закрыл диалог без выбора, используем режим PvP по умолчанию
        if (gameMode == -1) {
            gameMode = 0;
        }
        
        // Создаем и добавляем игровую панель
        GamePanel gp = new GamePanel(gameMode, difficulty);    // Создаем панель с выбранным режимом
        window.add(gp);                                        // Добавляем панель в окно
        window.pack();                                         // Устанавливаем оптимальный размер окна

        // Настраиваем отображение окна
        window.setLocationRelativeTo(null);                    // Центрируем окно на экране
        window.setVisible(true);                               // Делаем окно видимым

        // Запускаем игровой процесс
        gp.launchGame();                                       // Запускаем игровой цикл
    }
}