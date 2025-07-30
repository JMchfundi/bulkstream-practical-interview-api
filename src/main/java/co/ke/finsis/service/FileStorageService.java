package co.ke.finsis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // Import for logging

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    private String fileServingBaseUrl = "api/files/view/";

    public String saveFileToServer(MultipartFile file, String subDir) {
        try {
            // Enforce file size limit: 100KB = 102400 bytes
            final long MAX_FILE_SIZE = 100 * 1024; // 100 KB
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("File size exceeds maximum allowed limit of 100KB.");
            }

            Path basePath = Paths.get(System.getProperty("user.dir"), uploadDir);
            Path directoryPath = basePath.resolve(subDir);

            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            String originalFileName = file.getOriginalFilename();
            String cleanFileName = originalFileName.replaceAll("[^a-zA-Z0-9.\\-]", "_");
            String uniqueFileName = System.currentTimeMillis() + "_" + cleanFileName;

            Path filePath = directoryPath.resolve(uniqueFileName);

            file.transferTo(filePath.toFile());
            logger.info("File saved: {}/{}", subDir, uniqueFileName); // Logging

            return uniqueFileName;

        } catch (IllegalArgumentException e) {
            logger.warn("File rejected due to size: {}", e.getMessage());
            throw e; // rethrow for controller to catch

        } catch (IOException e) {
            logger.error("Error saving file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save file to server: " + e.getMessage(), e);
        }
    }

    public String getFileUrl(String subDir, String fileName) {
        // Construct the URL path for accessing the file via HTTP
        // This assumes your Spring Boot app is configured to serve static content from
        // 'uploadDir'
        // under the 'fileServingBaseUrl' path (e.g., /uploads/stations/your_file.jpg)
        return fileServingBaseUrl + subDir + "/" + fileName;
    }

    // --- NEW METHOD FOR FILE REVIEW/DOWNLOAD ---
    public Resource loadFileAsResource(String subDir, String fileName) {
        try {
            Path basePath = Paths.get(System.getProperty("user.dir"), uploadDir);
            Path filePath = basePath.resolve(subDir).resolve(fileName).normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + fileName);
            }
        } catch (MalformedURLException e) {
            logger.error("Error creating file URL: {}", e.getMessage(), e);
            throw new RuntimeException("File not found: " + fileName, e);
        } catch (IOException e) { // Catch general IO exceptions for directory traversal issues etc.
            logger.error("Error accessing file: {}", e.getMessage(), e);
            throw new RuntimeException("Error accessing file: " + fileName, e);
        }
    }

    public void deleteFileFromServer(String subDir, String fileName) {
        try {
            Path basePath = Paths.get(System.getProperty("user.dir"), uploadDir);
            Path filePath = basePath.resolve(subDir).resolve(fileName);

            if (Files.exists(filePath) && Files.isReadable(filePath)) {
                Files.delete(filePath);
                logger.info("Deleted file: {}", filePath.toString()); // Using logger
            } else {
                logger.warn("File not found or not readable for deletion: {}", filePath.toString()); // Using logger
            }
        } catch (NoSuchFileException e) {
            logger.warn("Attempted to delete a file that does not exist: {}", e.getMessage()); // Using logger
        } catch (DirectoryNotEmptyException e) {
            logger.error("Failed to delete file (directory not empty - expected file): {}", e.getMessage(), e); // Using
                                                                                                                // logger
            throw new RuntimeException("Failed to delete file due to directory not empty.", e);
        } catch (IOException e) {
            logger.error("Error deleting file: {}", e.getMessage(), e); // Using logger
            throw new RuntimeException("Failed to delete file from server: " + e.getMessage(), e);
        }
    }
}