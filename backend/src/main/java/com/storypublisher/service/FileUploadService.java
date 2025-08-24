package com.storypublisher.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Autowired
    private ImportJobRepository importJobRepository;

    @Autowired
    private StoryRepository storyRepository;

    /**
     * Process uploaded zip file and create import job
     */
    @Transactional
    public ImportJob processUploadedFile(User user, MultipartFile file) throws Exception {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("Only ZIP files are supported");
        }

        // Check if user already has a running import job
        long runningJobs = importJobRepository.countRunningJobsByUser(user);
        if (runningJobs > 0) {
            throw new IllegalStateException("You already have a running import job. Please wait for it to complete.");
        }

        // Count documents in zip file
        int documentCount = countDocumentsInZip(file);
        if (documentCount == 0) {
            throw new IllegalArgumentException("No supported documents found in ZIP file. Supported formats: .docx, .doc, .pdf, .txt");
        }

        // Create import job
        ImportJob importJob = new ImportJob(user, documentCount);
        importJob = importJobRepository.save(importJob);

        logger.info("📁 Created import job {} for user {} with {} documents from file: {}", 
                   importJob.getId(), user.getEmail(), documentCount, file.getOriginalFilename());

        return importJob;
    }

    /**
     * Start processing the zip file asynchronously
     */
    @Async
    @Transactional
    public CompletableFuture<Void> processZipFile(Long importJobId, byte[] fileData) {
        try {
            ImportJob importJob = importJobRepository.findById(importJobId)
                    .orElseThrow(() -> new IllegalArgumentException("Import job not found"));

            // Update status to running
            importJob.setStatus(ImportJob.ImportStatus.RUNNING);
            importJob.setStartedAt(LocalDateTime.now());
            importJobRepository.save(importJob);

            logger.info("🚀 Starting zip file processing for job {}", importJobId);

            List<String> errors = new ArrayList<>();

            // Process zip file
            try (ZipInputStream zipInput = new ZipInputStream(new ByteArrayInputStream(fileData))) {
                ZipEntry entry;
                while ((entry = zipInput.getNextEntry()) != null) {
                    if (!entry.isDirectory() && isSupportedFile(entry.getName())) {
                        try {
                            processDocument(importJob, entry.getName(), zipInput);
                            importJob.incrementProcessed();
                            importJob.incrementSuccessful();

                            // Update progress every 5 documents
                            if (importJob.getProcessedDocuments() % 5 == 0) {
                                importJobRepository.save(importJob);
                                logger.info("📈 Import progress: {}/{} documents processed", 
                                           importJob.getProcessedDocuments(), importJob.getTotalDocuments());
                            }

                        } catch (Exception e) {
                            logger.error("❌ Failed to import document '{}': {}", entry.getName(), e.getMessage());
                            errors.add("Document '" + entry.getName() + "': " + e.getMessage());
                            importJob.incrementProcessed();
                            importJob.incrementFailed();
                        }
                    }
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
     * Process a single document from zip file
     */
    private void processDocument(ImportJob importJob, String fileName, ZipInputStream zipInput) throws Exception {
        try {
            // Read file content from zip
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = zipInput.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] fileData = baos.toByteArray();

            // Extract content based on file type
            String content = extractContentFromFile(fileName, fileData);
            
            if (content == null || content.trim().isEmpty()) {
                throw new Exception("Document is empty or could not extract content");
            }

            // Extract title from filename
            String title = extractTitleFromFileName(fileName);

            // Generate excerpt
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
            logger.error("❌ Error processing document '{}': {}", fileName, e.getMessage());
            throw new Exception("Failed to process document '" + fileName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Extract content from different file types
     */
    private String extractContentFromFile(String fileName, byte[] fileData) throws Exception {
        String lowerFileName = fileName.toLowerCase();
        
        try (InputStream inputStream = new ByteArrayInputStream(fileData)) {
            if (lowerFileName.endsWith(".docx")) {
                return extractFromDocx(inputStream);
            } else if (lowerFileName.endsWith(".doc")) {
                return extractFromDoc(inputStream);
            } else if (lowerFileName.endsWith(".pdf")) {
                return extractFromPdf(inputStream);
            } else if (lowerFileName.endsWith(".txt")) {
                return new String(fileData, "UTF-8");
            } else {
                throw new Exception("Unsupported file format: " + fileName);
            }
        }
    }

    /**
     * Extract text from DOCX file
     */
    private String extractFromDocx(InputStream inputStream) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    /**
     * Extract text from DOC file
     */
    private String extractFromDoc(InputStream inputStream) throws Exception {
        try (HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    /**
     * Extract text from PDF file
     */
    private String extractFromPdf(InputStream inputStream) throws Exception {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper textStripper = new PDFTextStripper();
            return textStripper.getText(document);
        }
    }

    /**
     * Count supported documents in zip file
     */
    private int countDocumentsInZip(MultipartFile file) throws Exception {
        int count = 0;
        try (ZipInputStream zipInput = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                if (!entry.isDirectory() && isSupportedFile(entry.getName())) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Check if file is supported
     */
    private boolean isSupportedFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".docx") || lower.endsWith(".doc") || 
               lower.endsWith(".pdf") || lower.endsWith(".txt");
    }

    /**
     * Extract title from filename
     */
    private String extractTitleFromFileName(String fileName) {
        // Remove path and extension
        String name = fileName;
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf("/") + 1);
        }
        if (name.contains("\\")) {
            name = name.substring(name.lastIndexOf("\\") + 1);
        }
        
        // Remove file extension
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            name = name.substring(0, lastDot);
        }

        // Clean up the name
        name = name.replaceAll("[_-]", " ").trim();
        
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
