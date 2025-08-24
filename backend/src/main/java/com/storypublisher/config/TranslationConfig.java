package com.storypublisher.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for translation service settings.
 * Supports multiple translation providers like Google Translate, Azure Translator, etc.
 */
@Configuration
@ConfigurationProperties(prefix = "app.translation")
public class TranslationConfig {
    
    private boolean enabled = true;
    private String provider = "google";
    private String defaultSourceLanguage = "te"; // Telugu
    private String defaultTargetLanguage = "en"; // English
    private List<String> supportedLanguages = List.of("te", "en", "hi", "ta", "kn", "ml");
    
    private Google google = new Google();
    private Cache cache = new Cache();
    
    // Getters and setters
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String getDefaultSourceLanguage() {
        return defaultSourceLanguage;
    }
    
    public void setDefaultSourceLanguage(String defaultSourceLanguage) {
        this.defaultSourceLanguage = defaultSourceLanguage;
    }
    
    public String getDefaultTargetLanguage() {
        return defaultTargetLanguage;
    }
    
    public void setDefaultTargetLanguage(String defaultTargetLanguage) {
        this.defaultTargetLanguage = defaultTargetLanguage;
    }
    
    public List<String> getSupportedLanguages() {
        return supportedLanguages;
    }
    
    public void setSupportedLanguages(List<String> supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }
    
    public Google getGoogle() {
        return google;
    }
    
    public void setGoogle(Google google) {
        this.google = google;
    }
    
    public Cache getCache() {
        return cache;
    }
    
    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
    /**
     * Google Translate API configuration
     */
    public static class Google {
        private String apiKey;
        private String projectId;
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getProjectId() {
            return projectId;
        }
        
        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }
    }
    
    /**
     * Translation cache configuration
     */
    public static class Cache {
        private boolean enabled = true;
        private int ttl = 3600; // Time to live in seconds (1 hour)
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getTtl() {
            return ttl;
        }
        
        public void setTtl(int ttl) {
            this.ttl = ttl;
        }
    }
}
