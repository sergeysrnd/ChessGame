package com.chessgame.config;

import com.chessgame.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Configuration manager for the chess game
 */
public final class GameConfig {
    private static final Logger logger = LoggerFactory.getLogger(GameConfig.class);
    
    private final Map<String, String> configMap;
    private boolean loaded = false;
    
    public GameConfig() {
        this.configMap = new ConcurrentHashMap<>();
        loadDefaultConfiguration();
    }
    
    /**
     * Loads default configuration values
     */
    private void loadDefaultConfiguration() {
        // Board settings
        configMap.put("board.size", String.valueOf(Constants.BOARD_SIZE));
        configMap.put("square.size", String.valueOf(Constants.SQUARE_SIZE));
        
        // Colors
        configMap.put("color.lightSquare", Constants.LIGHT_SQUARE_COLOR);
        configMap.put("color.darkSquare", Constants.DARK_SQUARE_COLOR);
        configMap.put("color.highlight", Constants.HIGHLIGHT_COLOR);
        configMap.put("color.selected", Constants.SELECTED_COLOR);
        configMap.put("color.validMove", Constants.VALID_MOVE_COLOR);
        configMap.put("color.check", Constants.CHECK_COLOR);
        
        // Animation
        configMap.put("animation.moveDuration", String.valueOf(Constants.MOVE_ANIMATION_DURATION));
        configMap.put("animation.captureDuration", String.valueOf(Constants.CAPTURE_ANIMATION_DURATION));
        configMap.put("animation.hoverScale", String.valueOf(Constants.HOVER_SCALE_FACTOR));
        
        // Game timer
        configMap.put("timer.defaultTime", String.valueOf(Constants.DEFAULT_GAME_TIME_MINUTES));
        configMap.put("timer.increment", String.valueOf(Constants.INCREMENT_SECONDS));
        
        // UI
        configMap.put("ui.minWidth", String.valueOf(Constants.MIN_WINDOW_WIDTH));
        configMap.put("ui.minHeight", String.valueOf(Constants.MIN_WINDOW_HEIGHT));
        configMap.put("ui.preferredWidth", String.valueOf(Constants.PREFERRED_WINDOW_WIDTH));
        configMap.put("ui.preferredHeight", String.valueOf(Constants.PREFERRED_WINDOW_HEIGHT));
        
        // Theme
        configMap.put("theme.default", Constants.DEFAULT_THEME);
        configMap.put("theme.available", String.join(",", Constants.AVAILABLE_THEMES));
        
        // Logging
        configMap.put("logging.pattern", Constants.LOG_PATTERN);
        
        logger.debug("Default configuration loaded");
    }
    
    /**
     * Loads configuration from properties file
     */
    public void loadConfiguration() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                
                props.forEach((key, value) -> {
                    configMap.put(key.toString(), value.toString());
                });
                
                loaded = true;
                logger.info("Configuration loaded from config.properties");
            } else {
                logger.info("No config.properties found, using default configuration");
                loaded = true;
            }
        } catch (IOException e) {
            logger.warn("Failed to load configuration file, using defaults", e);
            loaded = true;
        }
    }
    
    /**
     * Gets a configuration value as string
     */
    public String getString(String key) {
        return configMap.get(key);
    }
    
    /**
     * Gets a configuration value as string with default
     */
    public String getString(String key, String defaultValue) {
        return configMap.getOrDefault(key, defaultValue);
    }
    
    /**
     * Gets a configuration value as integer
     */
    public int getInt(String key) {
        return Integer.parseInt(configMap.getOrDefault(key, "0"));
    }
    
    /**
     * Gets a configuration value as integer with default
     */
    public int getInt(String key, int defaultValue) {
        String value = configMap.get(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for key: {}, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Gets a configuration value as double
     */
    public double getDouble(String key) {
        return Double.parseDouble(configMap.getOrDefault(key, "0.0"));
    }
    
    /**
     * Gets a configuration value as double with default
     */
    public double getDouble(String key, double defaultValue) {
        String value = configMap.get(key);
        try {
            return value != null ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Invalid double value for key: {}, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Gets a configuration value as boolean
     */
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(configMap.getOrDefault(key, "false"));
    }
    
    /**
     * Gets a configuration value as boolean with default
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = configMap.get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    /**
     * Sets a configuration value
     */
    public void setConfig(String key, String value) {
        configMap.put(key, value);
    }
    
    /**
     * Gets all configuration as map
     */
    public Map<String, String> getAllConfig() {
        return new ConcurrentHashMap<>(configMap);
    }
    
    /**
     * Checks if configuration was loaded
     */
    public boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Validates the configuration
     */
    public boolean validate() {
        boolean valid = true;
        
        // Check essential board settings
        if (getInt("board.size") != Constants.BOARD_SIZE) {
            logger.error("Invalid board size configuration");
            valid = false;
        }
        
        // Check color values (basic hex validation)
        String[] colors = {"color.lightSquare", "color.darkSquare", "color.highlight"};
        for (String colorKey : colors) {
            String color = getString(colorKey);
            if (color != null && !color.matches("^#[0-9A-Fa-f]{6}$")) {
                logger.error("Invalid color format for: {}", colorKey);
                valid = false;
            }
        }
        
        logger.info("Configuration validation: {}", valid ? "PASSED" : "FAILED");
        return valid;
    }
    
    @Override
    public String toString() {
        return "GameConfig{loaded=" + loaded + ", keys=" + configMap.size() + "}";
    }
}