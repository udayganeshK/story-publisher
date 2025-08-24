package com.storypublisher.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.storypublisher.model.ImportJob;
import com.storypublisher.model.Story;
import com.storypublisher.model.StoryStatus;
import com.storypublisher.model.User;
import com.storypublisher.repository.ImportJobRepository;
import com.storypublisher.repository.StoryRepository;

@Service
public class FileUploadBulkImportService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadBulkImportService.class);
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final String[] SUPPORTED_EXTENSIONS = {".txt", ".docx", ".doc"};

    @Autowired
    private ImportJobRepository importJobRepository;

    @Autowired
    private StoryRepository storyRepository;

    public static class DocumentFile {
        private String filename;
        private String content;
        private String extension;

        public DocumentFile(String filename, String content, String extension) {
            this.filename = filename;
            this.content = content;
            this.extension = extension;
        }

        // Getters
        public String getFilename() { return filename; }
        public String getContent() { return content; }
        public String getExtension() { return extension; }
    }

    /**
     * Create import job and process uploaded zip file
     */
    @Transactional
    public ImportJob createImportJob(User user, MultipartFile zipFile) throws Exception {
        // Validate file
        validateZipFile(zipFile);

        // Check if user already has a running import job
        long runningJobs = importJobRepository.countRunningJobsByUser(user);
        if (runningJobs > 0) {
            throw new IllegalStateException("You already have a running import job. Please wait for it to complete.");
        }

        // Extract and count documents from zip
        List<DocumentFile> documents = extractDocumentsFromZip(zipFile);
        
        if (documents.isEmpty()) {
            throw new IllegalArgumentException("No supported document files found in the zip. Supported formats: .txt, .doc, .docx");
        }

        // Create import job
        ImportJob importJob = new ImportJob(user, documents.size());
        importJob.setUploadedFilename(zipFile.getOriginalFilename());
        importJob.setFileSize(zipFile.getSize());
        importJob = importJobRepository.save(importJob);

        logger.info("🚀 Created import job {} for user {} with {} documents from file: {}", 
                   importJob.getId(), user.getEmail(), documents.size(), zipFile.getOriginalFilename());

        // Start processing asynchronously
        startImportProcess(importJob.getId(), documents);

        return importJob;
    }

    /**
     * Validate uploaded zip file
     */
    private void validateZipFile(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 50MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("File must be a ZIP archive");
        }
    }

    /**
     * Extract documents from zip file
     */
    private List<DocumentFile> extractDocumentsFromZip(MultipartFile zipFile) throws IOException {
        List<DocumentFile> documents = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String filename = entry.getName();
                    String extension = getFileExtension(filename);

                    if (isSupportedFormat(extension)) {
                        try {
                            String content = extractContentFromEntry(zis, extension);
                            if (content != null && !content.trim().isEmpty()) {
                                documents.add(new DocumentFile(filename, content, extension));
                                logger.debug("📄 Extracted document: {}", filename);
                            }
                        } catch (Exception e) {
                            logger.warn("⚠️ Failed to extract content from {}: {}", filename, e.getMessage());
                        }
                    }
                }
                zis.closeEntry();
            }
        }

        return documents;
    }

    /**
     * Extract content from zip entry based on file type
     */
    private String extractContentFromEntry(ZipInputStream zis, String extension) throws IOException {
        byte[] buffer = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead;
        while ((bytesRead = zis.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }

        byte[] fileContent = baos.toByteArray();

        switch (extension.toLowerCase()) {
            case ".txt":
                return new String(fileContent, StandardCharsets.UTF_8);
            case ".docx":
                return extractFromDocx(new ByteArrayInputStream(fileContent));
            case ".doc":
                return extractFromDoc(new ByteArrayInputStream(fileContent));
            default:
                return null;
        }
    }

    /**
     * Extract text from DOCX file
     */
    private String extractFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    /**
     * Extract text from DOC file
     */
    private String extractFromDoc(InputStream inputStream) throws IOException {
        try (HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    /**
     * Check if file format is supported
     */
    private boolean isSupportedFormat(String extension) {
        for (String supported : SUPPORTED_EXTENSIONS) {
            if (supported.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }

    /**
     * Start the import process asynchronously
     */
    @Async
    @Transactional
    public CompletableFuture<Void> startImportProcess(Long importJobId, List<DocumentFile> documents) {
        try {
            ImportJob importJob = importJobRepository.findById(importJobId)
                    .orElseThrow(() -> new IllegalArgumentException("Import job not found"));

            // Update status to running
            importJob.setStatus(ImportJob.ImportStatus.RUNNING);
            importJob.setStartedAt(LocalDateTime.now());
            importJobRepository.save(importJob);

            logger.info("📚 Starting import of {} documents for job {}", documents.size(), importJobId);

            List<String> errors = new ArrayList<>();

            // Process each document
            for (DocumentFile doc : documents) {
                try {
                    processDocument(importJob, doc);
                    importJob.incrementProcessed();
                    importJob.incrementSuccessful();
                    
                    // Update progress every 10 documents
                    if (importJob.getProcessedDocuments() % 10 == 0) {
                        importJobRepository.save(importJob);
                        logger.info("📈 Import progress: {}/{} documents processed", 
                                   importJob.getProcessedDocuments(), importJob.getTotalDocuments());
                    }

                } catch (Exception e) {
                    logger.error("❌ Failed to import document '{}': {}", doc.getFilename(), e.getMessage());
                    errors.add("Document '" + doc.getFilename() + "': " + e.getMessage());
                    importJob.incrementProcessed();
                    importJob.incrementFailed();
                }
            }

            // Complete the job
            importJob.setStatus(ImportJob.ImportStatus.COMPLETED);
            importJob.setCompletedAt(LocalDateTime.now());
            importJob.setErrors(errors);
            importJobRepository.save(importJob);

            logger.info("✅ Import job {} completed. Success: {}, Failed: {}", 
                       importJobId, importJob.getSuccessfulImports(), importJob.getFailedImports());

        } catch (Exception e) {
            logger.error("💥 Import job {} failed: {}", importJobId, e.getMessage(), e);
            handleImportJobFailure(importJobId, e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Process a single document and create a Story
     */
    private void processDocument(ImportJob importJob, DocumentFile doc) throws Exception {
        try {
            String content = doc.getContent();
            if (content == null || content.trim().isEmpty()) {
                throw new Exception("Document is empty or could not extract content");
            }

            // Extract title from filename
            String title = extractTitleFromFilename(doc.getFilename());

            // Generate excerpt (first 200 characters)
            String excerpt = generateExcerpt(content);

            // Check if story with same title already exists for this user
            if (storyRepository.existsByTitleAndAuthor(title, importJob.getUser())) {
                // Append timestamp to make title unique
                title = title + " (Imported " + LocalDateTime.now().toString().substring(0, 16) + ")";
            }

            // Create Story entity
            Story story = new Story();
            story.setTitle(title);
            story.setContent(content);
            story.setExcerpt(excerpt);
            story.setAuthor(importJob.getUser());
            story.setStatus(StoryStatus.DRAFT); // Import as drafts by default
            story.setCreatedAt(LocalDateTime.now());
            story.setUpdatedAt(LocalDateTime.now());

            // Save the story
            storyRepository.save(story);

            logger.debug("📝 Successfully imported story: '{}'", title);

        } catch (Exception e) {
            logger.error("❌ Error processing document '{}': {}", doc.getFilename(), e.getMessage());
            throw new Exception("Failed to process document '" + doc.getFilename() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Extract title from filename
     */
    private String extractTitleFromFilename(String filename) {
        // Remove path separators (in case of nested folders in zip)
        String name = filename.substring(filename.lastIndexOf('/') + 1);
        name = name.substring(name.lastIndexOf('\\') + 1);
        
        // Remove file extension
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            name = name.substring(0, lastDot);
        }

        // Clean up the title
        name = name.trim();
        
        // Ensure title is not too long
        if (name.length() > 100) {
            name = name.substring(0, 97) + "...";
        }

        return name.isEmpty() ? "Untitled Story" : name;
    }

    /**
     * Generate excerpt from content
     */
    private String generateExcerpt(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "No content available";
        }

        // Remove extra whitespace and newlines
        String cleaned = content.replaceAll("\\s+", " ").trim();
        
        if (cleaned.length() <= 200) {
            return cleaned;
        }

        // Find a good breaking point near 200 characters
        String excerpt = cleaned.substring(0, 200);
        int lastSpace = excerpt.lastIndexOf(' ');
        if (lastSpace > 150) {
            excerpt = excerpt.substring(0, lastSpace);
        }

        return excerpt + "...";
    }

    /**
     * Handle import job failure
     */
    @Transactional
    public void handleImportJobFailure(Long importJobId, String errorMessage) {
        try {
            ImportJob importJob = importJobRepository.findById(importJobId).orElse(null);
            if (importJob != null) {
                importJob.setStatus(ImportJob.ImportStatus.FAILED);
                importJob.setErrorMessage(errorMessage);
                importJob.setCompletedAt(LocalDateTime.now());
                importJobRepository.save(importJob);
            }
        } catch (Exception e) {
            logger.error("Failed to update import job failure status: {}", e.getMessage());
        }
    }

    /**
     * Get import job status for user
     */
    public ImportJob getImportJobStatus(User user, Long importJobId) {
        return importJobRepository.findByIdAndUser(importJobId, user).orElse(null);
    }

    /**
     * Get all import jobs for user
     */
    public List<ImportJob> getUserImportJobs(User user) {
        return importJobRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Cancel an import job
     */
    @Transactional
    public boolean cancelImportJob(User user, Long importJobId) {
        ImportJob importJob = importJobRepository.findByIdAndUser(importJobId, user).orElse(null);
        if (importJob != null && !importJob.isCompleted()) {
            importJob.setStatus(ImportJob.ImportStatus.CANCELLED);
            importJob.setCompletedAt(LocalDateTime.now());
            importJobRepository.save(importJob);
            return true;
        }
        return false;
    }
}
