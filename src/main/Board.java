package main;

import java.awt.*;

/**
 * Board - класс, отвечающий за отрисовку шахматной доски
 * Создает классическую шахматную доску 8x8 клеток
 * с чередующимися светлыми и темными квадратами
 */
public class Board {

    // Board dimensions – an 8x8 grid like a chess board
    final int MAX_COL = 8;   // number of columns
    final int MAX_ROW = 8;   // number of rows

    public static final int SQUARE_SIZE = 75;
    public static final int HALF_SQUARE_SIZE = SQUARE_SIZE/2;
    
    // Отступы для координат
    public static final int MARGIN = 30;  // Отступ для координат
    
    // Массивы для координат
    private static final String[] FILES = {"a", "b", "c", "d", "e", "f", "g", "h"};
    private static final String[] RANKS = {"8", "7", "6", "5", "4", "3", "2", "1"};

    public void draw(Graphics2D g2) {
        // Очищаем фон
        g2.setColor(new Color(240, 240, 240));  // Светло-серый цвет для фона
        g2.fillRect(0, 0, (MAX_COL * SQUARE_SIZE) + (MARGIN * 2), 
                         (MAX_ROW * SQUARE_SIZE) + (MARGIN * 2));

        // Toggle flag – 0 for light square, 1 for dark square
        int c = 0;

        // Отрисовка квадратов доски (смещенной на MARGIN)
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                // Choose colour based on the toggle flag
                if (c == 0) {
                    g2.setColor(new Color(240, 217, 181));  // Светло-коричневый цвет
                    c = 1;
                } else {
                    g2.setColor(new Color(181, 136, 99));   // Темно-коричневый цвет
                    c = 0;
                }

                // Draw the square at its calculated pixel position with margin
                g2.fillRect(col * SQUARE_SIZE + MARGIN, 
                           row * SQUARE_SIZE + MARGIN,
                           SQUARE_SIZE, SQUARE_SIZE);
            }
            // After finishing a row, flip the starting colour
            c = (c == 0) ? 1 : 0;
        }

        // Настройка шрифта для координат
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);

        // Отрисовка букв (a-h) внизу доски
        for (int col = 0; col < MAX_COL; col++) {
            // Координаты для букв
            int x = col * SQUARE_SIZE + MARGIN + (SQUARE_SIZE / 2) - 5;
            int y = (MAX_ROW * SQUARE_SIZE + MARGIN) + (MARGIN * 2/3);
            g2.drawString(FILES[col], x, y);
        }

        // Отрисовка цифр (8-1) слева от доски
        for (int row = 0; row < MAX_ROW; row++) {
            // Координаты для цифр
            int x = MARGIN/3;
            int y = row * SQUARE_SIZE + MARGIN + (SQUARE_SIZE / 2) + 5;
            g2.drawString(RANKS[row], x, y);
        }
    }
}