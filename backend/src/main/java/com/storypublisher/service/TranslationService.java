package com.storypublisher.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.storypublisher.config.TranslationConfig;
import com.storypublisher.model.Story;
import com.storypublisher.model.Translation;
import com.storypublisher.repository.TranslationRepository;

/**
 * Translation service for translating stories between Telugu and English
 * Uses Google Translate API or fallback to LibreTranslate
 */
@Service
@Transactional
public class TranslationService {
    
    @Autowired
    private TranslationConfig translationConfig;
    
    @Autowired
    private TranslationRepository translationRepository;
    
    public TranslationService() {
        // Constructor
    }
    
    /**
     * Translate text from one language to another
     */
    public String translateText(String text, String sourceLanguage, String targetLanguage) {
        if (!translationConfig.isEnabled()) {
            return text; // Return original text if translation is disabled
        }
        
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        if (sourceLanguage.equals(targetLanguage)) {
            return text; // No translation needed
        }
        
        // Check cache first
        String contentHash = generateContentHash(text, sourceLanguage, targetLanguage);
        Optional<Translation> cachedTranslation = translationRepository
                .findValidTranslation(contentHash, sourceLanguage, targetLanguage, LocalDateTime.now());
        
        if (cachedTranslation.isPresent()) {
            return cachedTranslation.get().getTranslatedContent();
        }
        
        // Perform translation
        String translatedText = performTranslation(text, sourceLanguage, targetLanguage);
        
        // Cache the result
        if (translationConfig.getCache().isEnabled() && translatedText != null) {
            cacheTranslation(text, translatedText, sourceLanguage, targetLanguage, contentHash);
        }
        
        return translatedText != null ? translatedText : text;
    }
    
    /**
     * Translate a complete story
     */
    public Translation translateStory(Story story, String targetLanguage) {
        String sourceLanguage = detectLanguage(story.getContent());
        
        String translatedTitle = translateText(story.getTitle(), sourceLanguage, targetLanguage);
        String translatedContent = translateText(story.getContent(), sourceLanguage, targetLanguage);
        String translatedExcerpt = story.getExcerpt() != null ? 
                translateText(story.getExcerpt(), sourceLanguage, targetLanguage) : null;
        
        // Create a combined translation record
        String combinedSource = story.getTitle() + "\n\n" + story.getContent();
        String combinedTranslation = translatedTitle + "\n\n" + translatedContent;
        
        String contentHash = generateContentHash(combinedSource, sourceLanguage, targetLanguage);
        
        Translation translation = new Translation(
            combinedSource,
            combinedTranslation,
            sourceLanguage,
            targetLanguage,
            contentHash,
            translationConfig.getProvider()
        );
        
        translation.setStory(story);
        
        if (translationConfig.getCache().isEnabled()) {
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(translationConfig.getCache().getTtl());
            translation.setExpiresAt(expiresAt);
        }
        
        return translationRepository.save(translation);
    }
    
    /**
     * Get all translations for a story
     */
    public List<Translation> getStoryTranslations(Long storyId) {
        return translationRepository.findByStoryId(storyId);
    }
    
    /**
     * Simple language detection based on script
     */
    public String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return translationConfig.getDefaultSourceLanguage();
        }
        
        // Count Telugu characters
        long teluguCharCount = text.chars()
            .filter(c -> c >= 0x0C00 && c <= 0x0C7F)
            .count();
        
        // If we have any Telugu characters, it's Telugu
        if (teluguCharCount > 0) {
            return "te";
        }
        
        // Fallback regex check
        if (text.matches(".*[\\u0C00-\\u0C7F].*")) {
            return "te";
        }
        return "en";
    }
    
    /**
     * Perform the actual translation using external API
     */
    private String performTranslation(String text, String sourceLanguage, String targetLanguage) {
        try {
            // Using LibreTranslate (free, self-hosted option)
            // You can replace this with Google Translate API if you have API keys
            return translateWithLibreTranslate(text, sourceLanguage, targetLanguage);
        } catch (Exception e) {
            System.err.println("Translation failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Translate using LibreTranslate API (free alternative)
     */
    private String translateWithLibreTranslate(String text, String sourceLanguage, String targetLanguage) {
        try {
            // Use MyMemory API (free translation service)
            return translateWithMyMemory(text, sourceLanguage, targetLanguage);
        } catch (Exception e) {
            System.err.println("MyMemory translation failed: " + e.getMessage());
            // Return a user-friendly message instead of mock translation
            return getTranslationUnavailableMessage(sourceLanguage, targetLanguage);
        }
    }
    
    /**
     * Get a user-friendly message when translation is not available
     */
    private String getTranslationUnavailableMessage(String sourceLanguage, String targetLanguage) {
        if (targetLanguage.equals("te")) {
            return "అనువాదం ప్రస్తుతం అందుబాటులో లేదు. కొద్దిసేపటి తర్వాత మళ్లీ ప్రయత్నించండి.";
        } else if (targetLanguage.equals("hi")) {
            return "अनुवाद वर्तमान में उपलब्ध नहीं है। कृपया बाद में पुनः प्रयास करें।";
        } else if (targetLanguage.equals("ta")) {
            return "மொழிபெயர்ப்பு தற்போது கிடைக்கவில்லை. தயவுசெய்து பின்னர் மீண்டும் முயற்சிக்கவும்.";
        } else if (targetLanguage.equals("kn")) {
            return "ಅನುವಾದವು ಪ್ರಸ್ತುತ ಲಭ್ಯವಿಲ್ಲ. ದಯವಿಟ್ಟು ನಂತರ ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ.";
        } else if (targetLanguage.equals("ml")) {
            return "വിവർത്തനം നിലവിൽ ലഭ്യമല്ല. ദയവായി പിന്നീട് വീണ്ടും ശ്രമിക്കുക.";
        } else {
            // Default English message
            return "Translation is currently unavailable. Please try again later.";
        }
    }
    
    /**
     * Translate using MyMemory API (free, no API key required)
     */
    private String translateWithMyMemory(String text, String sourceLanguage, String targetLanguage) {
        try {
            // Build URL string directly to avoid URI issues
            String encodedText = java.net.URLEncoder.encode(text, java.nio.charset.StandardCharsets.UTF_8);
            String encodedLangPair = java.net.URLEncoder.encode(sourceLanguage + "|" + targetLanguage, java.nio.charset.StandardCharsets.UTF_8);
            
            String urlString = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=" + encodedLangPair;
            
            // Create URI from the properly encoded string
            java.net.URI uri = new java.net.URI(urlString);
            
            // Make HTTP request
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(uri)
                .header("User-Agent", "StoryPublisher/1.0")
                .timeout(java.time.Duration.ofSeconds(10))
                .GET()
                .build();
            
            java.net.http.HttpResponse<String> response = client.send(request, 
                java.net.http.HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseMyMemoryResponse(response.body());
            } else {
                throw new RuntimeException("MyMemory API returned status: " + response.statusCode());
            }
            
        } catch (Exception e) {
            throw new RuntimeException("MyMemory API call failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse MyMemory API response
     */
    private String parseMyMemoryResponse(String jsonResponse) {
        try {
            // Simple JSON parsing for the responseData.translatedText field
            int startIndex = jsonResponse.indexOf("\"translatedText\":\"");
            if (startIndex == -1) {
                throw new RuntimeException("Could not find translatedText in response");
            }
            
            startIndex += 18; // Length of "translatedText":"
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            
            if (endIndex == -1) {
                throw new RuntimeException("Could not parse translatedText from response");
            }
            
            String translatedText = jsonResponse.substring(startIndex, endIndex);
            
            // Decode escaped characters
            translatedText = translatedText.replace("\\\"", "\"")
                                         .replace("\\n", "\n")
                                         .replace("\\r", "\r")
                                         .replace("\\t", "\t")
                                         .replace("\\\\", "\\");
            
            return translatedText;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse MyMemory response: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cache translation result
     */
    private void cacheTranslation(String sourceContent, String translatedContent,
                                 String sourceLanguage, String targetLanguage, String contentHash) {
        Translation translation = new Translation(
            sourceContent,
            translatedContent,
            sourceLanguage,
            targetLanguage,
            contentHash,
            translationConfig.getProvider()
        );
        
        if (translationConfig.getCache().getTtl() > 0) {
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(translationConfig.getCache().getTtl());
            translation.setExpiresAt(expiresAt);
        }
        
        translationRepository.save(translation);
    }
    
    /**
     * Generate SHA-256 hash for content caching
     */
    private String generateContentHash(String content, String sourceLanguage, String targetLanguage) {
        try {
            String combined = content + "|" + sourceLanguage + "|" + targetLanguage;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            String combined = content + "|" + sourceLanguage + "|" + targetLanguage;
            return String.valueOf(combined.hashCode());
        }
    }
    
    /**
     * Clean up expired translations
     */
    @Transactional
    public void cleanupExpiredTranslations() {
        translationRepository.deleteExpiredTranslations(LocalDateTime.now());
    }
    
    /**
     * Get supported languages
     */
    public List<String> getSupportedLanguages() {
        return translationConfig.getSupportedLanguages();
    }
}
