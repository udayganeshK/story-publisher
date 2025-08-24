package com.storypublisher.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Translation entity to store translated content and cache translations
 */
@Entity
@Table(name = "translations")
public class Translation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "source_content", columnDefinition = "TEXT", nullable = false)
    private String sourceContent;
    
    @Column(name = "translated_content", columnDefinition = "TEXT", nullable = false)
    private String translatedContent;
    
    @Column(name = "source_language", nullable = false, length = 10)
    private String sourceLanguage;
    
    @Column(name = "target_language", nullable = false, length = 10)
    private String targetLanguage;
    
    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash; // SHA-256 hash for quick lookup
    
    @Column(name = "translation_provider", length = 50)
    private String translationProvider;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    // Reference to the original story (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private Story story;
    
    // Constructors
    public Translation() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Translation(String sourceContent, String translatedContent, 
                      String sourceLanguage, String targetLanguage, 
                      String contentHash, String translationProvider) {
        this();
        this.sourceContent = sourceContent;
        this.translatedContent = translatedContent;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.contentHash = contentHash;
        this.translationProvider = translationProvider;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSourceContent() {
        return sourceContent;
    }
    
    public void setSourceContent(String sourceContent) {
        this.sourceContent = sourceContent;
    }
    
    public String getTranslatedContent() {
        return translatedContent;
    }
    
    public void setTranslatedContent(String translatedContent) {
        this.translatedContent = translatedContent;
    }
    
    public String getSourceLanguage() {
        return sourceLanguage;
    }
    
    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }
    
    public String getTargetLanguage() {
        return targetLanguage;
    }
    
    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
    
    public String getContentHash() {
        return contentHash;
    }
    
    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }
    
    public String getTranslationProvider() {
        return translationProvider;
    }
    
    public void setTranslationProvider(String translationProvider) {
        this.translationProvider = translationProvider;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Story getStory() {
        return story;
    }
    
    public void setStory(Story story) {
        this.story = story;
    }
    
    /**
     * Check if this translation has expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
