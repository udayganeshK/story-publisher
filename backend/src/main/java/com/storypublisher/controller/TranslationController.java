package com.storypublisher.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.storypublisher.model.Story;
import com.storypublisher.model.Translation;
import com.storypublisher.model.User;
import com.storypublisher.service.StoryService;
import com.storypublisher.service.TranslationService;
import com.storypublisher.service.UserService;

/**
 * REST controller for translation operations
 */
@RestController
@RequestMapping("/translations")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class TranslationController {
    
    @Autowired
    private TranslationService translationService;
    
    @Autowired
    private StoryService storyService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Translate text
     */
    @PostMapping("/text")
    public ResponseEntity<?> translateText(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            String sourceLanguage = request.get("sourceLanguage");
            String targetLanguage = request.get("targetLanguage");
            
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Text is required"));
            }
            
            // Auto-detect source language if not provided
            if (sourceLanguage == null || sourceLanguage.trim().isEmpty()) {
                sourceLanguage = translationService.detectLanguage(text);
            }
            
            // Default target language to English if not provided
            if (targetLanguage == null || targetLanguage.trim().isEmpty()) {
                targetLanguage = "en";
            }
            
            String translatedText = translationService.translateText(text, sourceLanguage, targetLanguage);
            
            return ResponseEntity.ok(Map.of(
                "translatedText", translatedText,
                "sourceLanguage", sourceLanguage,
                "targetLanguage", targetLanguage,
                "originalText", text
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Translation failed: " + e.getMessage()));
        }
    }
    
    /**
     * Translate an entire story
     */
    @PostMapping("/story/{storyId}")
    public ResponseEntity<?> translateStory(@PathVariable Long storyId, 
                                          @RequestBody Map<String, String> request) {
        try {
            String targetLanguage = request.get("targetLanguage");
            if (targetLanguage == null || targetLanguage.trim().isEmpty()) {
                targetLanguage = "en"; // Default to English
            }
            
            // Get current user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = null;
            
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getPrincipal().equals("anonymousUser")) {
                String username = authentication.getName();
                currentUser = userService.findByUsername(username);
            }
            
            Story story = storyService.getStoryById(storyId, currentUser);
            
            Translation translation = translationService.translateStory(story, targetLanguage);
            
            return ResponseEntity.ok(Map.of(
                "translationId", translation.getId(),
                "sourceLanguage", translation.getSourceLanguage(),
                "targetLanguage", translation.getTargetLanguage(),
                "translatedContent", translation.getTranslatedContent(),
                "storyId", storyId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Story translation failed: " + e.getMessage()));
        }
    }
    
    /**
     * Get all translations for a story
     */
    @GetMapping("/story/{storyId}")
    public ResponseEntity<?> getStoryTranslations(@PathVariable Long storyId) {
        try {
            List<Translation> translations = translationService.getStoryTranslations(storyId);
            return ResponseEntity.ok(Map.of(
                "translations", translations,
                "count", translations.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to fetch translations: " + e.getMessage()));
        }
    }
    
    /**
     * Get supported languages
     */
    @GetMapping("/languages")
    public ResponseEntity<?> getSupportedLanguages() {
        try {
            List<String> languages = translationService.getSupportedLanguages();
            return ResponseEntity.ok(Map.of(
                "supportedLanguages", languages,
                "languageNames", Map.of(
                    "te", "Telugu",
                    "en", "English", 
                    "hi", "Hindi",
                    "ta", "Tamil",
                    "kn", "Kannada",
                    "ml", "Malayalam"
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to fetch supported languages"));
        }
    }
    
    /**
     * Detect language of given text
     */
    @PostMapping("/detect")
    public ResponseEntity<?> detectLanguage(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Text is required"));
            }
            
            String detectedLanguage = translationService.detectLanguage(text);
            
            return ResponseEntity.ok(Map.of(
                "detectedLanguage", detectedLanguage,
                "text", text
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Language detection failed: " + e.getMessage()));
        }
    }
}
