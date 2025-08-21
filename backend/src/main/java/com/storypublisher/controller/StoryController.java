package com.storypublisher.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.storypublisher.dto.StoryResponse;
import com.storypublisher.model.Story;
import com.storypublisher.model.User;
import com.storypublisher.service.StoryService;

@RestController
@RequestMapping("/stories")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:6001"})
public class StoryController {
    
    @Autowired
    private StoryService storyService;
    
    // Get all stories (public access) - returns published stories
    @GetMapping
    public ResponseEntity<Page<StoryResponse>> getAllStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<StoryResponse> stories = storyService.getPublicStoriesAsResponse(pageable);
        return ResponseEntity.ok(stories);
    }

    // Get user's own stories (authenticated access)
    @GetMapping("/my")
    public ResponseEntity<Page<Story>> getMyStories(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Story> stories = storyService.getStoriesByAuthor(currentUser, pageable);
        return ResponseEntity.ok(stories);
    }
    
    // Get story by ID (public access for reading)
    @GetMapping("/{id}")
    public ResponseEntity<StoryResponse> getStory(@PathVariable Long id) {
        Story story = storyService.getPublicStoryById(id);
        return ResponseEntity.ok(new StoryResponse(story));
    }
    
    // Create new story
    @PostMapping
    public ResponseEntity<Story> createStory(
            @RequestBody Story story,
            @AuthenticationPrincipal User currentUser) {
        
        Story createdStory = storyService.createStory(story, currentUser);
        return ResponseEntity.ok(createdStory);
    }
    
    // Update story (with ownership check)
    @PutMapping("/{id}")
    public ResponseEntity<Story> updateStory(
            @PathVariable Long id,
            @RequestBody Story story,
            @AuthenticationPrincipal User currentUser) {
        
        Story updatedStory = storyService.updateStory(id, story, currentUser);
        return ResponseEntity.ok(updatedStory);
    }
    
    // Delete story (with ownership check)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStory(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        storyService.deleteStory(id, currentUser);
        return ResponseEntity.ok().build();
    }
    
    // Publish story
    @PostMapping("/{id}/publish")
    public ResponseEntity<Story> publishStory(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        Story story = storyService.publishStory(id, currentUser);
        return ResponseEntity.ok(story);
    }
    
    // Unpublish story
    @PostMapping("/{id}/unpublish")
    public ResponseEntity<Story> unpublishStory(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        Story story = storyService.unpublishStory(id, currentUser);
        return ResponseEntity.ok(story);
    }
    
    // Search public stories
    @GetMapping("/search")
    public ResponseEntity<Page<StoryResponse>> searchStories(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by("publishedAt").descending());
        
        Page<StoryResponse> stories = storyService.searchPublicStoriesAsResponse(query, pageable);
        return ResponseEntity.ok(stories);
    }
}
