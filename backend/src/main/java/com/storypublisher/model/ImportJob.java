package com.storypublisher.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "import_jobs")
public class ImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportStatus status;

    @Column(name = "total_documents")
    private Integer totalDocuments;

    @Column(name = "processed_documents")
    private Integer processedDocuments = 0;

    @Column(name = "successful_imports")
    private Integer successfulImports = 0;

    @Column(name = "failed_imports")
    private Integer failedImports = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @ElementCollection
    @CollectionTable(name = "import_job_errors")
    private List<String> errors;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "uploaded_filename")
    private String uploadedFilename;

    @Column(name = "file_size")
    private Long fileSize;

    // Constructors
    public ImportJob() {}

    public ImportJob(User user, Integer totalDocuments) {
        this.user = user;
        this.totalDocuments = totalDocuments;
        this.status = ImportStatus.PENDING;
    }

    // Enums
    public enum ImportStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ImportStatus getStatus() {
        return status;
    }

    public void setStatus(ImportStatus status) {
        this.status = status;
    }

    public Integer getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(Integer totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public Integer getProcessedDocuments() {
        return processedDocuments;
    }

    public void setProcessedDocuments(Integer processedDocuments) {
        this.processedDocuments = processedDocuments;
    }

    public Integer getSuccessfulImports() {
        return successfulImports;
    }

    public void setSuccessfulImports(Integer successfulImports) {
        this.successfulImports = successfulImports;
    }

    public Integer getFailedImports() {
        return failedImports;
    }

    public void setFailedImports(Integer failedImports) {
        this.failedImports = failedImports;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getUploadedFilename() {
        return uploadedFilename;
    }

    public void setUploadedFilename(String uploadedFilename) {
        this.uploadedFilename = uploadedFilename;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    // Helper methods
    public void incrementProcessed() {
        this.processedDocuments++;
    }

    public void incrementSuccessful() {
        this.successfulImports++;
    }

    public void incrementFailed() {
        this.failedImports++;
    }

    public double getProgressPercentage() {
        if (totalDocuments == null || totalDocuments == 0) {
            return 0.0;
        }
        return (double) processedDocuments / totalDocuments * 100.0;
    }

    public boolean isCompleted() {
        return status == ImportStatus.COMPLETED || status == ImportStatus.FAILED || status == ImportStatus.CANCELLED;
    }
}
