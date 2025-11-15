package chessgame.sound;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

/**
 * Центральный менеджер для управления звуками в игре.
 * Обеспечивает загрузку, кэширование, воспроизведение и управление громкостью звуков.
 * Поддерживает асинхронное воспроизведение с использованием потокового пула.
 */
public class SoundManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SoundManager.class);
    private static final int THREAD_POOL_SIZE = 4;
    private static final int CACHE_SIZE = 50;
    
    private final ExecutorService executorService;
    private final Map<String, SoundSet> soundSets;
    private final Map<String, Media> mediaCache;
    private final Queue<String> cacheQueue;
    private SoundPreferences preferences;
    private String currentSoundSet;
    private List<SoundManagerListener> listeners;
    
    /**
     * Конструктор SoundManager.
     */
    public SoundManager() {
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.soundSets = new ConcurrentHashMap<>();
        this.mediaCache = new LinkedHashMap<String, Media>(CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > CACHE_SIZE;
            }
        };
        this.cacheQueue = new ConcurrentLinkedQueue<>();
        this.preferences = SoundPreferences.load();
        this.currentSoundSet = preferences.getCurrentSoundSet();
        this.listeners = new CopyOnWriteArrayList<>();
        
        logger.info("SoundManager инициализирован с размером кэша: {}", CACHE_SIZE);
    }
    
    /**
     * Сканирует директорию звуков и загружает доступные наборы.
     * 
     * @param soundsDirectory путь к директории со звуками
     */
    public void scanAndLoadSoundSets(String soundsDirectory) {
        executorService.submit(() -> {
            try {
                URL soundsUrl = getClass().getResource(soundsDirectory);
                if (soundsUrl == null) {
                    logger.warn("Директория звуков не найдена: {}", soundsDirectory);
                    return;
                }
                
                File soundsDir = new File(soundsUrl.toURI());
                if (!soundsDir.isDirectory()) {
                    logger.warn("Путь не является директорией: {}", soundsDirectory);
                    return;
                }
                
                File[] setDirs = soundsDir.listFiles(File::isDirectory);
                if (setDirs == null) {
                    logger.warn("Не удалось прочитать директорию: {}", soundsDirectory);
                    return;
                }
                
                for (File setDir : setDirs) {
                    loadSoundSet(setDir);
                }
                
                logger.info("Загружено {} наборов звуков", soundSets.size());
                notifyListeners("soundSetsLoaded", soundSets.keySet());
                
            } catch (Exception e) {
                logger.error("Ошибка при сканировании директории звуков", e);
            }
        });
    }
    
    /**
     * Загружает отдельный набор звуков из директории.
     * 
     * @param setDirectory директория набора
     */
    private void loadSoundSet(File setDirectory) {
        try {
            String setName = setDirectory.getName();
            SoundSet soundSet = new SoundSet(setName);
            
            File[] soundFiles = setDirectory.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".mp3") || 
                name.toLowerCase().endsWith(".wav") ||
                name.toLowerCase().endsWith(".aiff"));
            
            if (soundFiles == null || soundFiles.length == 0) {
                logger.warn("Набор звуков {} не содержит аудиофайлов", setName);
                return;
            }
            
            for (File soundFile : soundFiles) {
                String eventType = soundFile.getName()
                    .replaceAll("\\.[^.]+$", "")
                    .toLowerCase();
                String resourcePath = "/sounds/" + setName + "/" + soundFile.getName();
                soundSet.addSoundPath(eventType, resourcePath);
            }
            
            soundSets.put(setName, soundSet);
            logger.info("Загружен набор звуков: {} ({} звуков)", setName, soundSet.getSoundCount());
            
        } catch (Exception e) {
            logger.error("Ошибка при загрузке набора звуков", e);
        }
    }
    
    /**
     * Получает список доступных наборов звуков.
     * 
     * @return список названий наборов
     */
    public List<String> getAvailableSoundSets() {
        return new ArrayList<>(soundSets.keySet());
    }
    
    /**
     * Получает набор звуков по названию.
     * 
     * @param setName название набора
     * @return объект SoundSet или null
     */
    public SoundSet getSoundSet(String setName) {
        return soundSets.get(setName);
    }
    
    /**
     * Устанавливает текущий набор звуков.
     * 
     * @param setName название набора
     * @return true если набор успешно установлен
     */
    public boolean setCurrentSoundSet(String setName) {
        if (!soundSets.containsKey(setName)) {
            logger.warn("Набор звуков не найден: {}", setName);
            return false;
        }
        
        this.currentSoundSet = setName;
        preferences.setCurrentSoundSet(setName);
        preferences.save();
        
        logger.info("Текущий набор звуков изменен на: {}", setName);
        notifyListeners("soundSetChanged", setName);
        return true;
    }
    
    /**
     * Получает текущий набор звуков.
     */
    public String getCurrentSoundSet() {
        return currentSoundSet;
    }
    
    /**
     * Воспроизводит звук асинхронно.
     * 
     * @param eventType тип события (move, capture, check и т.д.)
     */
    public void playSound(String eventType) {
        if (!preferences.isSoundEnabled()) {
            logger.debug("Звуки отключены");
            return;
        }
        
        executorService.submit(() -> {
            try {
                SoundSet soundSet = soundSets.get(currentSoundSet);
                if (soundSet == null) {
                    logger.warn("Набор звуков не найден: {}", currentSoundSet);
                    return;
                }
                
                String soundPath = soundSet.getSoundPath(eventType);
                if (soundPath == null) {
                    logger.debug("Звук для события {} не найден в наборе {}", eventType, currentSoundSet);
                    return;
                }
                
                playSoundFromPath(soundPath, eventType);
                
            } catch (Exception e) {
                logger.error("Ошибка при воспроизведении звука: {}", eventType, e);
            }
        });
    }
    
    /**
     * Воспроизводит звук по пути.
     * 
     * @param soundPath путь к звуковому файлу
     * @param eventType тип события для логирования
     */
    private void playSoundFromPath(String soundPath, String eventType) {
        try {
            Media media = getOrLoadMedia(soundPath);
            if (media == null) {
                logger.warn("Не удалось загрузить медиа: {}", soundPath);
                return;
            }
            
            MediaPlayer player = new MediaPlayer(media);
            
            // Устанавливаем громкость
            float masterVolume = preferences.getMasterVolume();
            float eventVolume = preferences.getEventVolume(eventType);
            float finalVolume = masterVolume * eventVolume;
            player.setVolume(Math.max(0.0, Math.min(1.0, finalVolume)));
            
            // Обработчик ошибок
            player.setOnError(() -> {
                logger.error("Ошибка MediaPlayer для звука: {} ({})", eventType, soundPath);
            });
            
            // Обработчик завершения
            player.setOnEndOfMedia(() -> {
                player.dispose();
            });
            
            player.play();
            logger.debug("Воспроизведение звука: {} ({})", eventType, soundPath);
            
        } catch (IllegalAccessError e) {
            logger.warn("Модульная ошибка при воспроизведении звука, отключаем звуки", e);
            preferences.setSoundEnabled(false);
        } catch (Exception e) {
            logger.error("Ошибка при воспроизведении звука: {}", soundPath, e);
        }
    }
    
    /**
     * Получает медиа из кэша или загружает его.
     * 
     * @param soundPath путь к звуковому файлу
     * @return объект Media или null
     */
    private Media getOrLoadMedia(String soundPath) {
        if (mediaCache.containsKey(soundPath)) {
            logger.debug("Медиа загружено из кэша: {}", soundPath);
            return mediaCache.get(soundPath);
        }
        
        try {
            URL soundUrl = getClass().getResource(soundPath);
            if (soundUrl == null) {
                logger.warn("URL для звука не найден: {}", soundPath);
                return null;
            }
            
            Media media = new Media(soundUrl.toString());
            mediaCache.put(soundPath, media);
            cacheQueue.offer(soundPath);
            
            logger.debug("Медиа загружено и добавлено в кэш: {}", soundPath);
            return media;
            
        } catch (Exception e) {
            logger.error("Ошибка при загрузке медиа: {}", soundPath, e);
            return null;
        }
    }
    
    /**
     * Устанавливает главную громкость.
     * 
     * @param volume громкость от 0.0 до 1.0
     */
    public void setMasterVolume(float volume) {
        preferences.setMasterVolume(volume);
        preferences.save();
        logger.info("Главная громкость установлена на: {}", volume);
        notifyListeners("volumeChanged", volume);
    }
    
    /**
     * Получает главную громкость.
     */
    public float getMasterVolume() {
        return preferences.getMasterVolume();
    }
    
    /**
     * Устанавливает громкость для события.
     * 
     * @param eventType тип события
     * @param volume громкость от 0.0 до 1.0
     */
    public void setEventVolume(String eventType, float volume) {
        preferences.setEventVolume(eventType, volume);
        preferences.save();
        logger.info("Громкость события {} установлена на: {}", eventType, volume);
    }
    
    /**
     * Получает громкость события.
     * 
     * @param eventType тип события
     */
    public float getEventVolume(String eventType) {
        return preferences.getEventVolume(eventType);
    }
    
    /**
     * Включает/отключает звуки.
     * 
     * @param enabled true для включения
     */
    public void setSoundEnabled(boolean enabled) {
        preferences.setSoundEnabled(enabled);
        preferences.save();
        logger.info("Звуки {}", enabled ? "включены" : "отключены");
        notifyListeners("soundEnabledChanged", enabled);
    }
    
    /**
     * Проверяет включены ли звуки.
     */
    public boolean isSoundEnabled() {
        return preferences.isSoundEnabled();
    }
    
    /**
     * Получает объект предпочтений.
     */
    public SoundPreferences getPreferences() {
        return preferences;
    }
    
    /**
     * Очищает кэш медиа.
     */
    public void clearCache() {
        mediaCache.clear();
        cacheQueue.clear();
        logger.info("Кэш медиа очищен");
    }
    
    /**
     * Получает информацию о кэше.
     */
    public Map<String, Object> getCacheInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("cacheSize", mediaCache.size());
        info.put("maxCacheSize", CACHE_SIZE);
        info.put("cachedItems", new ArrayList<>(mediaCache.keySet()));
        return info;
    }
    
    /**
     * Добавляет слушателя событий.
     * 
     * @param listener слушатель
     */
    public void addListener(SoundManagerListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Удаляет слушателя событий.
     * 
     * @param listener слушатель
     */
    public void removeListener(SoundManagerListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Уведомляет слушателей об событии.
     * 
     * @param eventType тип события
     * @param data данные события
     */
    private void notifyListeners(String eventType, Object data) {
        for (SoundManagerListener listener : listeners) {
            try {
                listener.onSoundManagerEvent(eventType, data);
            } catch (Exception e) {
                logger.error("Ошибка при уведомлении слушателя", e);
            }
        }
    }
    
    /**
     * Завершает работу менеджера и освобождает ресурсы.
     */
    public void shutdown() {
        try {
            clearCache();
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            logger.info("SoundManager завершил работу");
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            logger.error("Ошибка при завершении SoundManager", e);
        }
    }
    
    /**
     * Интерфейс для слушателей событий SoundManager.
     */
    public interface SoundManagerListener {
        void onSoundManagerEvent(String eventType, Object data);
    }
}
