package com.storypublisher.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.storypublisher.model.Story;
import com.storypublisher.model.StoryStatus;
import com.storypublisher.model.User;
import com.storypublisher.repository.StoryRepository;
import com.storypublisher.repository.UserRepository;

@Component
public class ConditionalBulkImporter implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoryRepository storyRepository;

    private static final String ZIP_FILE_PATH = "/Users/udaykanteti/Workspaces/StoryPublisher/bulk-stories.zip";
    private static final String USER_EMAIL = "udayganesh.kanteti@gmail.com";

    @Override
    public void run(String... args) throws Exception {
        // Only run if the specific argument is passed
        if (args.length > 0 && "import-bulk-stories".equals(args[0])) {
            System.out.println("Starting conditional bulk story import...");
            importStories();
        }
    }

    @Transactional
    public void importStories() {
        try {
            System.out.println("Starting direct bulk story import...");
            
            // Find user by email
            User user = userRepository.findByEmail(USER_EMAIL)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + USER_EMAIL));
            
            System.out.println("Found user: " + user.getUsername() + " (ID: " + user.getId() + ")");
            
            // Process ZIP file
            processZipFile(user);
            
            System.out.println("Direct bulk import completed!");
            
        } catch (Exception e) {
            System.err.println("Error during bulk import: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processZipFile(User user) throws IOException {
        File zipFile = new File(ZIP_FILE_PATH);
        if (!zipFile.exists()) {
            throw new RuntimeException("ZIP file not found: " + ZIP_FILE_PATH);
        }

        int processedCount = 0;
        int successCount = 0;
        int errorCount = 0;

        try (FileInputStream fis = new FileInputStream(zipFile);
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".docx")) {
                    try {
                        processDocxEntry(zis, entry, user);
                        successCount++;
                        System.out.println("✓ Processed: " + entry.getName());
                    } catch (Exception e) {
                        errorCount++;
                        System.err.println("✗ Error processing " + entry.getName() + ": " + e.getMessage());
                    }
                    processedCount++;
                    
                    if (processedCount % 10 == 0) {
                        System.out.println("Progress: " + processedCount + " files processed");
                    }
                }
            }
        }

        System.out.println("\n=== Import Summary ===");
        System.out.println("Total processed: " + processedCount);
        System.out.println("Successful: " + successCount);
        System.out.println("Errors: " + errorCount);
    }

    private void processDocxEntry(ZipInputStream zis, ZipEntry entry, User user) throws IOException {
        // Create a temporary file to process the DOCX
        Path tempFile = Files.createTempFile("story_", ".docx");
        try {
            Files.copy(zis, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // Extract text from DOCX
            try (FileInputStream fis = new FileInputStream(tempFile.toFile());
                 XWPFDocument document = new XWPFDocument(fis);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                
                String textContent = extractor.getText().trim();
                
                // Skip if content is empty
                if (textContent.isEmpty()) {
                    System.out.println("Skipping empty file: " + entry.getName());
                    return;
                }
                
                // Extract title from filename
                String filename = entry.getName();
                String title = extractTitleFromFilename(filename);
                
                // Check if story with this title already exists for this user
                if (storyRepository.existsByTitleAndAuthor(title, user)) {
                    System.out.println("Story already exists, skipping: " + title);
                    return;
                }
                
                // Create and save story
                Story story = new Story();
                story.setTitle(title);
                story.setContent(textContent);
                story.setAuthor(user);
                story.setStatus(StoryStatus.DRAFT);
                story.setCreatedAt(java.time.LocalDateTime.now());
                story.setUpdatedAt(java.time.LocalDateTime.now());
                
                storyRepository.save(story);
                
                System.out.println("Created story: " + title + " (content length: " + textContent.length() + " chars)");
            }
        } finally {
            // Clean up temp file
            Files.deleteIfExists(tempFile);
        }
    }

    private String extractTitleFromFilename(String filename) {
        // Remove the "stories mine/" prefix and ".docx" suffix
        String title = filename;
        if (title.contains("/")) {
            title = title.substring(title.lastIndexOf("/") + 1);
        }
        if (title.toLowerCase().endsWith(".docx")) {
            title = title.substring(0, title.length() - 5);
        }
        
        // Clean up the title
        title = title.trim();
        if (title.endsWith("_")) {
            title = title.substring(0, title.length() - 1);
        }
        
        // Replace any problematic characters
        title = title.replaceAll("[\\p{Cntrl}]", ""); // Remove control characters
        
        // If title is empty or just symbols, use a default with timestamp
        if (title.isEmpty() || title.matches("^[^a-zA-Z0-9\\u0C00-\\u0C7F]+$")) {
            title = "Story " + System.currentTimeMillis();
        }
        
        // Limit title length
        if (title.length() > 190) {
            title = title.substring(0, 190) + "...";
        }
        
        return title;
    }
}
