package chessgame.sound;

import java.util.HashMap;
import java.util.Map;

/**
 * Модель звукового набора, содержащая метаданные и пути к звуковым файлам.
 * Представляет отдельный набор звуков для игровых событий.
 */
public class SoundSet {
    
    private String name;
    private String description;
    private Map<String, String> soundPaths;
    private boolean isAvailable;
    
    /**
     * Конструктор звукового набора.
     * 
     * @param name название набора звуков
     */
    public SoundSet(String name) {
        this.name = name;
        this.soundPaths = new HashMap<>();
        this.isAvailable = true;
        this.description = "";
    }
    
    /**
     * Конструктор с описанием.
     * 
     * @param name название набора
     * @param description описание набора
     */
    public SoundSet(String name, String description) {
        this(name);
        this.description = description;
    }
    
    /**
     * Получает название набора.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Устанавливает название набора.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Получает описание набора.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Устанавливает описание набора.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Добавляет путь к звуковому файлу для определенного события.
     * 
     * @param eventType тип события (move, capture, check и т.д.)
     * @param path путь к звуковому файлу
     */
    public void addSoundPath(String eventType, String path) {
        soundPaths.put(eventType, path);
    }
    
    /**
     * Получает путь к звуковому файлу для события.
     * 
     * @param eventType тип события
     * @return путь к файлу или null если не найден
     */
    public String getSoundPath(String eventType) {
        return soundPaths.get(eventType);
    }
    
    /**
     * Получает все пути к звукам.
     */
    public Map<String, String> getSoundPaths() {
        return new HashMap<>(soundPaths);
    }
    
    /**
     * Проверяет наличие звука для события.
     * 
     * @param eventType тип события
     * @return true если звук существует
     */
    public boolean hasSoundFor(String eventType) {
        return soundPaths.containsKey(eventType);
    }
    
    /**
     * Получает количество звуков в наборе.
     */
    public int getSoundCount() {
        return soundPaths.size();
    }
    
    /**
     * Проверяет доступность набора.
     */
    public boolean isAvailable() {
        return isAvailable;
    }
    
    /**
     * Устанавливает доступность набора.
     */
    public void setAvailable(boolean available) {
        isAvailable = available;
    }
    
    @Override
    public String toString() {
        return name + " (" + soundPaths.size() + " звуков)";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SoundSet soundSet = (SoundSet) obj;
        return name.equals(soundSet.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
