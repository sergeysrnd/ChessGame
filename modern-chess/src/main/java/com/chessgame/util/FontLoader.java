package com.chessgame.util;

import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Optimized font loading and caching system
 * Provides efficient font loading with caching, preloading, and memory management
 */
public final class FontLoader {
    private static final Logger logger = LoggerFactory.getLogger(FontLoader.class);
    
    private static FontLoader instance;
    private final Map<String, Font> fontCache;
    private final Set<String> loadingFonts;
    private final ExecutorService loadingExecutor;
    private final FontMetricsTracker metricsTracker;
    
    // Font loading configuration
    private static final int MAX_CACHE_SIZE = 1000;
    private static final long CACHE_CLEANUP_INTERVAL = 5 * 60 * 1000; // 5 minutes
    private static final int PREFETCH_BATCH_SIZE = 10;
    
    private FontLoader() {
        this.fontCache = new ConcurrentHashMap<>(MAX_CACHE_SIZE);
        this.loadingFonts = new ConcurrentSkipListSet<>();
        this.loadingExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "FontLoader-Thread");
            t.setDaemon(true);
            return t;
        });
        this.metricsTracker = new FontMetricsTracker();
        
        // Start cache cleanup task
        startCacheCleanupTask();
        
        logger.info("FontLoader initialized with cache size: {}", MAX_CACHE_SIZE);
    }
    
    /**
     * Gets the singleton instance
     */
    public static synchronized FontLoader getInstance() {
        if (instance == null) {
            instance = new FontLoader();
        }
        return instance;
    }
    
    /**
     * Loads a font asynchronously with caching
     */
    public CompletableFuture<Font> loadFontAsync(String family, double size) {
        String cacheKey = generateCacheKey(family, size);
        
        // Check cache first
        Font cached = fontCache.get(cacheKey);
        if (cached != null) {
            metricsTracker.recordHit(cacheKey);
            return CompletableFuture.completedFuture(cached);
        }
        
        // Check if already loading
        if (loadingFonts.contains(cacheKey)) {
            return CompletableFuture.supplyAsync(() -> {
                // Wait for loading to complete
                while (loadingFonts.contains(cacheKey)) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                return fontCache.get(cacheKey);
            }, loadingExecutor);
        }
        
        // Start loading
        loadingFonts.add(cacheKey);
        metricsTracker.recordMiss(cacheKey);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Font font = Font.font(family, size);
                fontCache.put(cacheKey, font);
                logger.debug("Loaded font: {} {}px (cache size: {})", family, size, fontCache.size());
                return font;
            } finally {
                loadingFonts.remove(cacheKey);
            }
        }, loadingExecutor);
    }
    
    /**
     * Loads a font synchronously
     */
    public Font loadFontSync(String family, double size) {
        return loadFontAsync(family, size).join();
    }
    
    /**
     * Preloads common fonts for better performance
     */
    public void preloadCommonFonts() {
        logger.info("Starting font preloading...");
        
        List<CompletableFuture<Font>> futures = new ArrayList<>();
        
        // Preload primary fonts in common sizes
        double[] commonSizes = {
            Constants.FONT_SIZE_SMALL,
            Constants.FONT_SIZE_NORMAL,
            Constants.FONT_SIZE_MEDIUM,
            Constants.FONT_SIZE_LARGE
        };
        
        for (double size : commonSizes) {
            futures.add(loadFontAsync(FontConfig.PRIMARY_FONT, size));
            futures.add(loadFontAsync(FontConfig.SECONDARY_FONT, size));
            futures.add(loadFontAsync(FontConfig.MONO_FONT, size));
            futures.add(loadFontAsync(FontConfig.DISPLAY_FONT, size));
        }
        
        // Wait for all preloading to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> logger.info("Font preloading completed. Cache size: {}", fontCache.size()))
            .exceptionally(throwable -> {
                logger.error("Error during font preloading", throwable);
                return null;
            });
    }
    
    /**
     * Preloads fonts in batches for memory efficiency
     */
    public void preloadFontsInBatches() {
        logger.info("Starting batch font preloading...");
        
        List<String> fontFamilies = List.of(
            FontConfig.PRIMARY_FONT,
            FontConfig.SECONDARY_FONT,
            FontConfig.MONO_FONT,
            FontConfig.DISPLAY_FONT,
            "Times New Roman",
            "Courier New",
            "Arial Black",
            "Verdana"
        );
        
        List<Double> fontSizes = List.of(
            Constants.FONT_SIZE_TINY,
            Constants.FONT_SIZE_SMALL,
            Constants.FONT_SIZE_NORMAL,
            Constants.FONT_SIZE_MEDIUM,
            Constants.FONT_SIZE_LARGE,
            Constants.FONT_SIZE_XLARGE
        );
        
        // Process in batches
        List<CompletableFuture<Font>> batch = new ArrayList<>();
        int count = 0;
        
        for (String family : fontFamilies) {
            for (double size : fontSizes) {
                batch.add(loadFontAsync(family, size));
                count++;
                
                if (batch.size() >= PREFETCH_BATCH_SIZE || count == fontFamilies.size() * fontSizes.size()) {
                    // Process current batch
                    CompletableFuture.allOf(batch.toArray(new CompletableFuture[0]))
                        .thenRun(() -> {
                            logger.debug("Processed batch of {} fonts. Cache size: {}", batch.size(), fontCache.size());
                        })
                        .join(); // Wait for batch to complete
                    
                    batch.clear();
                    
                    // Small delay to prevent overwhelming the system
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        logger.info("Batch font preloading completed. Final cache size: {}", fontCache.size());
    }
    
    /**
     * Gets font with automatic loading if not cached
     */
    public Font getFont(String family, double size) {
        return loadFontSync(family, size);
    }
    
    /**
     * Gets cache statistics
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("cacheSize", fontCache.size());
        stats.put("maxCacheSize", MAX_CACHE_SIZE);
        stats.put("loadingFonts", loadingFonts.size());
        stats.put("hitRate", metricsTracker.getHitRate());
        stats.put("totalRequests", metricsTracker.getTotalRequests());
        stats.put("cacheUtilization", (double) fontCache.size() / MAX_CACHE_SIZE);
        return stats;
    }
    
    /**
     * Clears the font cache
     */
    public void clearCache() {
        int clearedCount = fontCache.size();
        fontCache.clear();
        metricsTracker.reset();
        logger.info("Cleared font cache. Removed {} fonts.", clearedCount);
    }
    
    /**
     * Optimizes cache by removing least recently used fonts
     */
    public void optimizeCache() {
        if (fontCache.size() < MAX_CACHE_SIZE * 0.8) {
            return; // No optimization needed
        }
        
        List<String> toRemove = metricsTracker.getLeastRecentlyUsed((int) (MAX_CACHE_SIZE * 0.2));
        int removedCount = 0;
        
        for (String key : toRemove) {
            if (fontCache.remove(key) != null) {
                removedCount++;
            }
        }
        
        logger.info("Cache optimization completed. Removed {} fonts, {} remaining.", 
                   removedCount, fontCache.size());
    }
    
    /**
     * Shuts down the font loader and cleans up resources
     */
    public void shutdown() {
        logger.info("Shutting down FontLoader...");
        
        try {
            loadingExecutor.shutdown();
            if (!loadingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                loadingExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            loadingExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        clearCache();
        logger.info("FontLoader shutdown completed");
    }
    
    private String generateCacheKey(String family, double size) {
        return String.format("%s-%.1f", family, size);
    }
    
    private void startCacheCleanupTask() {
        CompletableFuture.runAsync(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(CACHE_CLEANUP_INTERVAL);
                    optimizeCache();
                    var stats = getCacheStatistics();
                    logger.debug("Cache cleanup completed. Stats: {}", stats);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.warn("Error during cache cleanup", e);
                }
            }
        }, loadingExecutor);
    }
    
    /**
     * Tracks font usage metrics for optimization
     */
    private static class FontMetricsTracker {
        private final Map<String, Long> accessTimes;
        private final Map<String, Integer> accessCounts;
        private long totalRequests;
        private long cacheHits;
        
        public FontMetricsTracker() {
            this.accessTimes = new ConcurrentHashMap<>();
            this.accessCounts = new ConcurrentHashMap<>();
            this.totalRequests = 0;
            this.cacheHits = 0;
        }
        
        public void recordHit(String cacheKey) {
            accessTimes.put(cacheKey, System.currentTimeMillis());
            accessCounts.merge(cacheKey, 1, Integer::sum);
            totalRequests++;
            cacheHits++;
        }
        
        public void recordMiss(String cacheKey) {
            totalRequests++;
            accessTimes.put(cacheKey, System.currentTimeMillis());
        }
        
        public double getHitRate() {
            return totalRequests > 0 ? (double) cacheHits / totalRequests : 0.0;
        }
        
        public long getTotalRequests() {
            return totalRequests;
        }
        
        public List<String> getLeastRecentlyUsed(int count) {
            return accessTimes.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(count)
                .map(Map.Entry::getKey)
                .toList();
        }
        
        public void reset() {
            accessTimes.clear();
            accessCounts.clear();
            totalRequests = 0;
            cacheHits = 0;
        }
    }
    
    /**
     * Font loading configuration
     */
    public static class Config {
        private int maxCacheSize = MAX_CACHE_SIZE;
        private long cleanupInterval = CACHE_CLEANUP_INTERVAL;
        private int prefetchBatchSize = PREFETCH_BATCH_SIZE;
        private boolean enableMetrics = true;
        private boolean enablePreloading = true;
        
        public Config setMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            return this;
        }
        
        public Config setCleanupInterval(long cleanupInterval) {
            this.cleanupInterval = cleanupInterval;
            return this;
        }
        
        public Config setPrefetchBatchSize(int prefetchBatchSize) {
            this.prefetchBatchSize = prefetchBatchSize;
            return this;
        }
        
        public Config setEnableMetrics(boolean enableMetrics) {
            this.enableMetrics = enableMetrics;
            return this;
        }
        
        public Config setEnablePreloading(boolean enablePreloading) {
            this.enablePreloading = enablePreloading;
            return this;
        }
        
        // Getters
        public int getMaxCacheSize() { return maxCacheSize; }
        public long getCleanupInterval() { return cleanupInterval; }
        public int getPrefetchBatchSize() { return prefetchBatchSize; }
        public boolean isEnableMetrics() { return enableMetrics; }
        public boolean isEnablePreloading() { return enablePreloading; }
    }
    
    @Override
    public String toString() {
        var stats = getCacheStatistics();
        return String.format("FontLoader{cache=%d/%d, hits=%.2f%%, loading=%d}", 
            stats.get("cacheSize"), stats.get("maxCacheSize"),
            stats.get("hitRate"), stats.get("loadingFonts"));
    }
}