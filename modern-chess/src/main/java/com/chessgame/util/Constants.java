package com.chessgame.util;

/**
 * Application constants and configuration values
 */
public final class Constants {
    private Constants() {
        // Prevent instantiation
    }
    
    // Board dimensions
    public static final int BOARD_SIZE = 8;
    public static final int SQUARE_SIZE = 80;
    public static final int BOARD_PIXEL_SIZE = BOARD_SIZE * SQUARE_SIZE;
    
    // Colors
    public static final String LIGHT_SQUARE_COLOR = "#F0D9B5";
    public static final String DARK_SQUARE_COLOR = "#B58863";
    public static final String HIGHLIGHT_COLOR = "#FFFF00";
    public static final String SELECTED_COLOR = "#00FFFF";
    public static final String VALID_MOVE_COLOR = "#90EE90";
    public static final String CHECK_COLOR = "#FF6B6B";
    
    // Animation
    public static final double MOVE_ANIMATION_DURATION = 0.3; // seconds
    public static final double CAPTURE_ANIMATION_DURATION = 0.5;
    public static final double HOVER_SCALE_FACTOR = 1.1;
    
    // Game timer
    public static final int DEFAULT_GAME_TIME_MINUTES = 10;
    public static final int INCREMENT_SECONDS = 0;
    
    // File and rank notation
    public static final char[] FILE_NOTATION = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
    public static final char[] RANK_NOTATION = {'1', '2', '3', '4', '5', '6', '7', '8'};
    
    // Piece values for evaluation
    public static final int PAWN_VALUE = 100;
    public static final int KNIGHT_VALUE = 320;
    public static final int BISHOP_VALUE = 330;
    public static final int ROOK_VALUE = 500;
    public static final int QUEEN_VALUE = 900;
    public static final int KING_VALUE = 20000;
    
    // Castling rights
    public static final boolean CAN_CASTLE_KINGSIDE = true;
    public static final boolean CAN_CASTLE_QUEENSIDE = true;
    
    // Move validation
    public static final int MAX_MOVES_IN_GAME = 1000; // 50-move rule threshold
    public static final int DRAW_MOVE_COUNT = 50;
    
    // UI
    public static final double MIN_WINDOW_WIDTH = 1200.0;
    public static final double MIN_WINDOW_HEIGHT = 800.0;
    public static final double PREFERRED_WINDOW_WIDTH = 1400.0;
    public static final double PREFERRED_WINDOW_HEIGHT = 900.0;
    
    // Themes
    public static final String DEFAULT_THEME = "classic";
    public static final String[] AVAILABLE_THEMES = {"classic", "dark", "blue", "green"};
    
    // Logging
    public static final String LOG_PATTERN = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";
    
    // Font Constants
    public static final String PRIMARY_FONT = "Segoe UI";
    public static final String SECONDARY_FONT = "Arial";
    public static final String MONO_FONT = "Consolas";
    public static final String DISPLAY_FONT = "Georgia";
    
    // Font Sizes
    public static final double FONT_SIZE_TINY = 10.0;
    public static final double FONT_SIZE_SMALL = 12.0;
    public static final double FONT_SIZE_NORMAL = 14.0;
    public static final double FONT_SIZE_MEDIUM = 16.0;
    public static final double FONT_SIZE_LARGE = 18.0;
    public static final double FONT_SIZE_XLARGE = 24.0;
    public static final double FONT_SIZE_TITLE = 32.0;
    
    // Font Rendering Settings
    public static final String FONT_SMOOTHING_LCD = "lcd";
    public static final String FONT_SMOOTHING_GRAY = "gray";
    public static final String FONT_SMOOTHING_NONE = "none";
    public static final boolean FONT_SUBPIXEL_ENABLED = true;
    public static final boolean FONT_KERNING_ENABLED = true;
    public static final boolean FONT_LIGATURES_ENABLED = true;
    
    // Text Color Constants
    public static final String TEXT_COLOR_PRIMARY = "#2C3E50";
    public static final String TEXT_COLOR_SECONDARY = "#7F8C8D";
    public static final String TEXT_COLOR_TERTIARY = "#95A5A6";
    public static final String TEXT_COLOR_INVERSE = "#ECF0F1";
    public static final String TEXT_COLOR_SUCCESS = "#27AE60";
    public static final String TEXT_COLOR_WARNING = "#F39C12";
    public static final String TEXT_COLOR_ERROR = "#E74C3C";
}