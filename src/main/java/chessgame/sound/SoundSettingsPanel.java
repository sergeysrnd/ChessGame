package chessgame.sound;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Панель управления звуками в пользовательском интерфейсе.
 * Предоставляет удобный интерфейс для выбора наборов звуков, управления громкостью и включения/отключения звука.
 */
public class SoundSettingsPanel extends VBox {
    
    private static final Logger logger = LoggerFactory.getLogger(SoundSettingsPanel.class);
    
    private SoundManager soundManager;
    private ComboBox<String> soundSetCombo;
    private Slider masterVolumeSlider;
    private ToggleButton soundToggleButton;
    private Label currentSetLabel;
    private Label volumeLabel;
    private VBox eventVolumesBox;
    private List<SoundManagerListener> listeners;
    
    /**
     * Конструктор панели управления звуками.
     * 
     * @param soundManager менеджер звуков
     */
    public SoundSettingsPanel(SoundManager soundManager) {
        this.soundManager = soundManager;
        this.listeners = new java.util.concurrent.CopyOnWriteArrayList<>();
        
        initializeComponents();
        updateSoundSets();
        
        logger.info("SoundSettingsPanel инициализирована");
    }
    
    /**
     * Инициализирует компоненты панели.
     */
    private void initializeComponents() {
        // Заголовок
        Text title = new Text("Управление звуками");
        title.setFont(new Font("Arial", 14));
        title.setStyle("-fx-font-weight: bold;");
        
        // Выбор набора звуков
        Text soundSetLabel = new Text("Набор звуков:");
        soundSetLabel.setFont(new Font("Arial", 12));
        
        soundSetCombo = new ComboBox<>();
        soundSetCombo.setPrefWidth(200);
        soundSetCombo.setOnAction(e -> handleSoundSetChange());
        
        currentSetLabel = new Label("Текущий: " + soundManager.getCurrentSoundSet());
        currentSetLabel.setFont(new Font("Arial", 10));
        currentSetLabel.setStyle("-fx-text-fill: #666;");
        
        HBox soundSetBox = new HBox(10);
        soundSetBox.setAlignment(Pos.CENTER_LEFT);
        soundSetBox.getChildren().addAll(soundSetLabel, soundSetCombo, currentSetLabel);
        
        // Главная громкость
        Text masterVolumeLabel = new Text("Главная громкость:");
        masterVolumeLabel.setFont(new Font("Arial", 12));
        
        masterVolumeSlider = new Slider(0, 1, soundManager.getMasterVolume());
        masterVolumeSlider.setShowTickLabels(true);
        masterVolumeSlider.setShowTickMarks(true);
        masterVolumeSlider.setMajorTickUnit(0.1);
        masterVolumeSlider.setMinorTickCount(1);
        masterVolumeSlider.setPrefWidth(200);
        masterVolumeSlider.setOnMouseReleased(e -> handleMasterVolumeChange());
        
        volumeLabel = new Label(String.format("%.0f%%", soundManager.getMasterVolume() * 100));
        volumeLabel.setFont(new Font("Arial", 10));
        volumeLabel.setStyle("-fx-text-fill: #666;");
        volumeLabel.setPrefWidth(40);
        
        masterVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            volumeLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
        });
        
        HBox masterVolumeBox = new HBox(10);
        masterVolumeBox.setAlignment(Pos.CENTER_LEFT);
        masterVolumeBox.getChildren().addAll(masterVolumeLabel, masterVolumeSlider, volumeLabel);
        
        // Включение/отключение звука
        soundToggleButton = new ToggleButton(soundManager.isSoundEnabled() ? "Звук включен" : "Звук отключен");
        soundToggleButton.setSelected(soundManager.isSoundEnabled());
        soundToggleButton.setPrefWidth(150);
        soundToggleButton.setStyle("-fx-font-size: 11px;");
        soundToggleButton.setOnAction(e -> handleSoundToggle());
        
        HBox toggleBox = new HBox(10);
        toggleBox.setAlignment(Pos.CENTER_LEFT);
        toggleBox.getChildren().add(soundToggleButton);
        
        // Громкость для отдельных событий
        Text eventVolumesLabel = new Text("Громкость событий:");
        eventVolumesLabel.setFont(new Font("Arial", 12));
        eventVolumesLabel.setStyle("-fx-font-weight: bold;");
        
        eventVolumesBox = new VBox(8);
        eventVolumesBox.setPadding(new Insets(10, 0, 0, 20));
        createEventVolumeControls();
        
        // Кнопка сброса
        Button resetButton = new Button("Сбросить на умолчание");
        resetButton.setPrefWidth(150);
        resetButton.setStyle("-fx-font-size: 11px;");
        resetButton.setOnAction(e -> handleReset());
        
        HBox resetBox = new HBox(10);
        resetBox.setAlignment(Pos.CENTER_LEFT);
        resetBox.getChildren().add(resetButton);
        
        // Информация о кэше
        Label cacheInfoLabel = new Label("Кэш медиа: " + soundManager.getCacheInfo().get("cacheSize") + 
                                         "/" + soundManager.getCacheInfo().get("maxCacheSize"));
        cacheInfoLabel.setFont(new Font("Arial", 9));
        cacheInfoLabel.setStyle("-fx-text-fill: #999;");
        
        // Добавляем все компоненты
        this.setSpacing(12);
        this.setPadding(new Insets(15));
        this.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: #f9f9f9;");
        this.getChildren().addAll(
            title,
            new Separator(),
            soundSetBox,
            masterVolumeBox,
            toggleBox,
            eventVolumesLabel,
            eventVolumesBox,
            new Separator(),
            resetBox,
            cacheInfoLabel
        );
    }
    
    /**
     * Создает элементы управления громкостью для отдельных событий.
     */
    private void createEventVolumeControls() {
        String[] eventTypes = {"move", "capture", "check", "checkmate"};
        String[] eventLabels = {"Ход", "Взятие", "Шах", "Мат"};
        
        for (int i = 0; i < eventTypes.length; i++) {
            String eventType = eventTypes[i];
            String eventLabel = eventLabels[i];
            
            Text label = new Text(eventLabel + ":");
            label.setFont(new Font("Arial", 10));
            
            Slider slider = new Slider(0, 1, soundManager.getEventVolume(eventType));
            slider.setShowTickLabels(false);
            slider.setShowTickMarks(false);
            slider.setPrefWidth(150);
            slider.setStyle("-fx-control-inner-background: #e0e0e0;");
            
            Label percentLabel = new Label(String.format("%.0f%%", soundManager.getEventVolume(eventType) * 100));
            percentLabel.setFont(new Font("Arial", 9));
            percentLabel.setPrefWidth(35);
            
            slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                percentLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
                soundManager.setEventVolume(eventType, (float) newVal.doubleValue());
            });
            
            HBox eventBox = new HBox(8);
            eventBox.setAlignment(Pos.CENTER_LEFT);
            eventBox.getChildren().addAll(label, slider, percentLabel);
            
            eventVolumesBox.getChildren().add(eventBox);
        }
    }
    
    /**
     * Обновляет список доступных наборов звуков.
     */
    public void updateSoundSets() {
        List<String> soundSets = soundManager.getAvailableSoundSets();
        soundSetCombo.getItems().clear();
        soundSetCombo.getItems().addAll(soundSets);
        soundSetCombo.setValue(soundManager.getCurrentSoundSet());
        logger.info("Список наборов звуков обновлен: {} наборов", soundSets.size());
    }
    
    /**
     * Обрабатывает изменение набора звуков.
     */
    private void handleSoundSetChange() {
        String selectedSet = soundSetCombo.getValue();
        if (selectedSet != null && !selectedSet.equals(soundManager.getCurrentSoundSet())) {
            if (soundManager.setCurrentSoundSet(selectedSet)) {
                currentSetLabel.setText("Текущий: " + selectedSet);
                logger.info("Набор звуков изменен на: {}", selectedSet);
            } else {
                soundSetCombo.setValue(soundManager.getCurrentSoundSet());
                showError("Ошибка", "Не удалось установить набор звуков: " + selectedSet);
            }
        }
    }
    
    /**
     * Обрабатывает изменение главной громкости.
     */
    private void handleMasterVolumeChange() {
        float volume = (float) masterVolumeSlider.getValue();
        soundManager.setMasterVolume(volume);
        logger.info("Главная громкость изменена на: {}", volume);
    }
    
    /**
     * Обрабатывает включение/отключение звука.
     */
    private void handleSoundToggle() {
        boolean enabled = soundToggleButton.isSelected();
        soundManager.setSoundEnabled(enabled);
        soundToggleButton.setText(enabled ? "Звук включен" : "Звук отключен");
        logger.info("Звуки {}", enabled ? "включены" : "отключены");
    }
    
    /**
     * Обрабатывает сброс настроек на умолчание.
     */
    private void handleReset() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Сбросить настройки звука?");
        alert.setContentText("Все настройки звука будут восстановлены на значения по умолчанию.");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            soundManager.getPreferences().reset();
            soundManager.getPreferences().save();
            
            // Обновляем UI
            masterVolumeSlider.setValue(soundManager.getMasterVolume());
            soundToggleButton.setSelected(soundManager.isSoundEnabled());
            soundToggleButton.setText(soundManager.isSoundEnabled() ? "Звук включен" : "Звук отключен");
            
            // Обновляем громкость событий
            eventVolumesBox.getChildren().clear();
            createEventVolumeControls();
            
            logger.info("Настройки звука сброшены на умолчание");
        }
    }
    
    /**
     * Показывает диалог ошибки.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Добавляет слушателя событий.
     */
    public void addListener(SoundManagerListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Удаляет слушателя событий.
     */
    public void removeListener(SoundManagerListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Интерфейс для слушателей событий.
     */
    public interface SoundManagerListener {
        void onSoundSettingsChanged(String setting, Object value);
    }
}
