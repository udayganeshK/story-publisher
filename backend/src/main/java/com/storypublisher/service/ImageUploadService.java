package com.storypublisher.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.storypublisher.config.ImageUploadConfig;

@Service
public class ImageUploadService {
    
    @Autowired
    private ImageUploadConfig imageUploadConfig;
    
    public boolean isImageUploadEnabled() {
        return imageUploadConfig.isEnabled();
    }
    
    public String uploadImage(MultipartFile file) throws IOException {
        if (!isImageUploadEnabled()) {
            throw new IllegalStateException("Image upload is disabled");
        }
        
        validateImage(file);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(imageUploadConfig.getStoragePath());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return URL
        return imageUploadConfig.getBaseUrl() + "/" + filename;
    }
    
    public void deleteImage(String imageUrl) {
        if (!isImageUploadEnabled() || imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        
        try {
            // Extract filename from URL
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(imageUploadConfig.getStoragePath()).resolve(filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (Exception e) {
            // Log error but don't throw - image deletion shouldn't break the main operation
            System.err.println("Failed to delete image: " + imageUrl + ", error: " + e.getMessage());
        }
    }
    
    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be empty");
        }
        
        // Check file size
        if (file.getSize() > imageUploadConfig.getMaxSizeBytes()) {
            throw new IllegalArgumentException("Image file size exceeds maximum allowed size of " + imageUploadConfig.getMaxSize());
        }
        
        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(imageUploadConfig.getAllowedTypesArray()).contains(contentType)) {
            throw new IllegalArgumentException("Invalid image type. Allowed types: " + imageUploadConfig.getAllowedTypes());
        }
    }
    
    public boolean isValidImageUrl(String imageUrl) {
        return imageUrl != null && 
               !imageUrl.isEmpty() && 
               imageUrl.startsWith(imageUploadConfig.getBaseUrl());
    }
}
