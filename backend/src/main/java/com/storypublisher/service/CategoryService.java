package com.storypublisher.service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.storypublisher.config.CategoryConfig;
import com.storypublisher.model.Category;
import com.storypublisher.repository.CategoryRepository;

@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CategoryConfig categoryConfig;
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    public Optional<Category> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }
    
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }
    
    public Category updateCategory(Long id, Category updatedCategory) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());
        existingCategory.setSlug(updatedCategory.getSlug());
        existingCategory.setColor(updatedCategory.getColor());
        
        return categoryRepository.save(existingCategory);
    }
    
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        categoryRepository.delete(category);
    }
    
    /**
     * Automatically categorizes content based on word count using configurable thresholds
     */
    public Category getCategoryByWordCount(String content) {
        if (content == null || content.trim().isEmpty()) {
            return getCategoryByName(categoryConfig.getShort().getName()).orElse(null);
        }
        
        // Simple word count based on character length (approximation)
        int charCount = content.length();
        
        String categoryName;
        if (charCount < categoryConfig.getThresholds().getShortMax()) {
            categoryName = categoryConfig.getShort().getName();
        } else if (charCount <= categoryConfig.getThresholds().getMediumMax()) {
            categoryName = categoryConfig.getMedium().getName();
        } else if (charCount <= categoryConfig.getThresholds().getLongMax()) {
            categoryName = categoryConfig.getLong().getName();
        } else {
            categoryName = categoryConfig.getDrama().getName();
        }
        
        return getCategoryByName(categoryName).orElse(null);
    }
    
    /**
     * Initialize default word-count-based categories if they don't exist using configuration
     */
    @Transactional
    public void initializeDefaultCategories() {
        // Format descriptions with actual threshold values
        String shortDesc = MessageFormat.format(categoryConfig.getShort().getDescription(), 
                categoryConfig.getThresholds().getShortMax());
        String mediumDesc = MessageFormat.format(categoryConfig.getMedium().getDescription(), 
                categoryConfig.getThresholds().getShortMax(), categoryConfig.getThresholds().getMediumMax());
        String longDesc = MessageFormat.format(categoryConfig.getLong().getDescription(), 
                categoryConfig.getThresholds().getMediumMax() + 1, categoryConfig.getThresholds().getLongMax());
        String dramaDesc = MessageFormat.format(categoryConfig.getDrama().getDescription(), 
                categoryConfig.getThresholds().getLongMax() + 1);
        
        createCategoryIfNotExists(categoryConfig.getShort().getName(), shortDesc, categoryConfig.getShort().getColor());
        createCategoryIfNotExists(categoryConfig.getMedium().getName(), mediumDesc, categoryConfig.getMedium().getColor());
        createCategoryIfNotExists(categoryConfig.getLong().getName(), longDesc, categoryConfig.getLong().getColor());
        createCategoryIfNotExists(categoryConfig.getDrama().getName(), dramaDesc, categoryConfig.getDrama().getColor());
        
        // Content-based categories
        createCategoryIfNotExists(categoryConfig.getTravelog().getName(), 
                categoryConfig.getTravelog().getDescription(), categoryConfig.getTravelog().getColor());
        createCategoryIfNotExists(categoryConfig.getPoetry().getName(), 
                categoryConfig.getPoetry().getDescription(), categoryConfig.getPoetry().getColor());
        createCategoryIfNotExists(categoryConfig.getInterestingAspects().getName(), 
                categoryConfig.getInterestingAspects().getDescription(), categoryConfig.getInterestingAspects().getColor());
        createCategoryIfNotExists(categoryConfig.getDevotionalSongs().getName(), 
                categoryConfig.getDevotionalSongs().getDescription(), categoryConfig.getDevotionalSongs().getColor());
        createCategoryIfNotExists(categoryConfig.getKian().getName(), 
                categoryConfig.getKian().getDescription(), categoryConfig.getKian().getColor());
    }
    
    private void createCategoryIfNotExists(String name, String description, String color) {
        if (!categoryRepository.existsByName(name)) {
            Category category = new Category(name, description);
            category.setColor(color);
            categoryRepository.save(category);
        }
    }
}
