package com.storypublisher.repository;

import com.storypublisher.model.Story;
import com.storypublisher.model.StoryPrivacy;
import com.storypublisher.model.StoryStatus;
import com.storypublisher.model.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    
    // Override findById to always fetch author
    @Query("SELECT s FROM Story s JOIN FETCH s.author WHERE s.id = :id")
    Optional<Story> findByIdWithAuthor(@Param("id") Long id);
    
    // Find stories by author (user-specific filtering)
    @Query("SELECT s FROM Story s JOIN FETCH s.author WHERE s.author = :author")
    Page<Story> findByAuthor(@Param("author") User author, Pageable pageable);
    
    @Query("SELECT s FROM Story s JOIN FETCH s.author WHERE s.author = :author AND s.status = :status")
    Page<Story> findByAuthorAndStatus(@Param("author") User author, @Param("status") StoryStatus status, Pageable pageable);
    
    // Find public stories for discovery
    @Query("SELECT s FROM Story s JOIN FETCH s.author WHERE s.status = :status AND s.privacy = :privacy")
    Page<Story> findByStatusAndPrivacy(@Param("status") StoryStatus status, @Param("privacy") StoryPrivacy privacy, Pageable pageable);
    
    // Find story by ID and author (ownership validation)
    @Query("SELECT s FROM Story s JOIN FETCH s.author WHERE s.id = :id AND s.author = :author")
    Optional<Story> findByIdAndAuthor(@Param("id") Long id, @Param("author") User author);
    
    // Find public story by slug (for public viewing)
    @Query("SELECT s FROM Story s JOIN FETCH s.author WHERE s.slug = :slug AND s.status = :status AND s.privacy = :privacy")
    Optional<Story> findBySlugAndStatusAndPrivacy(@Param("slug") String slug, @Param("status") StoryStatus status, @Param("privacy") StoryPrivacy privacy);
    
    // Find any story by slug (for authenticated access)
    @Query("SELECT s FROM Story s JOIN FETCH s.author WHERE s.slug = :slug")
    Optional<Story> findBySlug(@Param("slug") String slug);
    
    // Search stories by title or content
    @Query("SELECT s FROM Story s JOIN FETCH s.author WHERE s.status = :status AND s.privacy = :privacy AND " +
           "(LOWER(s.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Story> searchPublicStories(@Param("searchTerm") String searchTerm, 
                                   @Param("status") StoryStatus status,
                                   @Param("privacy") StoryPrivacy privacy,
                                   Pageable pageable);
    
    // Get user's story count by status
    Long countByAuthorAndStatus(User author, StoryStatus status);
    
    // Find stories by category
    @Query("SELECT s FROM Story s JOIN FETCH s.author WHERE s.category.id = :categoryId AND s.status = :status AND s.privacy = :privacy")
    Page<Story> findByCategoryAndStatusAndPrivacy(@Param("categoryId") Long categoryId,
                                                 @Param("status") StoryStatus status,
                                                 @Param("privacy") StoryPrivacy privacy,
                                                 Pageable pageable);
    
    // Find popular stories (by like count)
    @Query("SELECT s FROM Story s JOIN FETCH s.author WHERE s.status = :status AND s.privacy = :privacy ORDER BY s.likeCount DESC")
    Page<Story> findPopularStories(@Param("status") StoryStatus status,
                                  @Param("privacy") StoryPrivacy privacy,
                                  Pageable pageable);
}
