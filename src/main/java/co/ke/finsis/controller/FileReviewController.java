package co.ke.finsis.controller;

import co.ke.finsis.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest; // Use jakarta.servlet.http.HttpServletRequest for Spring Boot 3+
import java.io.IOException;

// You might need to adjust this import based on your Spring Boot version.
// For Spring Boot 3 and newer:
// import jakarta.servlet.http.HttpServletRequest;
// For Spring Boot 2 and older:
// import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/files") // A dedicated path for file operations
@RequiredArgsConstructor
public class FileReviewController {

    private final FileStorageService fileStorageService;

    /**
     * Serves a file for review/download.
     * The file's content type is determined dynamically.
     *
     * @param subDir The subdirectory where the file is located (e.g., "officers", "stations").
     * @param fileName The unique file name (e.g., "1678901234_document.pdf").
     * @param request HttpServletRequest to determine content type.
     * @return ResponseEntity containing the file as a Resource.
     */
    @GetMapping("/view/{subDir}/{fileName}")
    public ResponseEntity<Resource> viewFile(
            @PathVariable String subDir,
            @PathVariable String fileName,
            HttpServletRequest request) { // Injected to get content type

        Resource resource = null;
        try {
            resource = fileStorageService.loadFileAsResource(subDir, fileName);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Log the error but proceed with default content type
            System.err.println("Could not determine file type. Using default. " + ex.getMessage());
        }

        // Fallback to default content type if not determined
        if (contentType == null) {
            contentType = "application/octet-stream"; // Generic binary data
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"") // "inline" to view in browser, "attachment" to force download
                .body(resource);
    }

    /**
     * Forces the download of a file.
     * This is an alternative endpoint if you always want to force download.
     *
     * @param subDir The subdirectory where the file is located.
     * @param fileName The unique file name.
     * @param request HttpServletRequest to determine content type.
     * @return ResponseEntity containing the file as a Resource.
     */
    @GetMapping("/download/{subDir}/{fileName}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String subDir,
            @PathVariable String fileName,
            HttpServletRequest request) {

        Resource resource = null;
        try {
            resource = fileStorageService.loadFileAsResource(subDir, fileName);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            System.err.println("Could not determine file type. Using default. " + ex.getMessage());
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"") // "attachment" forces download
                .body(resource);
    }

    // Optional: If you need to map document URLs that already include subDir and fileName
    // e.g., if you store the full path 'officers/123_document.pdf' in your DB
    // You'd need to parse the subDir and fileName from the full path.
    // This example assumes you have subDir and fileName separately.
    /*
    @GetMapping("/by-full-path")
    public ResponseEntity<Resource> getFileByFullPath(@RequestParam String filePath, HttpServletRequest request) {
        // You'd need logic here to extract subDir and fileName from filePath
        // For example:
        // Path path = Paths.get(filePath);
        // String subDir = path.getParent().getFileName().toString();
        // String fileName = path.getFileName().toString();
        // Then call fileStorageService.loadFileAsResource(subDir, fileName);
        // ... rest of the logic
    }
    */
}