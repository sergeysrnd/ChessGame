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

    /**
     * Draws the board onto the supplied {@link Graphics2D} context.
     *
     * The method iterates over each row and column, alternating the
     * background colour to create a checker‑pattern.  Two colours are used:
     * a light brown (227,171,117) and a darker brown (201,138,18).
     */
    public void draw(Graphics2D g2){
        // Toggle flag – 0 for light square, 1 for dark square
        int c = 0;

        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {

                // Choose colour based on the toggle flag
                if (c == 0) {
                    g2.setColor(new Color(227, 171, 117));
                    c = 1;
                } else {
                    g2.setColor(new Color(201, 138, 18));
                    c = 0;
                }

                // Draw the square at its calculated pixel position
                g2.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE,
                            SQUARE_SIZE, SQUARE_SIZE);
            }

            // After finishing a row, flip the starting colour so that
            // the pattern continues correctly on the next line.
            c = (c == 0) ? 1 : 0;
        }
    }
}
