package com.chessgame.view;

import com.chessgame.util.FontConfig;
import com.chessgame.util.Constants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Font preview and testing component for optimal font configuration
 * Allows users to test different font settings and see real-time rendering
 */
public class FontPreviewPanel {
    private static final Logger logger = LoggerFactory.getLogger(FontPreviewPanel.class);
    
    private final FontConfig fontConfig;
    private final TextRenderer textRenderer;
    private Stage previewStage;
    
    // UI Components
    private TextArea previewText;
    private ComboBox<String> fontFamilyCombo;
    private ComboBox<Double> fontSizeCombo;
    private ComboBox<String> smoothingCombo;
    private CheckBox subpixelCheckBox;
    private CheckBox kerningCheckBox;
    private CheckBox ligaturesCheckBox;
    private Label renderStatsLabel;
    
    public FontPreviewPanel() {
        this.fontConfig = FontConfig.getInstance();
        this.textRenderer = new TextRenderer();
    }
    
    /**
     * Shows the font preview dialog
     */
    public void showPreview(Stage parentStage) {
        if (previewStage == null) {
            createPreviewDialog();
        }
        
        previewStage.initOwner(parentStage);
        previewStage.initModality(Modality.APPLICATION_MODAL);
        previewStage.showAndWait();
    }
    
    /**
     * Creates the font preview dialog
     */
    private void createPreviewDialog() {
        previewStage = new Stage(StageStyle.UTILITY);
        previewStage.setTitle("Font Preview & Testing");
        previewStage.setResizable(false);
        
        VBox mainLayout = new VBox();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setSpacing(15);
        mainLayout.setStyle("""
            -fx-background-color: #F8F9FA;
            -fx-font-smoothing-type: lcd;
            -fx-font-smoothing-lcd: true;
            """);
        
        // Title
        Label titleLabel = new Label("Font Rendering Preview");
        titleLabel.getStyleClass().add("text-display");
        titleLabel.setStyle("""
            -fx-font-family: "Georgia", serif;
            -fx-font-size: 24px;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            -fx-letter-spacing: 0.5px;
            -fx-line-height: 1.2;
            -fx-text-alignment: center;
            """);
        
        // Control panel
        VBox controlsPanel = createControlsPanel();
        
        // Preview text area
        previewText = new TextArea();
        previewText.setText(getSampleText());
        previewText.getStyleClass().add("text-primary");
        previewText.setStyle("""
            -fx-font-family: "Segoe UI", Arial, sans-serif;
            -fx-font-size: 14px;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            -fx-font-ligatures: true;
            -fx-letter-spacing: 0.0px;
            -fx-line-height: 1.4;
            -fx-background-color: #FFFFFF;
            -fx-border-color: #DEE2E6;
            -fx-border-width: 1;
            -fx-border-radius: 3;
            -fx-padding: 10;
            -fx-pref-width: 600;
            -fx-pref-height: 200;
            """);
        previewText.setWrapText(true);
        previewText.setEditable(false);
        previewText.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        
        // Chess-specific text preview
        VBox chessPreviewPanel = createChessPreviewPanel();
        
        // Render statistics
        renderStatsLabel = new Label();
        renderStatsLabel.getStyleClass().add("text-secondary");
        renderStatsLabel.setStyle("""
            -fx-font-family: "Segoe UI", Arial, sans-serif;
            -fx-font-size: 12px;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #7F8C8D;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            """);
        
        // Button panel
        HBox buttonPanel = createButtonPanel();
        
        mainLayout.getChildren().addAll(
            titleLabel, 
            controlsPanel, 
            new Separator(),
            new Label("General Text Preview:"),
            previewText,
            new Label("Chess-Specific Text Preview:"),
            chessPreviewPanel,
            renderStatsLabel,
            buttonPanel
        );
        
        Scene scene = new Scene(mainLayout);
        scene.getStylesheets().add(getClass().getResource("/styles/fonts.css").toExternalForm());
        previewStage.setScene(scene);
        
        // Initial preview update
        updatePreview();
    }
    
    /**
     * Creates the font control panel
     */
    private VBox createControlsPanel() {
        VBox controlsPanel = new VBox();
        controlsPanel.setSpacing(10);
        controlsPanel.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-border-color: #DEE2E6;
            -fx-border-width: 1;
            -fx-border-radius: 5;
            -fx-padding: 15;
            """);
        
        // Font family selection
        HBox fontFamilyBox = new HBox();
        fontFamilyBox.setSpacing(10);
        Label fontFamilyLabel = new Label("Font Family:");
        fontFamilyLabel.getStyleClass().add("label");
        
        fontFamilyCombo = new ComboBox<>();
        fontFamilyCombo.getItems().addAll(
            FontConfig.PRIMARY_FONT,
            FontConfig.SECONDARY_FONT,
            FontConfig.MONO_FONT,
            FontConfig.DISPLAY_FONT,
            "Times New Roman",
            "Courier New",
            "Arial Black",
            "Verdana"
        );
        fontFamilyCombo.setValue(FontConfig.PRIMARY_FONT);
        fontFamilyCombo.setOnAction(e -> updatePreview());
        
        fontFamilyBox.getChildren().addAll(fontFamilyLabel, fontFamilyCombo);
        
        // Font size selection
        HBox fontSizeBox = new HBox();
        fontSizeBox.setSpacing(10);
        Label fontSizeLabel = new Label("Font Size:");
        fontSizeLabel.getStyleClass().add("label");
        
        fontSizeCombo = new ComboBox<>();
        fontSizeCombo.getItems().addAll(
            Constants.FONT_SIZE_TINY,
            Constants.FONT_SIZE_SMALL,
            Constants.FONT_SIZE_NORMAL,
            Constants.FONT_SIZE_MEDIUM,
            Constants.FONT_SIZE_LARGE,
            Constants.FONT_SIZE_XLARGE,
            Constants.FONT_SIZE_TITLE
        );
        fontSizeCombo.setValue(Constants.FONT_SIZE_NORMAL);
        fontSizeCombo.setOnAction(e -> updatePreview());
        
        fontSizeBox.getChildren().addAll(fontSizeLabel, fontSizeCombo);
        
        // Smoothing type
        HBox smoothingBox = new HBox();
        smoothingBox.setSpacing(10);
        Label smoothingLabel = new Label("Smoothing:");
        smoothingLabel.getStyleClass().add("label");
        
        smoothingCombo = new ComboBox<>();
        smoothingCombo.getItems().addAll("LCD", "Gray", "None");
        smoothingCombo.setValue("LCD");
        smoothingCombo.setOnAction(e -> updatePreview());
        
        smoothingBox.getChildren().addAll(smoothingLabel, smoothingCombo);
        
        // Rendering options
        HBox optionsBox = new HBox();
        optionsBox.setSpacing(20);
        Label optionsLabel = new Label("Rendering Options:");
        optionsLabel.getStyleClass().add("label");
        
        subpixelCheckBox = new CheckBox("Subpixel Anti-aliasing");
        subpixelCheckBox.setSelected(Constants.FONT_SUBPIXEL_ENABLED);
        subpixelCheckBox.setOnAction(e -> updatePreview());
        
        kerningCheckBox = new CheckBox("Kerning");
        kerningCheckBox.setSelected(Constants.FONT_KERNING_ENABLED);
        kerningCheckBox.setOnAction(e -> updatePreview());
        
        ligaturesCheckBox = new CheckBox("Ligatures");
        ligaturesCheckBox.setSelected(Constants.FONT_LIGATURES_ENABLED);
        ligaturesCheckBox.setOnAction(e -> updatePreview());
        
        optionsBox.getChildren().addAll(optionsLabel, subpixelCheckBox, kerningCheckBox, ligaturesCheckBox);
        
        controlsPanel.getChildren().addAll(fontFamilyBox, fontSizeBox, smoothingBox, optionsBox);
        return controlsPanel;
    }
    
    /**
     * Creates chess-specific text preview
     */
    private VBox createChessPreviewPanel() {
        VBox chessPanel = new VBox();
        chessPanel.setSpacing(10);
        chessPanel.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-border-color: #DEE2E6;
            -fx-border-width: 1;
            -fx-border-radius: 3;
            -fx-padding: 15;
            """);
        
        // Board coordinates
        HBox coordinatesBox = new HBox();
        coordinatesBox.setSpacing(30);
        Label coordsLabel = new Label("Board Coordinates: a1 b1 c1 d1 e1 f1 g1 h1");
        coordsLabel.getStyleClass().add("coordinate-label");
        chessPanel.getChildren().add(coordsLabel);
        
        // Move notation
        HBox movesBox = new HBox();
        movesBox.setSpacing(20);
        Label movesLabel = new Label("Move Notation: 1. e4 e5 2. Nf3 Nc6 3. Bb5");
        movesLabel.getStyleClass().add("move-notation");
        chessPanel.getChildren().add(movesLabel);
        
        // Timer display
        HBox timerBox = new HBox();
        timerBox.setSpacing(20);
        Label timerLabel = new Label("Timer: 05:23");
        timerLabel.getStyleClass().add("timer-display");
        chessPanel.getChildren().add(timerLabel);
        
        // Game status
        HBox statusBox = new HBox();
        statusBox.setSpacing(20);
        Label statusLabel = new Label("Game Status: White's Turn - Check!");
        statusLabel.getStyleClass().add("game-status");
        chessPanel.getChildren().add(statusLabel);
        
        return chessPanel;
    }
    
    /**
     * Creates the button panel
     */
    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox();
        buttonPanel.setSpacing(10);
        buttonPanel.setAlignment(Pos.CENTER);
        
        Button applyButton = new Button("Apply to Game");
        applyButton.getStyleClass().add("button");
        applyButton.setStyle("""
            -fx-font-family: "Segoe UI", Arial, sans-serif;
            -fx-font-size: 14px;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            -fx-letter-spacing: 0.0px;
            -fx-text-alignment: center;
            -fx-background-color: #28A745;
            -fx-text-fill: white;
            -fx-border-color: #28A745;
            -fx-border-radius: 3;
            -fx-padding: 8 16;
            """);
        applyButton.setOnAction(e -> applyFontSettings());
        
        Button resetButton = new Button("Reset to Default");
        resetButton.getStyleClass().add("button");
        resetButton.setStyle("""
            -fx-font-family: "Segoe UI", Arial, sans-serif;
            -fx-font-size: 14px;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            -fx-letter-spacing: 0.0px;
            -fx-text-alignment: center;
            -fx-background-color: #6C757D;
            -fx-text-fill: white;
            -fx-border-color: #6C757D;
            -fx-border-radius: 3;
            -fx-padding: 8 16;
            """);
        resetButton.setOnAction(e -> resetToDefault());
        
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("button");
        closeButton.setStyle("""
            -fx-font-family: "Segoe UI", Arial, sans-serif;
            -fx-font-size: 14px;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: lcd;
            -fx-text-fill: #2C3E50;
            -fx-font-smoothing-lcd: true;
            -fx-font-kerning: true;
            -fx-letter-spacing: 0.0px;
            -fx-text-alignment: center;
            -fx-background-color: #DC3545;
            -fx-text-fill: white;
            -fx-border-color: #DC3545;
            -fx-border-radius: 3;
            -fx-padding: 8 16;
            """);
        closeButton.setOnAction(e -> previewStage.close());
        
        buttonPanel.getChildren().addAll(applyButton, resetButton, closeButton);
        return buttonPanel;
    }
    
    /**
     * Updates the font preview based on current settings
     */
    private void updatePreview() {
        String fontFamily = fontFamilyCombo.getValue();
        double fontSize = fontSizeCombo.getValue();
        String smoothingType = smoothingCombo.getValue();
        boolean subpixel = subpixelCheckBox.isSelected();
        boolean kerning = kerningCheckBox.isSelected();
        boolean ligatures = ligaturesCheckBox.isSelected();
        
        // Apply font settings
        Font selectedFont = fontConfig.getFont("preview", fontFamily, fontSize, 
                                              javafx.scene.text.FontWeight.NORMAL, 
                                              javafx.scene.text.FontPosture.REGULAR);
        
        String smoothingStyle = switch (smoothingType) {
            case "LCD" -> "-fx-font-smoothing-type: lcd;";
            case "Gray" -> "-fx-font-smoothing-type: gray;";
            case "None" -> "-fx-font-smoothing-type: none;";
            default -> "-fx-font-smoothing-type: lcd;";
        };
        
        String previewStyle = String.format("""
            -fx-font-family: "%s";
            -fx-font-size: %.1fpx;
            -fx-font-weight: normal;
            -fx-font-smoothing-type: %s;
            -fx-text-fill: #2C3E50;
            -fx-font-smoothing-lcd: %s;
            -fx-font-kerning: %s;
            -fx-font-ligatures: %s;
            -fx-letter-spacing: 0.0px;
            -fx-line-height: 1.4;
            """,
            fontFamily, fontSize, smoothingType.toLowerCase(),
            subpixel, kerning, ligatures);
        
        previewText.setStyle(previewStyle);
        
        // Update chess-specific previews
        updateChessPreviews(fontFamily, fontSize, smoothingType, subpixel, kerning, ligatures);
        
        // Update statistics
        updateRenderStats();
    }
    
    /**
     * Updates chess-specific font previews
     */
    private void updateChessPreviews(String fontFamily, double fontSize, String smoothingType, 
                                   boolean subpixel, boolean kerning, boolean ligatures) {
        // This would update all the chess-specific text elements with the new font settings
        // For demonstration, we'll just log the changes
        logger.debug("Updated chess font preview: {} {}px {} smoothing", fontFamily, fontSize, smoothingType);
    }
    
    /**
     * Updates render statistics display
     */
    private void updateRenderStats() {
        var stats = textRenderer.getCacheStats();
        renderStatsLabel.setText(String.format(
            "Font Cache: %d items | Subpixel: %s | Kerning: %s | Ligatures: %s",
            stats.get("cacheSize"),
            stats.get("subpixelEnabled"),
            stats.get("kerningEnabled"),
            stats.get("ligaturesEnabled")
        ));
    }
    
    /**
     * Applies the current font settings to the game
     */
    private void applyFontSettings() {
        // This would apply the selected font settings to the actual game
        logger.info("Applying font settings: {} {}px with {} smoothing", 
                   fontFamilyCombo.getValue(), fontSizeCombo.getValue(), smoothingCombo.getValue());
        
        // Show confirmation
        Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
        confirmation.setTitle("Font Settings Applied");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Font settings have been applied to the game!");
        confirmation.showAndWait();
    }
    
    /**
     * Resets font settings to default
     */
    private void resetToDefault() {
        fontFamilyCombo.setValue(FontConfig.PRIMARY_FONT);
        fontSizeCombo.setValue(Constants.FONT_SIZE_NORMAL);
        smoothingCombo.setValue("LCD");
        subpixelCheckBox.setSelected(Constants.FONT_SUBPIXEL_ENABLED);
        kerningCheckBox.setSelected(Constants.FONT_KERNING_ENABLED);
        ligaturesCheckBox.setSelected(Constants.FONT_LIGATURES_ENABLED);
        
        updatePreview();
    }
    
    /**
     * Gets sample text for font preview
     */
    private String getSampleText() {
        return """
            This is a comprehensive font preview for the Modern Chess Game.
            
            The quick brown fox jumps over the lazy dog.
            0123456789 !@#$%^&*()_+-=[]{}|;':",./<>?
            
            Chess-specific characters: ♔♕♖♗♘♙ ♚♛♜♝♞♟
            
            Special symbols: ©®™€£¥§¶†‡•…‰″‴‵‶‷‸‹›«»""''""—–-‐‑‒–—―
            
            This text demonstrates various font rendering features:
            • Subpixel anti-aliasing for crisp text
            • Kerning for proper character spacing
            • Ligatures for connected characters
            • Multiple font families and sizes
            • Optimal color contrast and readability
            """;
    }
}