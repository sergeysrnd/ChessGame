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
        window.setResizable(false);                            // Запрещаем изменение размера окна
//      window.getIconImage(logo.getImage());                  // TODO: Добавить иконку окна

        // Создаем и добавляем игровую панель
        GamePanel gp = new GamePanel();                        // Создаем панель с игровой логикой
        window.add(gp);                                        // Добавляем панель в окно
        window.pack();                                         // Устанавливаем оптимальный размер окна

        // Настраиваем отображение окна
        window.setLocationRelativeTo(null);                    // Центрируем окно на экране
        window.setVisible(true);                               // Делаем окно видимым

        // Запускаем игровой процесс
        gp.launchGame();                                       // Запускаем игровой цикл
    }
}