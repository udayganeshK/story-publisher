package com.storypublisher.dto;

import com.storypublisher.model.Story;
import com.storypublisher.model.StoryPrivacy;
import com.storypublisher.model.StoryStatus;
import java.time.LocalDateTime;

public class StoryResponse {
    private Long id;
    private String title;
    private String content;
    private String excerpt;
    private String slug;
    private String coverImageUrl;
    private StoryStatus status;
    private StoryPrivacy privacy;
    private UserResponse author;
    private Integer readTime;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private boolean published;

    public StoryResponse() {}

    public StoryResponse(Story story) {
        this.id = story.getId();
        this.title = story.getTitle();
        this.content = story.getContent();
        this.excerpt = story.getExcerpt();
        this.slug = story.getSlug();
        this.coverImageUrl = story.getCoverImageUrl();
        this.status = story.getStatus();
        this.privacy = story.getPrivacy();
        this.author = story.getAuthor() != null ? new UserResponse(story.getAuthor()) : null;
        this.readTime = story.getReadTime();
        this.viewCount = story.getViewCount();
        this.likeCount = story.getLikeCount();
        this.commentCount = story.getCommentCount();
        this.createdAt = story.getCreatedAt();
        this.updatedAt = story.getUpdatedAt();
        this.publishedAt = story.getPublishedAt();
        this.published = story.isPublished();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public StoryStatus getStatus() {
        return status;
    }

    public void setStatus(StoryStatus status) {
        this.status = status;
    }

    public StoryPrivacy getPrivacy() {
        return privacy;
    }

    public void setPrivacy(StoryPrivacy privacy) {
        this.privacy = privacy;
    }

    public UserResponse getAuthor() {
        return author;
    }

    public void setAuthor(UserResponse author) {
        this.author = author;
    }

    public Integer getReadTime() {
        return readTime;
    }

    public void setReadTime(Integer readTime) {
        this.readTime = readTime;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }
}
