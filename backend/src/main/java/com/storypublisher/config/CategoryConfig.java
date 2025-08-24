package com.storypublisher.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for category settings.
 * Allows customization of category thresholds, names, descriptions, and colors.
 */
@Configuration
@ConfigurationProperties(prefix = "app.categories")
public class CategoryConfig {
    
    private Thresholds thresholds = new Thresholds();
    private CategoryInfo shortCategory = new CategoryInfo();
    private CategoryInfo medium = new CategoryInfo();
    private CategoryInfo longCategory = new CategoryInfo();
    private CategoryInfo drama = new CategoryInfo();
    private CategoryInfo travelog = new CategoryInfo();
    private CategoryInfo poetry = new CategoryInfo();
    private CategoryInfo interestingAspects = new CategoryInfo();
    private CategoryInfo devotionalSongs = new CategoryInfo();
    private CategoryInfo kian = new CategoryInfo();
    private CategoryInfo general = new CategoryInfo();
    private CategoryInfo technology = new CategoryInfo();
    private CategoryInfo spiritual = new CategoryInfo();
    private CategoryInfo family = new CategoryInfo();
    
    // Getters and setters
    public Thresholds getThresholds() {
        return thresholds;
    }
    
    public void setThresholds(Thresholds thresholds) {
        this.thresholds = thresholds;
    }
    
    public CategoryInfo getShort() {
        return shortCategory;
    }
    
    public void setShort(CategoryInfo shortCategory) {
        this.shortCategory = shortCategory;
    }
    
    public CategoryInfo getMedium() {
        return medium;
    }
    
    public void setMedium(CategoryInfo medium) {
        this.medium = medium;
    }
    
    public CategoryInfo getLong() {
        return longCategory;
    }
    
    public void setLong(CategoryInfo longCategory) {
        this.longCategory = longCategory;
    }
    
    public CategoryInfo getDrama() {
        return drama;
    }
    
    public void setDrama(CategoryInfo drama) {
        this.drama = drama;
    }
    
    public CategoryInfo getTravelog() {
        return travelog;
    }
    
    public void setTravelog(CategoryInfo travelog) {
        this.travelog = travelog;
    }
    
    public CategoryInfo getPoetry() {
        return poetry;
    }
    
    public void setPoetry(CategoryInfo poetry) {
        this.poetry = poetry;
    }
    
    public CategoryInfo getInterestingAspects() {
        return interestingAspects;
    }
    
    public void setInterestingAspects(CategoryInfo interestingAspects) {
        this.interestingAspects = interestingAspects;
    }
    
    public CategoryInfo getDevotionalSongs() {
        return devotionalSongs;
    }
    
    public void setDevotionalSongs(CategoryInfo devotionalSongs) {
        this.devotionalSongs = devotionalSongs;
    }
    
    public CategoryInfo getGeneral() {
        return general;
    }
    
    public void setGeneral(CategoryInfo general) {
        this.general = general;
    }
    
    public CategoryInfo getTechnology() {
        return technology;
    }
    
    public void setTechnology(CategoryInfo technology) {
        this.technology = technology;
    }
    
    public CategoryInfo getSpiritual() {
        return spiritual;
    }
    
    public void setSpiritual(CategoryInfo spiritual) {
        this.spiritual = spiritual;
    }
    
    public CategoryInfo getFamily() {
        return family;
    }
    
    public void setFamily(CategoryInfo family) {
        this.family = family;
    }

    public CategoryInfo getKian() {
        return kian;
    }
    
    public void setKian(CategoryInfo kian) {
        this.kian = kian;
    }

    /**
     * Threshold configuration for categorizing stories based on character count
     */
    public static class Thresholds {
        private int shortMax = 300;
        private int mediumMax = 750;
        private int longMax = 1500;
        
        public int getShortMax() {
            return shortMax;
        }
        
        public void setShortMax(int shortMax) {
            this.shortMax = shortMax;
        }
        
        public int getMediumMax() {
            return mediumMax;
        }
        
        public void setMediumMax(int mediumMax) {
            this.mediumMax = mediumMax;
        }
        
        public int getLongMax() {
            return longMax;
        }
        
        public void setLongMax(int longMax) {
            this.longMax = longMax;
        }
    }
    
    /**
     * Category information including name, description, and color
     */
    public static class CategoryInfo {
        private String name;
        private String description;
        private String color;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getColor() {
            return color;
        }
        
        public void setColor(String color) {
            this.color = color;
        }
    }
}
