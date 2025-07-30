package co.ke.finsis.controller;

import co.ke.finsis.payload.OfficerRegistrationDto;
import co.ke.finsis.service.OfficerRegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/officers")
@RequiredArgsConstructor // For injecting OfficerRegistrationService
public class OfficerRegistrationController {

    private final OfficerRegistrationService officerService;
    private final ObjectMapper objectMapper; // Inject ObjectMapper

    // Constructor injection for ObjectMapper (good practice with @RequiredArgsConstructor)
    // If you explicitly provide a constructor, @RequiredArgsConstructor will not generate one.
    // For final fields, Lombok's @RequiredArgsConstructor handles this automatically.
    // public OfficerRegistrationController(OfficerRegistrationService officerService, ObjectMapper objectMapper) {
    //     this.officerService = officerService;
    //     this.objectMapper = objectMapper;
    // }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<OfficerRegistrationDto> createOfficer(
            @RequestPart("officer") String officerJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        try {
            OfficerRegistrationDto request = objectMapper.readValue(officerJson, OfficerRegistrationDto.class);
            OfficerRegistrationDto savedOfficerDto = officerService.createOfficer(request, files);
            return new ResponseEntity<>(savedOfficerDto, HttpStatus.CREATED);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid officer data or file upload issue: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating officer: " + e.getMessage(), e);
        }
    }

    @GetMapping
    public ResponseEntity<List<OfficerRegistrationDto>> getAllOfficers() {
        List<OfficerRegistrationDto> officers = officerService.getAllOfficers();
        return ResponseEntity.ok(officers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfficerRegistrationDto> getOfficerById(@PathVariable Long id) {
        try {
            OfficerRegistrationDto officerDto = officerService.getOfficerById(id);
            return ResponseEntity.ok(officerDto);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    // --- UPDATED PUT MAPPING TO HANDLE FILES ---
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<OfficerRegistrationDto> updateOfficer(
            @PathVariable Long id,
            @RequestPart("officer") String officerJson, // Officer data as JSON string
            @RequestPart(value = "files", required = false) List<MultipartFile> files) { // Optional files

        try {
            OfficerRegistrationDto request = objectMapper.readValue(officerJson, OfficerRegistrationDto.class);
            // You will need to modify the service method to accept files for update
            OfficerRegistrationDto updatedOfficerDto = officerService.updateOfficer(id, request, files);
            return ResponseEntity.ok(updatedOfficerDto);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid officer data or file upload issue: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e); // Or specific internal server error
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOfficer(@PathVariable Long id) {
        try {
            officerService.deleteOfficer(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
}