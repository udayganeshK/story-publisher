package com.storypublisher.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.storypublisher.dto.StoryResponse;
import com.storypublisher.exception.StoryNotFoundException;
import com.storypublisher.model.Story;
import com.storypublisher.model.StoryPrivacy;
import com.storypublisher.model.StoryStatus;
import com.storypublisher.model.User;
import com.storypublisher.repository.StoryRepository;

@Service
@Transactional
public class StoryService {
    
    @Autowired
    private StoryRepository storyRepository;
    
    public Page<Story> getStoriesByAuthor(User author, Pageable pageable) {
        return storyRepository.findByAuthor(author, pageable);
    }
    
    public Page<Story> getPublicStories(Pageable pageable) {
        return storyRepository.findByStatusAndPrivacy(StoryStatus.PUBLISHED, StoryPrivacy.PUBLIC, pageable);
    }
    
    public Page<StoryResponse> getPublicStoriesAsResponse(Pageable pageable) {
        Page<Story> stories = storyRepository.findByStatusAndPrivacy(StoryStatus.PUBLISHED, StoryPrivacy.PUBLIC, pageable);
        List<StoryResponse> storyResponses = stories.getContent().stream()
                .map(StoryResponse::new)
                .collect(Collectors.toList());
        return new PageImpl<>(storyResponses, pageable, stories.getTotalElements());
    }
    
    public Story getStoryById(Long id, User currentUser) {
        Story story = storyRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + id));
        
        // Check if story is accessible to current user
        if (story.getPrivacy() == StoryPrivacy.PRIVATE && !story.isOwnedBy(currentUser)) {
            throw new RuntimeException("Access denied: You don't have permission to view this story");
        }
        
        // Increment view count if not the author
        if (!story.isOwnedBy(currentUser)) {
            story.setViewCount(story.getViewCount() + 1);
            storyRepository.save(story);
        }
        
        return story;
    }
    
    public Story createStory(Story story, User author) {
        story.setAuthor(author);
        story.setStatus(StoryStatus.DRAFT);
        story.setPrivacy(StoryPrivacy.PRIVATE);
        story.setViewCount(0L);
        story.setLikeCount(0L);
        story.setCommentCount(0L);
        
        return storyRepository.save(story);
    }
    
    public Story updateStory(Long id, Story updatedStory, User currentUser) {
        Story existingStory = storyRepository.findByIdAndAuthor(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Story not found or access denied"));
        
        // Update allowed fields
        existingStory.setTitle(updatedStory.getTitle());
        existingStory.setContent(updatedStory.getContent());
        existingStory.setExcerpt(updatedStory.getExcerpt());
        existingStory.setCoverImageUrl(updatedStory.getCoverImageUrl());
        existingStory.setPrivacy(updatedStory.getPrivacy());
        existingStory.setCategory(updatedStory.getCategory());
        
        return storyRepository.save(existingStory);
    }
    
    public void deleteStory(Long id, User currentUser) {
        Story story = storyRepository.findByIdAndAuthor(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Story not found or access denied"));
        
        storyRepository.delete(story);
    }
    
    public Story publishStory(Long id, User currentUser) {
        Story story = storyRepository.findByIdAndAuthor(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Story not found or access denied"));
        
        story.publish();
        return storyRepository.save(story);
    }
    
    public Story unpublishStory(Long id, User currentUser) {
        Story story = storyRepository.findByIdAndAuthor(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Story not found or access denied"));
        
        story.unpublish();
        return storyRepository.save(story);
    }
    
    public Page<Story> searchPublicStories(String searchTerm, Pageable pageable) {
        return storyRepository.searchPublicStories(searchTerm, StoryStatus.PUBLISHED, StoryPrivacy.PUBLIC, pageable);
    }
    
    public Page<StoryResponse> searchPublicStoriesAsResponse(String searchTerm, Pageable pageable) {
        Page<Story> stories = storyRepository.searchPublicStories(searchTerm, StoryStatus.PUBLISHED, StoryPrivacy.PUBLIC, pageable);
        List<StoryResponse> storyResponses = stories.getContent().stream()
                .map(StoryResponse::new)
                .collect(Collectors.toList());
        return new PageImpl<>(storyResponses, pageable, stories.getTotalElements());
    }
    
    public Page<Story> getStoriesByCategory(Long categoryId, Pageable pageable) {
        return storyRepository.findByCategoryAndStatusAndPrivacy(categoryId, StoryStatus.PUBLISHED, StoryPrivacy.PUBLIC, pageable);
    }
    
    public Page<Story> getPopularStories(Pageable pageable) {
        return storyRepository.findPopularStories(StoryStatus.PUBLISHED, StoryPrivacy.PUBLIC, pageable);
    }
    
    // Get story by ID (public access for reading all stories)
    public Story getPublicStoryById(Long id) {
        return storyRepository.findByIdWithAuthor(id)
                .map(story -> {
                    // Increment view count
                    story.setViewCount(story.getViewCount() + 1);
                    return storyRepository.save(story);
                })
                .orElseThrow(() -> new StoryNotFoundException("Story not found with id: " + id));
    }
    
    // Get public story by slug (no authentication required)
    public Story getPublicStoryBySlug(String slug) {
        return storyRepository.findBySlugAndStatusAndPrivacy(slug, StoryStatus.PUBLISHED, StoryPrivacy.PUBLIC)
                .map(story -> {
                    // Increment view count
                    story.setViewCount(story.getViewCount() + 1);
                    return storyRepository.save(story);
                })
                .orElseThrow(() -> new StoryNotFoundException("Published story not found with slug: " + slug));
    }
}
