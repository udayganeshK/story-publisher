package com.storypublisher.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.storypublisher.service.CategoryService;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private CategoryService categoryService;
    
    @Override
    public void run(String... args) throws Exception {
        // Initialize default word-count-based categories
        categoryService.initializeDefaultCategories();
    }
}
