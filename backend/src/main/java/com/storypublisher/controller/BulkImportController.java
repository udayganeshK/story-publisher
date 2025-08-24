package com.storypublisher.controller;

import com.storypublisher.model.ImportJob;
import com.storypublisher.model.User;
import com.storypublisher.service.FileUploadService;
import com.storypublisher.repository.ImportJobRepository;
import com.storypublisher.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bulk-import")
@CrossOrigin(origins = {"http://localhost:3000", "https://yourdomain.com"})
public class BulkImportController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private ImportJobRepository importJobRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            // Get user ID from JWT token
            String userId = (String) request.getAttribute("userId");
            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            // Get user entity
            Optional<User> userOpt = userRepository.findById(Long.parseLong(userId));
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "User not found");
                return ResponseEntity.status(401).body(response);
            }
            User user = userOpt.get();

            // Validate file
            if (file.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".zip")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Only ZIP files are supported");
                return ResponseEntity.badRequest().body(response);
            }

            // Create import job and start async processing
            ImportJob job = fileUploadService.processUploadedFile(user, file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jobId", job.getId().toString());
            response.put("message", "File upload started");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable Long jobId) {
        try {
            Optional<ImportJob> jobOpt = importJobRepository.findById(jobId);
            if (jobOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Job not found");
                return ResponseEntity.status(404).body(response);
            }

            ImportJob job = jobOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", job.getStatus().toString());
            response.put("progress", job.getProgressPercentage());
            response.put("totalFiles", job.getTotalDocuments());
            response.put("processedFiles", job.getProcessedDocuments());
            response.put("createdStories", job.getSuccessfulImports());
            response.put("failedFiles", job.getFailedImports());
            response.put("errorMessage", job.getErrorMessage());
            response.put("createdAt", job.getCreatedAt());
            response.put("completedAt", job.getCompletedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getHistory(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            // Get user entity
            Optional<User> userOpt = userRepository.findById(Long.parseLong(userId));
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "User not found");
                return ResponseEntity.status(401).body(response);
            }
            User user = userOpt.get();

            List<ImportJob> jobs = importJobRepository.findByUserOrderByCreatedAtDesc(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jobs", jobs);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/cancel/{jobId}")
    public ResponseEntity<Map<String, Object>> cancelJob(@PathVariable Long jobId, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            // Get user entity
            Optional<User> userOpt = userRepository.findById(Long.parseLong(userId));
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "User not found");
                return ResponseEntity.status(401).body(response);
            }
            User user = userOpt.get();

            // Use the service method to cancel the job
            boolean cancelled = fileUploadService.cancelImportJob(user, jobId);
            
            Map<String, Object> response = new HashMap<>();
            if (cancelled) {
                response.put("success", true);
                response.put("message", "Job cancelled successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "Job not found or cannot be cancelled");
                return ResponseEntity.status(400).body(response);
            }

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
