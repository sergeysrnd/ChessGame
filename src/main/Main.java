package main;

import javax.swing.*;

/**
 * Main класс - точка входа в шахматную игру
 * Автоматически запускает PvP игру (Человек vs Человек) без диалога выбора режима
 */
public class Main {

    public static void main(String[] args) {
        // Создаем главное окно игры
        JFrame window = new JFrame("Chess Game - PvP Only");
        
        // Настраиваем параметры окна
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Закрытие окна завершает программу
        window.setResizable(true);                             // Разрешаем изменение размера окна
        window.setMinimumSize(new java.awt.Dimension(850, 600)); // Минимальный размер окна

        // Создаем и добавляем игровую панель (только PvP режим)
        GamePanel gp = new GamePanel();    // Создаем панель для PvP режима с стандартной начальной позицией
        window.add(gp);                                        // Добавляем панель в окно
        window.pack();                                         // Устанавливаем оптимальный размер окна

        // Настраиваем отображение окна
        window.setLocationRelativeTo(null);                    // Центрируем окно на экране
        window.setVisible(true);                               // Делаем окно видимым

        // Запускаем игровой процесс
        gp.launchGame();                                       // Запускаем игровой цикл
    }
}