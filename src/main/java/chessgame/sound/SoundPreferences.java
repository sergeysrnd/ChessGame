package chessgame.sound;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс для управления сохранением и загрузкой предпочтений звука пользователя.
 * Использует JSON для сохранения настроек в файл конфигурации.
 */
public class SoundPreferences {
    
    private static final Logger logger = LoggerFactory.getLogger(SoundPreferences.class);
    private static final String PREFERENCES_DIR = System.getProperty("user.home") + "/.chessgame";
    private static final String PREFERENCES_FILE = PREFERENCES_DIR + "/sound_preferences.json";
    
    private String currentSoundSet;
    private float masterVolume;
    private boolean soundEnabled;
    private Map<String, Float> eventVolumes;
    private Map<String, Object> customSettings;
    
    /**
     * Конструктор с значениями по умолчанию.
     */
    public SoundPreferences() {
        this.currentSoundSet = "marble";
        this.masterVolume = 1.0f;
        this.soundEnabled = true;
        this.eventVolumes = new HashMap<>();
        this.customSettings = new HashMap<>();
        
        // Инициализируем громкость для каждого события
        eventVolumes.put("move", 1.0f);
        eventVolumes.put("capture", 1.0f);
        eventVolumes.put("check", 1.0f);
        eventVolumes.put("checkmate", 1.0f);
    }
    
    /**
     * Загружает предпочтения из файла.
     * 
     * @return объект SoundPreferences с загруженными настройками
     */
    public static SoundPreferences load() {
        try {
            File preferencesFile = new File(PREFERENCES_FILE);
            if (preferencesFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                SoundPreferences prefs = mapper.readValue(preferencesFile, SoundPreferences.class);
                logger.info("Предпочтения звука загружены из {}", PREFERENCES_FILE);
                return prefs;
            }
        } catch (IOException e) {
            logger.warn("Не удалось загрузить предпочтения звука, используются значения по умолчанию", e);
        }
        
        return new SoundPreferences();
    }
    
    /**
     * Сохраняет предпочтения в файл.
     * 
     * @return true если сохранение успешно
     */
    public boolean save() {
        try {
            // Создаем директорию если её нет
            Files.createDirectories(Paths.get(PREFERENCES_DIR));
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter()
                  .writeValue(new File(PREFERENCES_FILE), this);
            
            logger.info("Предпочтения звука сохранены в {}", PREFERENCES_FILE);
            return true;
        } catch (IOException e) {
            logger.error("Не удалось сохранить предпочтения звука", e);
            return false;
        }
    }
    
    /**
     * Получает текущий набор звуков.
     */
    public String getCurrentSoundSet() {
        return currentSoundSet;
    }
    
    /**
     * Устанавливает текущий набор звуков.
     */
    public void setCurrentSoundSet(String soundSet) {
        this.currentSoundSet = soundSet;
    }
    
    /**
     * Получает главную громкость (0.0 - 1.0).
     */
    public float getMasterVolume() {
        return Math.max(0.0f, Math.min(1.0f, masterVolume));
    }
    
    /**
     * Устанавливает главную громкость.
     * 
     * @param volume значение от 0.0 до 1.0
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    /**
     * Проверяет включены ли звуки.
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    /**
     * Включает/отключает звуки.
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    /**
     * Получает громкость для конкретного события.
     * 
     * @param eventType тип события
     * @return громкость от 0.0 до 1.0
     */
    public float getEventVolume(String eventType) {
        Float volume = eventVolumes.get(eventType);
        if (volume == null) {
            volume = 1.0f;
            eventVolumes.put(eventType, volume);
        }
        return Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    /**
     * Устанавливает громкость для события.
     * 
     * @param eventType тип события
     * @param volume громкость от 0.0 до 1.0
     */
    public void setEventVolume(String eventType, float volume) {
        eventVolumes.put(eventType, Math.max(0.0f, Math.min(1.0f, volume)));
    }
    
    /**
     * Получает все громкости событий.
     */
    public Map<String, Float> getEventVolumes() {
        return new HashMap<>(eventVolumes);
    }
    
    /**
     * Устанавливает все громкости событий.
     */
    public void setEventVolumes(Map<String, Float> volumes) {
        this.eventVolumes = new HashMap<>(volumes);
    }
    
    /**
     * Получает пользовательскую настройку.
     */
    public Object getCustomSetting(String key) {
        return customSettings.get(key);
    }
    
    /**
     * Устанавливает пользовательскую настройку.
     */
    public void setCustomSetting(String key, Object value) {
        customSettings.put(key, value);
    }
    
    /**
     * Получает все пользовательские настройки.
     */
    public Map<String, Object> getCustomSettings() {
        return new HashMap<>(customSettings);
    }
    
    /**
     * Сбрасывает все настройки на значения по умолчанию.
     */
    public void reset() {
        this.currentSoundSet = "marble";
        this.masterVolume = 1.0f;
        this.soundEnabled = true;
        this.eventVolumes.clear();
        this.eventVolumes.put("move", 1.0f);
        this.eventVolumes.put("capture", 1.0f);
        this.eventVolumes.put("check", 1.0f);
        this.eventVolumes.put("checkmate", 1.0f);
        this.customSettings.clear();
    }
    
    @Override
    public String toString() {
        return "SoundPreferences{" +
                "currentSoundSet='" + currentSoundSet + '\'' +
                ", masterVolume=" + masterVolume +
                ", soundEnabled=" + soundEnabled +
                ", eventVolumes=" + eventVolumes +
                '}';
    }
}
