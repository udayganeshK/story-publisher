package com.storypublisher.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.storypublisher.model.ImportJob;
import com.storypublisher.model.User;
import com.storypublisher.repository.UserRepository;
import com.storypublisher.service.FileUploadBulkImportService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:30002", "http://localhost:3000"})
public class AdminController {

    @Autowired
    private FileUploadBulkImportService fileUploadService;

    @Autowired
    private UserRepository userRepository;

    // Custom MultipartFile implementation for byte arrays
    private static class CustomMultipartFile implements MultipartFile {
        private final String name;
        private final byte[] content;

        public CustomMultipartFile(String name, byte[] content) {
            this.name = name;
            this.content = content;
        }

        @Override
        public String getName() { return "file"; }

        @Override
        public String getOriginalFilename() { return name; }

        @Override
        public String getContentType() { return "application/zip"; }

        @Override
        public boolean isEmpty() { return content.length == 0; }

        @Override
        public long getSize() { return content.length; }

        @Override
        public byte[] getBytes() { return content; }

        @Override
        public InputStream getInputStream() { return new ByteArrayInputStream(content); }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), content);
        }
    }

    @PostMapping("/bulk-import-from-file")
    public ResponseEntity<Map<String, Object>> bulkImportFromFile(
            @RequestParam("userEmail") String userEmail,
            @RequestParam("filePath") String filePath) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Find user by email
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("error", "User not found with email: " + userEmail);
                return ResponseEntity.status(404).body(response);
            }
            
            User user = userOpt.get();
            
            // Check if file exists
            File file = new File(filePath);
            if (!file.exists()) {
                response.put("success", false);
                response.put("error", "File not found: " + filePath);
                return ResponseEntity.status(404).body(response);
            }
            
            // Read file into byte array
            byte[] fileData = Files.readAllBytes(file.toPath());
            
            // Create a custom implementation of MultipartFile
            CustomMultipartFile multipartFile = new CustomMultipartFile(file.getName(), fileData);
            
            // Process the file
            ImportJob job = fileUploadService.createImportJob(user, multipartFile);
            
            response.put("success", true);
            response.put("jobId", job.getId());
            response.put("message", "Bulk import started successfully");
            response.put("user", user.getUsername());
            response.put("totalFiles", job.getTotalDocuments());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error starting bulk import: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
