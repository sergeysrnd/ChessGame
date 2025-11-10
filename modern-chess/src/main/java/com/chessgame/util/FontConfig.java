package com.chessgame.util;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Font configuration and management system for the chess game
 * Provides optimized font loading, caching, and rendering settings
 */
public final class FontConfig {
    private static FontConfig instance;
    private final Map<String, Font> fontCache;
    private final FontSmoothingSettings smoothingSettings;
    
    // Default font families
    public static final String PRIMARY_FONT = "Segoe UI";
    public static final String SECONDARY_FONT = "Arial";
    public static final String MONO_FONT = "Consolas";
    public static final String DISPLAY_FONT = "Georgia";
    
    // Font sizes
    public static final double SIZE_TINY = 10.0;
    public static final double SIZE_SMALL = 12.0;
    public static final double SIZE_NORMAL = 14.0;
    public static final double SIZE_MEDIUM = 16.0;
    public static final double SIZE_LARGE = 18.0;
    public static final double SIZE_XLARGE = 24.0;
    public static final double SIZE_TITLE = 32.0;
    
    private FontConfig() {
        this.fontCache = new ConcurrentHashMap<>();
        this.smoothingSettings = new FontSmoothingSettings();
        initializeDefaultFonts();
    }
    
    /**
     * Gets the singleton instance
     */
    public static synchronized FontConfig getInstance() {
        if (instance == null) {
            instance = new FontConfig();
        }
        return instance;
    }
    
    /**
     * Initializes default fonts with optimal settings
     */
    private void initializeDefaultFonts() {
        // Cache commonly used fonts
        loadFont("primary-normal", PRIMARY_FONT, SIZE_NORMAL, FontWeight.NORMAL, FontPosture.REGULAR);
        loadFont("primary-medium", PRIMARY_FONT, SIZE_MEDIUM, FontWeight.NORMAL, FontPosture.REGULAR);
        loadFont("primary-large", PRIMARY_FONT, SIZE_LARGE, FontWeight.NORMAL, FontPosture.REGULAR);
        loadFont("primary-bold", PRIMARY_FONT, SIZE_NORMAL, FontWeight.BOLD, FontPosture.REGULAR);
        loadFont("mono-normal", MONO_FONT, SIZE_NORMAL, FontWeight.NORMAL, FontPosture.REGULAR);
        loadFont("mono-small", MONO_FONT, SIZE_SMALL, FontWeight.NORMAL, FontPosture.REGULAR);
        loadFont("display-normal", DISPLAY_FONT, SIZE_NORMAL, FontWeight.NORMAL, FontPosture.REGULAR);
        loadFont("display-title", DISPLAY_FONT, SIZE_TITLE, FontWeight.NORMAL, FontPosture.REGULAR);
    }
    
    /**
     * Loads and caches a font
     */
    public Font loadFont(String key, String family, double size, FontWeight weight, FontPosture posture) {
        String cacheKey = generateCacheKey(key, family, size, weight, posture);
        return fontCache.computeIfAbsent(cacheKey, k -> Font.font(family, weight, posture, size));
    }
    
    /**
     * Gets a cached font or loads it if not exists
     */
    public Font getFont(String key, String family, double size, FontWeight weight, FontPosture posture) {
        String cacheKey = generateCacheKey(key, family, size, weight, posture);
        return fontCache.computeIfAbsent(cacheKey, k -> Font.font(family, weight, posture, size));
    }
    
    /**
     * Gets a primary font for UI elements
     */
    public Font getPrimaryFont(double size) {
        return getFont("primary", PRIMARY_FONT, size, FontWeight.NORMAL, FontPosture.REGULAR);
    }
    
    /**
     * Gets a primary bold font
     */
    public Font getPrimaryBoldFont(double size) {
        return getFont("primary-bold", PRIMARY_FONT, size, FontWeight.BOLD, FontPosture.REGULAR);
    }
    
    /**
     * Gets a monospace font for move notation
     */
    public Font getMonoFont(double size) {
        return getFont("mono", MONO_FONT, size, FontWeight.NORMAL, FontPosture.REGULAR);
    }
    
    /**
     * Gets a display font for titles and special text
     */
    public Font getDisplayFont(double size) {
        return getFont("display", DISPLAY_FONT, size, FontWeight.NORMAL, FontPosture.REGULAR);
    }
    
    /**
     * Gets board coordinate font
     */
    public Font getCoordinateFont() {
        return getPrimaryFont(SIZE_SMALL);
    }
    
    /**
     * Gets move notation font
     */
    public Font getMoveNotationFont() {
        return getMonoFont(SIZE_NORMAL);
    }
    
    /**
     * Gets player info font
     */
    public Font getPlayerInfoFont() {
        return getPrimaryFont(SIZE_MEDIUM);
    }
    
    /**
     * Gets menu font
     */
    public Font getMenuFont() {
        return getPrimaryFont(SIZE_NORMAL);
    }
    
    /**
     * Gets title font
     */
    public Font getTitleFont() {
        return getDisplayFont(SIZE_TITLE);
    }
    
    /**
     * Gets button font
     */
    public Font getButtonFont() {
        return getPrimaryFont(SIZE_MEDIUM);
    }
    
    /**
     * Gets label font
     */
    public Font getLabelFont() {
        return getPrimaryFont(SIZE_NORMAL);
    }
    
    /**
     * Gets text field font
     */
    public Font getTextFieldFont() {
        return getPrimaryFont(SIZE_NORMAL);
    }
    
    /**
     * Preloads common fonts for better performance
     */
    public void preloadCommonFonts() {
        double[] sizes = {SIZE_SMALL, SIZE_NORMAL, SIZE_MEDIUM, SIZE_LARGE};
        
        for (double size : sizes) {
            getPrimaryFont(size);
            getPrimaryBoldFont(size);
            getMonoFont(size);
        }
    }
    
    /**
     * Clears the font cache
     */
    public void clearCache() {
        fontCache.clear();
        initializeDefaultFonts();
    }
    
    /**
     * Gets the number of cached fonts
     */
    public int getCacheSize() {
        return fontCache.size();
    }
    
    /**
     * Gets font smoothing settings
     */
    public FontSmoothingSettings getSmoothingSettings() {
        return smoothingSettings;
    }
    
    private String generateCacheKey(String key, String family, double size, FontWeight weight, FontPosture posture) {
        return String.format("%s-%s-%.1f-%s-%s", 
            key, family, size, 
            weight != null ? weight.name() : "NORMAL",
            posture != null ? posture.name() : "REGULAR");
    }
    
    /**
     * Font smoothing settings for optimal text rendering
     */
    public static class FontSmoothingSettings {
        // Anti-aliasing settings
        public static final String TEXT_ANTIALIASING_LCD = "-fx-text-fill: #000000; -fx-font-smoothing-type: lcd;";
        public static final String TEXT_ANTIALIASING_GRAY = "-fx-text-fill: #000000; -fx-font-smoothing-type: gray;";
        public static final String TEXT_ANTIALIASING_NONE = "-fx-text-fill: #000000; -fx-font-smoothing-type: none;";
        
        // Hint settings for subpixel rendering
        public static final String RENDERING_HINT_DEFAULT = "-fx-font-smoothing-type: lcd;";
        public static final String RENDERING_HINT_SUBPIXEL = "-fx-font-smoothing-type: lcd; -fx-font-smoothing-lcd: true;";
        public static final String RENDERING_HINT_CRISP = "-fx-font-smoothing-type: gray;";
        
        // Color settings for different text types
        public static final String PRIMARY_TEXT_STYLE = """
            -fx-font-family: '%s';
            -fx-font-size: %.1fpx;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-fill: #2C3E50;
            """;
        
        public static final String SECONDARY_TEXT_STYLE = """
            -fx-font-family: '%s';
            -fx-font-size: %.1fpx;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #7F8C8D;
            -fx-fill: #7F8C8D;
            """;
        
        public static final String MONO_TEXT_STYLE = """
            -fx-font-family: '%s';
            -fx-font-size: %.1fpx;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-fill: #2C3E50;
            -fx-font-kerning: true;
            """;
        
        public static final String DISPLAY_TEXT_STYLE = """
            -fx-font-family: '%s';
            -fx-font-size: %.1fpx;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-fill: #2C3E50;
            -fx-font-weight: normal;
            """;
        
        private final boolean enableSubpixel;
        private final boolean enableKerning;
        private final boolean enableLigatures;
        private final String smoothingType;
        
        public FontSmoothingSettings() {
            this(true, true, true, "lcd");
        }
        
        public FontSmoothingSettings(boolean enableSubpixel, boolean enableKerning, 
                                   boolean enableLigatures, String smoothingType) {
            this.enableSubpixel = enableSubpixel;
            this.enableKerning = enableKerning;
            this.enableLigatures = enableLigatures;
            this.smoothingType = smoothingType;
        }
        
        public boolean isEnableSubpixel() { return enableSubpixel; }
        public boolean isEnableKerning() { return enableKerning; }
        public boolean isEnableLigatures() { return enableLigatures; }
        public String getSmoothingType() { return smoothingType; }
        
        /**
         * Gets CSS style for primary text with optimal smoothing
         */
        public String getPrimaryTextCSS(double fontSize) {
            return String.format(PRIMARY_TEXT_STYLE, FontConfig.PRIMARY_FONT, fontSize);
        }
        
        /**
         * Gets CSS style for secondary text
         */
        public String getSecondaryTextCSS(double fontSize) {
            return String.format(SECONDARY_TEXT_STYLE, FontConfig.PRIMARY_FONT, fontSize);
        }
        
        /**
         * Gets CSS style for monospace text
         */
        public String getMonoTextCSS(double fontSize) {
            return String.format(MONO_TEXT_STYLE, FontConfig.MONO_FONT, fontSize);
        }
        
        /**
         * Gets CSS style for display text
         */
        public String getDisplayTextCSS(double fontSize) {
            return String.format(DISPLAY_TEXT_STYLE, FontConfig.DISPLAY_FONT, fontSize);
        }
    }
    
    @Override
    public String toString() {
        return String.format("FontConfig{cacheSize=%d, smoothing=%s}", 
            fontCache.size(), smoothingSettings.getSmoothingType());
    }
}