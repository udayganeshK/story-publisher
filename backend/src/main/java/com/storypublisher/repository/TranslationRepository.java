package com.storypublisher.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.storypublisher.model.Translation;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Long> {
    
    /**
     * Find cached translation by content hash and language pair
     */
    @Query("SELECT t FROM Translation t WHERE t.contentHash = :contentHash " +
           "AND t.sourceLanguage = :sourceLanguage AND t.targetLanguage = :targetLanguage " +
           "AND (t.expiresAt IS NULL OR t.expiresAt > :now)")
    Optional<Translation> findValidTranslation(@Param("contentHash") String contentHash,
                                              @Param("sourceLanguage") String sourceLanguage,
                                              @Param("targetLanguage") String targetLanguage,
                                              @Param("now") LocalDateTime now);
    
    /**
     * Find all translations for a specific story
     */
    List<Translation> findByStoryId(Long storyId);
    
    /**
     * Find translations by source language
     */
    List<Translation> findBySourceLanguage(String sourceLanguage);
    
    /**
     * Find translations by target language
     */
    List<Translation> findByTargetLanguage(String targetLanguage);
    
    /**
     * Delete expired translations
     */
    @Query("DELETE FROM Translation t WHERE t.expiresAt IS NOT NULL AND t.expiresAt < :now")
    void deleteExpiredTranslations(@Param("now") LocalDateTime now);
    
    /**
     * Count translations by provider
     */
    long countByTranslationProvider(String provider);
}
