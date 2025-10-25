package main;

import piece.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

/**
 * GamePanel - основной класс игровой логики шахмат
 * Управляет отрисовкой доски, ходами фигур и игровым процессом
 */
public class GamePanel extends JPanel implements Runnable {
    // Минимальные размеры игрового окна
    public static final int MIN_WIDTH = 850 + Board.MARGIN * 2;   // Минимальная ширина окна с отступами
    public static final int MIN_HEIGHT = 600 + Board.MARGIN * 2;  // Минимальная высота окна с отступами
    final int FPS = 60;                                          // Частота обновления экрана

    // Текущие размеры панели
    private int currentWidth;
    private int currentHeight;

    // Основные компоненты
    Thread gameThread;                      // Поток игрового цикла
    Board board = new Board();             // Шахматная доска
    Mouse mouse = new Mouse();             // Обработчик мыши

    // Управление фигурами
    public static ArrayList<Piece> pieces = new ArrayList<>();     // Список всех фигур на доске
    public static ArrayList<Piece> simPieces = new ArrayList<>();  // Копия фигур для симуляции ходов
    ArrayList<Piece> promoPieces = new ArrayList<>();             // Фигуры для превращения пешки
    public static Piece castlingP;                                // Фигура для рокировки
    Piece activeP,                                               // Выбранная фигура
          checkingP;                                             // Фигура, создающая шах


    // Цвета игроков
    public static final int WHITE = 0;                           // Белые фигуры
    public static final int BLACK = 1;                           // Черные фигуры
    int currentColor = WHITE;                                    // Текущий ход

    // Флаги состояния игры
    boolean canMove;                                             // Можно ли двигать фигуру
    boolean validSquare;                                         // Допустимая клетка для хода
    boolean promotion;                                           // Режим превращения пешки
    boolean gameOver;                                            // Игра окончена
    boolean stalemate;                                          // Патовая ситуация

    public GamePanel() {
        setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);

        // Добавляем слушатель изменения размера
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                currentWidth = getWidth();
                currentHeight = getHeight();
                repaint();
            }
        });

        setPieces();
//        testPromotion();
//        testIllegal();
        copyPieces(pieces, simPieces);
    }

    public void launchGame() {

        gameThread = new Thread(this);
        gameThread.start();

    }

    public void setPieces() {

        // WHITE TEAM
        pieces.add(new  Pawn(0, 6, WHITE));
        pieces.add(new  Pawn(1, 6, WHITE));
        pieces.add(new  Pawn(2, 6, WHITE));
        pieces.add(new  Pawn(3, 6, WHITE));
        pieces.add(new  Pawn(4, 6, WHITE));
        pieces.add(new  Pawn(5, 6, WHITE));
        pieces.add(new  Pawn(6, 6, WHITE));
        pieces.add(new  Pawn(7, 6, WHITE));
        pieces.add(new  Rook(0,7, WHITE));
        pieces.add(new  Rook(7, 7,WHITE));
        pieces.add(new  Knight(1, 7,WHITE));
        pieces.add(new  Knight(6, 7,WHITE));
        pieces.add(new  Bishop(2, 7,WHITE));
        pieces.add(new  Bishop(5, 7,WHITE));
        pieces.add(new  Queen(3, 7,WHITE));
        pieces.add(new  King(4, 7,WHITE));

        // BLACK TEAM
        pieces.add(new Pawn(0, 1,BLACK));
        pieces.add(new Pawn(1, 1,BLACK));
        pieces.add(new Pawn(2, 1,BLACK));
        pieces.add(new Pawn(3, 1,BLACK));
        pieces.add(new Pawn(4, 1,BLACK));
        pieces.add(new Pawn(5, 1,BLACK));
        pieces.add(new Pawn(6, 1,BLACK));
        pieces.add(new Pawn(7, 1,BLACK));
        pieces.add(new Rook(0, 0,BLACK));
        pieces.add(new Rook(7, 0,BLACK));
        pieces.add(new Knight(1, 0,BLACK));
        pieces.add(new Knight(6, 0,BLACK));
        pieces.add(new Bishop(2, 0,BLACK));
        pieces.add(new Bishop(5, 0,BLACK));
        pieces.add(new Queen(3, 0,BLACK));
        pieces.add(new King(4, 0,BLACK));
    }

    //Test
    public void testPromotion() {
        pieces.add(new Pawn(0,4,WHITE));
        pieces.add(new Pawn(5,6,BLACK));
    }

    public void testIllegal(){
        pieces.add(new Queen(2,2,WHITE));
        pieces.add(new King(3,4,WHITE));
        pieces.add(new King(0,3,BLACK));
        pieces.add(new Bishop(0,4,BLACK));
        pieces.add(new Queen(4,4,BLACK));

    }

    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target){
        target.clear();
        for (int i = 0; i < source.size(); i++) {
            target.add(source.get(i));
        }
    }



    @Override
    public void run() {

        //Game loop
        double drawInterval = (double) 1000000000 /FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null){

            currentTime = System.nanoTime();

            delta += (currentTime - lastTime)/drawInterval;
            lastTime = currentTime;

            if (delta >= 1){
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update(){
        if (promotion){
            promotion();
        }else if (!gameOver && !stalemate){
            // Mouse Button Pressed //
            if (mouse.pressed) {
                // Пересчитываем координаты мыши с учетом масштаба
                double scale = Math.min((double) currentWidth / MIN_WIDTH, 
                                      (double) currentHeight / MIN_HEIGHT);
                int scaledMouseX = (int) (mouse.x / scale);
                int scaledMouseY = (int) (mouse.y / scale);

                if (activeP == null) {
                    // If the activeP is Null, check if you can pick up a piece
                    for (Piece piece : simPieces) {
                        // if the mouse is on an all piece, pick it up as the activeP
                        if (piece.color == currentColor &&
                                piece.col == (scaledMouseX - Board.MARGIN) / Board.SQUARE_SIZE &&
                                piece.row == (scaledMouseY - Board.MARGIN) / Board.SQUARE_SIZE) {
                            activeP = piece;
                        }
                    }
                }else {
                    // if the player is holding a piece, simulate the move
                    simulate();
                }

            }
            // Mouse Button released
            if (!mouse.pressed){

                if (activeP != null){

                    if (validSquare){
                        // Move confirmed

                        //Update the piece list in case a piece has been captured and removed during the simulation
                        copyPieces(simPieces, pieces);
                        activeP.updatePosition();
                        if (castlingP != null){
                            castlingP.updatePosition();
                        }

                        if (isKingInCheck() && isCheckMate()){
                            gameOver = true;
                        }else if (isStalemate() && !isKingInCheck()){
                            stalemate = true;
                        }
                        else { // The game is still going on
                            if (canPromote()){
                                promotion = true;
                            }else {
                                changePlayer();
                            }
                        }


                    }else {
                        //The move is not valid so reset everything
                        copyPieces(pieces, simPieces);
                        activeP.resetPosition();
                        activeP = null;
                    }
                }
            }
        }
    }


    private void promotion() {

        if (mouse.pressed){
            for(Piece piece : promoPieces){
                if (piece.col == mouse.x/Board.SQUARE_SIZE && piece.row == mouse.y/Board.SQUARE_SIZE){
                    switch (piece.type){
                        case ROOK: simPieces.add(new Rook(activeP.col, activeP.row, currentColor)); break;
                        case KNIGHT: simPieces.add(new Knight(activeP.col, activeP.row, currentColor)); break;
                        case BISHOP: simPieces.add(new Bishop(activeP.col, activeP.row, currentColor)); break;
                        case QUEEN: simPieces.add(new Queen(activeP.col, activeP.row, currentColor)); break;
                        default: break;
                    }
                    simPieces.remove(activeP.getIndex());
                    copyPieces(simPieces, pieces);
                    activeP = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }

    }

    private void simulate() {

        canMove = false;
        validSquare = false;

        // Reset the piece list in every loop
        // This is basically for restoring the removed piece during the simulation
        copyPieces(pieces, simPieces);

        // Reset the castling piece's position
        if (castlingP != null){
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP = null;
        }

        // Пересчитываем координаты мыши с учетом масштаба
        double scale = Math.min((double) currentWidth / MIN_WIDTH, 
                              (double) currentHeight / MIN_HEIGHT);
        int scaledMouseX = (int) (mouse.x / scale);
        int scaledMouseY = (int) (mouse.y / scale);

        // if a piece is being hold, update its position
        activeP.x = scaledMouseX - Board.HALF_SQUARE_SIZE;
        activeP.y = scaledMouseY - Board.HALF_SQUARE_SIZE;
        activeP.col = activeP.getCol(activeP.x);
        activeP.row = activeP.getRow(activeP.y);

        // Check if the piece is hovering over a reachable square
        if (activeP.canMove(activeP.col, activeP.row)){

            canMove = true;

            // if hitting a piece, remove it from the list
            if(activeP.hittingP != null){
                simPieces.remove(activeP.hittingP.getIndex());
            }

            checkCastling();

            if (!isIllegal(activeP) || !opponentCanCaptureKing()){
                validSquare = true;
            }
        }
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        
        // Получаем текущие размеры панели
        currentWidth = getWidth();
        currentHeight = getHeight();
        
        // Устанавливаем масштабирование
        double scaleX = (double) currentWidth / MIN_WIDTH;
        double scaleY = (double) currentHeight / MIN_HEIGHT;
        double scale = Math.min(scaleX, scaleY);
        
        // Применяем масштабирование
        g2.scale(scale, scale);
        //Board
        board.draw(g2);

        //Piece
        for(Piece p : simPieces){
            p.draw(g2);
        }

        if (activeP != null){
            if (canMove){
                if (isIllegal(activeP) || opponentCanCaptureKing()){
                    g2.setColor(Color.gray);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 0.7f));
                    g2.fillRect(activeP.col*Board.SQUARE_SIZE + Board.MARGIN, 
                              activeP.row*Board.SQUARE_SIZE + Board.MARGIN,
                              Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }else {
                    g2.setColor(Color.white);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 0.7f));
                    g2.fillRect(activeP.col*Board.SQUARE_SIZE + Board.MARGIN, 
                              activeP.row*Board.SQUARE_SIZE + Board.MARGIN,
                              Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
            }
            // Draw the active piece in the end sp it won't be hidde by the board or the colored square
            activeP.draw(g2);
        }
        //Status Messages
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 25));
        g2.setColor(Color.white);

        // Рассчитываем центр области для сообщений
        int messageAreaX = 600; // Начало области сообщений
        int messageAreaWidth = MIN_WIDTH - messageAreaX; // Ширина области сообщений
        
        // Вспомогательная функция для центрирования текста
        FontMetrics fm = g2.getFontMetrics();

        if (promotion){
            String promoText = "Promote to:";
            int textWidth = fm.stringWidth(promoText);
            int x = messageAreaX + (messageAreaWidth - textWidth) / 2;
            g2.drawString(promoText, x, 150);
            for (Piece piece : promoPieces)
                g2.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row),
                        Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
        }else {
            if (currentColor == WHITE){
                String turnText = "White's turn";
                int textWidth = fm.stringWidth(turnText);
                int x = messageAreaX + (messageAreaWidth - textWidth) / 2;
                g2.drawString(turnText, x, 490);
                if (checkingP != null && checkingP.color == BLACK){
                    g2.setColor(Color.red);
                    String kingText = "The King";
                    String checkText = "is in check!";
                    int kingWidth = fm.stringWidth(kingText);
                    int checkWidth = fm.stringWidth(checkText);
                    int kingX = messageAreaX + (messageAreaWidth - kingWidth) / 2;
                    int checkX = messageAreaX + (messageAreaWidth - checkWidth) / 2;
                    g2.drawString(kingText, kingX, 530);
                    g2.drawString(checkText, checkX, 560);
                }
            }else {
                String turnText = "Black's turn";
                int textWidth = fm.stringWidth(turnText);
                int x = messageAreaX + (messageAreaWidth - textWidth) / 2;
                g2.drawString(turnText, x, 120);
                if (checkingP != null && checkingP.color == WHITE) {
                    g2.setColor(Color.red);
                    String kingText = "The King";
                    String checkText = "is in check!";
                    int kingWidth = fm.stringWidth(kingText);
                    int checkWidth = fm.stringWidth(checkText);
                    int kingX = messageAreaX + (messageAreaWidth - kingWidth) / 2;
                    int checkX = messageAreaX + (messageAreaWidth - checkWidth) / 2;
                    g2.drawString(kingText, kingX, 50);
                    g2.drawString(checkText, checkX, 80);
                }
            }
        }

        if (gameOver){

            String s = (currentColor == WHITE) ? "White Wins" : "Black Wins";

            // Draw semi-transparent black rectangle
            g2.setColor(new Color(0, 0, 0, 128)); // 128 is the alpha value for 50% opacity
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw text message on top of the semi-transparent background
            g2.setFont(new Font("Arial", Font.BOLD, 90));
            g2.setColor(Color.green);
            FontMetrics fmBig = g2.getFontMetrics();
            int textWidth = fmBig.stringWidth(s);
            int x = (MIN_WIDTH - textWidth) / 2;
            g2.drawString(s, x, 320);
        }

        if (stalemate){
            // Draw semi-transparent black rectangle
            g2.setColor(new Color(0, 0, 0, 128)); // 128 is the alpha value for 50% opacity
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw text message on top of the semi-transparent background
            g2.setFont(new Font("Arial", Font.BOLD, 90));
            g2.setColor(Color.lightGray);
            FontMetrics fmBig = g2.getFontMetrics();
            String stalemateText = "Stalemate";
            int textWidth = fmBig.stringWidth(stalemateText);
            int x = (MIN_WIDTH - textWidth) / 2;
            g2.drawString(stalemateText, x, 320);
        }
    }

    private void changePlayer(){

        if (currentColor == WHITE){
            currentColor = BLACK;
            //Reset black's two stepped status
            for (Piece piece : pieces){
                if (piece.color == BLACK){
                    piece.twoStepped = false;
                }
            }
        }else {
            currentColor = WHITE;
            //Reset white's two stepped status
            for (Piece piece : pieces){
                if (piece.color == WHITE){
                    piece.twoStepped = false;
                }
            }
        }
        activeP = null;
    }

    private void checkCastling(){

        if (castlingP != null){
            if (castlingP.col == 0) {
                castlingP.col += 3;
            }else if (castlingP.col == 7){
                castlingP.col -= 2;
            }
            castlingP.x = castlingP.getX(castlingP.col);
        }
    }

    private  boolean canPromote(){

        if (activeP.type == Type.PAWN){
            if (currentColor == WHITE && activeP.row == 0 || currentColor == BLACK && activeP.row == 7){
                promoPieces.clear();
                promoPieces.add(new Rook(9,2,currentColor));
                promoPieces.add(new Knight(9,3,currentColor));
                promoPieces.add(new Bishop(9,4,currentColor));
                promoPieces.add(new Queen(9,5,currentColor));
                return true;
            }
        }
        return false;
    }

    private boolean isKingInCheck() {

        Piece king = getKing(true);

        if (activeP.canMove(king.col, king.row)){
            checkingP = activeP;
            return true;
        }else {
            checkingP = null;
        }

        return false;
    }

    private Piece getKing(boolean opponent){

        Piece king = null;

        for (Piece piece : simPieces){
            if (opponent){
                if (piece.type == Type.KING && piece.color != currentColor){
                    king = piece;
                }
            }else {
                if (piece.type == Type.KING && piece.color == currentColor){
                    king = piece;
                }
            }
        }

        return king;
    }

    private boolean isIllegal(Piece king){

        if (king.type == Type.KING){
             for (Piece piece : simPieces){
                 if (piece != king && piece.color != king.color && piece.canMove(king.col, king.row)){
                     return true;
                 }
             }
        }
        return false;
    }

    private boolean opponentCanCaptureKing() {

        Piece king = getKing(false);

        for (Piece piece : simPieces){
            if (piece.color != king.color && piece.canMove(king.col, king.row)){
                return true;
            }
        }

        return false;
    }

    private boolean isCheckMate(){

        Piece king = getKing(true);

        if (kingCanMove(king)) {
            return false;
        } else {
            // The player still had a chance
            // Check if he can block attack with his pieces

            // Check the position of the checking piece and the king in check
            int colDiff = Math.abs(checkingP.col - king.col);
            int rowDiff = Math.abs(checkingP.row - king.row);

            if (colDiff == 0) {
                // The checking piece is attacking vertically
                if (checkingP.row < king.row) {
                    // The checking piece is above the king
                    for (int row = checkingP.row; row < king.row; row++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor &&
                                    piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }

                if (checkingP.row > king.row) {
                    // The checking piece is below the king
                    for (int row = checkingP.row; row > king.row; row--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor &&
                                    piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }

            } else if (rowDiff == 0) {
                // The checking piece is attacking horizontally
                if (checkingP.col < king.col) {
                    // The checking piece is to the left
                    for (int col = checkingP.col; col < king.row; col++) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor &&
                                    piece.canMove(col, checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }

                if (checkingP.col > king.col) {
                    // The checking piece is to the right
                    for (int col = checkingP.col; col > king.row; col--) {
                        for (Piece piece : simPieces) {
                            if (piece != king && piece.color != currentColor &&
                                    piece.canMove(col, checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }
            } else if (colDiff == rowDiff) {
                // The checking piece is attacking diagonally
                if (checkingP.row < king.row) {
                    // The checking piece is above the king
                    if (checkingP.col < king.col) {
                        // The checking piece is in the upper left
                        for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row++) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor &&
                                        piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }

                    if (checkingP.col > king.col) {
                        // The checking piece is in the upper right
                        for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor &&
                                        piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }
                }

                if (checkingP.row > king.row) {
                    // The checking piece is below the king
                    if (checkingP.col < king.col) {
                        // The checking piece is in the lower left
                        for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row--) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor &&
                                        piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }

                    if (checkingP.col > king.col) {
                        // The checking piece is in the lower right
                        for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row--) {
                            for (Piece piece : simPieces) {
                                if (piece != king && piece.color != currentColor &&
                                        piece.canMove(col, row)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            } else {
                // The checking place is knight
            }
        }

        return true;

    }

    private boolean kingCanMove(Piece king){

        // Simulate if there is a square where the king can move
        if (isValidMove(king, -1, -1)) {return true;}
        if (isValidMove(king, 0, -1)) {return true;}
        if (isValidMove(king, 1, -1)) {return true;}
        if (isValidMove(king, -1, 0)) {return true;}
        if (isValidMove(king, 1, 0)) {return true;}
        if (isValidMove(king, -1, 1)) {return true;}
        if (isValidMove(king, 0, 1)) {return true;}
        if (isValidMove(king, 1, 1)) {return true;}

        return false;
    }

    private boolean isValidMove(Piece king, int colPlus, int rowPlus){

        boolean isValidMove = false;

        // Update the temporary King position
        king.col += colPlus;
        king.row += rowPlus;

        if (king.canMove(king.col, king.row)) {
            if (king.hittingP != null) {
                simPieces.remove(king.hittingP.getIndex());
            }

            if (isIllegal(king) == false) {
                isValidMove = true;
            }
        }

        // Reset the temporary King position
        king.resetPosition();
        copyPieces(pieces, simPieces);

        return isValidMove;
    }

    private boolean isStalemate(){
        int count = 0;
        // Count the number of piece
        for (Piece piece : simPieces){
            if (piece.color != currentColor){
                count++;
            }
        }
        // If only one piece (the King) is left
        if (count == 1) return !kingCanMove(getKing(true));

        return false;
    }
}
