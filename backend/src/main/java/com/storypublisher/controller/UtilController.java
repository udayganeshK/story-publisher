package com.storypublisher.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.storypublisher.util.DirectBulkImporter;

@RestController
@RequestMapping("/api/util")
@CrossOrigin(origins = "*")
public class UtilController {

    @Autowired
    private DirectBulkImporter directBulkImporter;

    @PostMapping("/import-stories")
    public ResponseEntity<Map<String, Object>> importStories() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            directBulkImporter.importStories();
            
            response.put("success", true);
            response.put("message", "Bulk import completed successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error during bulk import: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Util controller is working");
        return ResponseEntity.ok(response);
    }
}
