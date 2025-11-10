package com.chessgame.view;

import com.chessgame.util.FontConfig;
import com.chessgame.util.Constants;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Optimized text rendering system with subpixel anti-aliasing
 * Provides high-quality text rendering for all chess game UI elements
 */
public final class TextRenderer {
    private final Map<String, Text> textCache;
    private final FontConfig fontConfig;
    private final boolean enableSubpixel;
    private final boolean enableKerning;
    private final boolean enableLigatures;
    
    public TextRenderer() {
        this.fontConfig = FontConfig.getInstance();
        this.textCache = new ConcurrentHashMap<>();
        this.enableSubpixel = Constants.FONT_SUBPIXEL_ENABLED;
        this.enableKerning = Constants.FONT_KERNING_ENABLED;
        this.enableLigatures = Constants.FONT_LIGATURES_ENABLED;
    }
    
    /**
     * Renders text with optimal subpixel anti-aliasing
     */
    public void renderText(GraphicsContext gc, String text, double x, double y, 
                          Font font, Color color, TextAlignment alignment) {
        if (text == null || text.isEmpty()) return;
        
        // Set up optimal rendering context
        gc.setFont(font);
        gc.setFill(color);
        gc.setTextAlign(alignment);
        
        // Enable subpixel anti-aliasing for better text quality
        if (enableSubpixel) {
            gc.setTextAlign(TextAlignment.LEFT); // Reset alignment for subpixel
            gc.setFontSmoothingType(javafx.scene.canvas.Canvas.SMOOTH_TEXT_SMOOTHING_LCD);
        }
        
        // Render the text
        gc.fillText(text, x, y);
    }
    
    /**
     * Renders text with shadow for better visibility
     */
    public void renderTextWithShadow(GraphicsContext gc, String text, double x, double y,
                                   Font font, Color textColor, Color shadowColor, 
                                   double shadowOffset, TextAlignment alignment) {
        if (text == null || text.isEmpty()) return;
        
        // Draw shadow
        gc.setFont(font);
        gc.setFill(shadowColor);
        gc.setTextAlign(alignment);
        gc.fillText(text, x + shadowOffset, y + shadowOffset);
        
        // Draw main text
        gc.setFill(textColor);
        gc.fillText(text, x, y);
    }
    
    /**
     * Renders board coordinates (file letters and rank numbers)
     */
    public void renderBoardCoordinates(GraphicsContext gc, Canvas boardCanvas) {
        double width = boardCanvas.getWidth();
        double height = boardCanvas.getHeight();
        double squareSize = Constants.SQUARE_SIZE;
        
        // Font for coordinates
        Font coordFont = fontConfig.getCoordinateFont();
        Color coordColor = Color.web(Constants.TEXT_COLOR_TERTIARY);
        
        // Render file letters (a-h) at bottom
        for (int file = 0; file < Constants.BOARD_SIZE; file++) {
            String letter = String.valueOf(Constants.FILE_NOTATION[file]);
            double x = file * squareSize + squareSize / 2;
            double y = height - 5;
            
            // Subpixel alignment for crisp text
            double alignedX = Math.round(x) + 0.5;
            renderText(gc, letter, alignedX, y, coordFont, coordColor, TextAlignment.CENTER);
        }
        
        // Render rank numbers (1-8) on left
        for (int rank = 0; rank < Constants.BOARD_SIZE; rank++) {
            String number = String.valueOf(Constants.RANK_NOTATION[rank]);
            double x = 5;
            double y = (Constants.BOARD_SIZE - rank - 1) * squareSize + squareSize / 2 + 5;
            
            // Subpixel alignment for crisp text
            double alignedY = Math.round(y) + 0.5;
            renderText(gc, number, x, alignedY, coordFont, coordColor, TextAlignment.LEFT);
        }
    }
    
    /**
     * Renders move notation with monospace font
     */
    public void renderMoveNotation(GraphicsContext gc, String notation, double x, double y, 
                                 double maxWidth, Color color) {
        if (notation == null || notation.isEmpty()) return;
        
        Font monoFont = fontConfig.getMoveNotationFont();
        
        // Use text wrapping for long notation
        Text textNode = getCachedText(notation, monoFont, maxWidth);
        textNode.setFill(color);
        
        // Position and render
        gc.save();
        gc.translate(x, y);
        textNode.applyCss();
        
        // Render each line
        for (javafx.scene.Node line : textNode.getChildrenUnmodifiable()) {
            if (line instanceof Text) {
                Text textLine = (Text) line;
                gc.setFont(textLine.getFont());
                gc.setFill(textLine.getFill());
                gc.fillText(textLine.getText(), textLine.getLayoutX(), textLine.getLayoutY());
            }
        }
        gc.restore();
    }
    
    /**
     * Renders player information with enhanced typography
     */
    public void renderPlayerInfo(GraphicsContext gc, String name, String status, 
                               double x, double y, Color nameColor, Color statusColor) {
        Font nameFont = fontConfig.getPlayerInfoFont();
        Font statusFont = fontConfig.getLabelFont();
        
        // Render player name
        renderText(gc, name, x, y, nameFont, nameColor, TextAlignment.LEFT);
        
        // Render player status (below name)
        renderText(gc, status, x, y + 20, statusFont, statusColor, TextAlignment.LEFT);
    }
    
    /**
     * Renders timer with digital display styling
     */
    public void renderTimer(GraphicsContext gc, String timeString, double x, double y, 
                          Color timeColor, boolean isActive) {
        Font timerFont = fontConfig.getDisplayFont(Constants.FONT_SIZE_XLARGE);
        
        // Use shadow for active timer to draw attention
        if (isActive) {
            Color shadowColor = Color.web(Constants.TEXT_COLOR_TERTIARY).deriveColor(0, 0, 0, 0.3);
            renderTextWithShadow(gc, timeString, x, y, timerFont, timeColor, shadowColor, 2, TextAlignment.CENTER);
        } else {
            renderText(gc, timeString, x, y, timerFont, timeColor, TextAlignment.CENTER);
        }
    }
    
    /**
     * Renders game status messages
     */
    public void renderGameStatus(GraphicsContext gc, String status, double canvasWidth, double canvasHeight) {
        Font statusFont = fontConfig.getTitleFont();
        Color statusColor = Color.web(Constants.TEXT_COLOR_PRIMARY);
        
        // Center the status message
        double x = canvasWidth / 2;
        double y = canvasHeight / 2;
        
        // Add semi-transparent background
        gc.setFill(Color.web("#000000").deriveColor(0, 0, 0, 0.7));
        gc.fillRect(x - 200, y - 50, 400, 100);
        
        // Render status with white text
        renderText(gc, status, x, y + 10, statusFont, Color.WHITE, TextAlignment.CENTER);
    }
    
    /**
     * Optimizes text rendering performance by caching Text nodes
     */
    private Text getCachedText(String content, Font font, double maxWidth) {
        String cacheKey = content + "-" + font.getName() + "-" + font.getSize() + "-" + maxWidth;
        return textCache.computeIfAbsent(cacheKey, k -> {
            Text text = new Text(content);
            text.setFont(font);
            text.setWrappingWidth(maxWidth);
            text.setLineSpacing(2);
            text.setTextAlignment(TextAlignment.LEFT);
            return text;
        });
    }
    
    /**
     * Clears the text cache to free memory
     */
    public void clearCache() {
        textCache.clear();
    }
    
    /**
     * Gets cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("cacheSize", textCache.size());
        stats.put("subpixelEnabled", enableSubpixel);
        stats.put("kerningEnabled", enableKerning);
        stats.put("ligaturesEnabled", enableLigatures);
        return stats;
    }
    
    /**
     * Pre-renders common text for better performance
     */
    public void preloadCommonText() {
        // Preload common move notations
        String[] commonMoves = {"1. e4", "1... c5", "Nf3", "d6", "d4", "cxd4", "Nxd4"};
        Font monoFont = fontConfig.getMoveNotationFont();
        
        for (String move : commonMoves) {
            getCachedText(move, monoFont, 100);
        }
        
        // Preload common status messages
        String[] statusMessages = {"Check", "Checkmate", "Stalemate", "Draw", "White's turn", "Black's turn"};
        Font statusFont = fontConfig.getLabelFont();
        
        for (String status : statusMessages) {
            getCachedText(status, statusFont, 150);
        }
    }
    
    /**
     * Sets up the canvas for optimal text rendering
     */
    public void setupCanvasForText(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Enable subpixel anti-aliasing
        gc.setImageSmoothing(true);
        gc.setImageSmoothingQuality(javafx.scene.image.ImageQuality.HIGH);
        
        // Set text rendering hints
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(javafx.geometry.VPos.BASELINE);
    }
    
    /**
     * Gets optimal text color for different contexts
     */
    public Color getOptimalTextColor(String context) {
        switch (context.toLowerCase()) {
            case "primary":
                return Color.web(Constants.TEXT_COLOR_PRIMARY);
            case "secondary":
                return Color.web(Constants.TEXT_COLOR_SECONDARY);
            case "tertiary":
                return Color.web(Constants.TEXT_COLOR_TERTIARY);
            case "inverse":
                return Color.web(Constants.TEXT_COLOR_INVERSE);
            case "success":
                return Color.web(Constants.TEXT_COLOR_SUCCESS);
            case "warning":
                return Color.web(Constants.TEXT_COLOR_WARNING);
            case "error":
                return Color.web(Constants.TEXT_COLOR_ERROR);
            default:
                return Color.web(Constants.TEXT_COLOR_PRIMARY);
        }
    }
    
    @Override
    public String toString() {
        return String.format("TextRenderer{cacheSize=%d, subpixel=%s, kerning=%s, ligatures=%s}", 
            textCache.size(), enableSubpixel, enableKerning, enableLigatures);
    }
}