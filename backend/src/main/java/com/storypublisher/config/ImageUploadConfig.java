package com.storypublisher.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.image.upload")
public class ImageUploadConfig {
    
    private boolean enabled = false;
    private String maxSize = "5MB";
    private String allowedTypes = "image/jpeg,image/png,image/gif,image/webp";
    private String storagePath = "./uploads/images";
    private String baseUrl = "http://localhost:8080/api/images";
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getMaxSize() {
        return maxSize;
    }
    
    public void setMaxSize(String maxSize) {
        this.maxSize = maxSize;
    }
    
    public String getAllowedTypes() {
        return allowedTypes;
    }
    
    public void setAllowedTypes(String allowedTypes) {
        this.allowedTypes = allowedTypes;
    }
    
    public String getStoragePath() {
        return storagePath;
    }
    
    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String[] getAllowedTypesArray() {
        return allowedTypes.split(",");
    }
    
    public long getMaxSizeBytes() {
        String size = maxSize.toUpperCase();
        if (size.endsWith("MB")) {
            return Long.parseLong(size.replace("MB", "")) * 1024 * 1024;
        } else if (size.endsWith("KB")) {
            return Long.parseLong(size.replace("KB", "")) * 1024;
        } else if (size.endsWith("GB")) {
            return Long.parseLong(size.replace("GB", "")) * 1024 * 1024 * 1024;
        }
        return Long.parseLong(size); // Assume bytes if no unit
    }
}
