// ============================
// SERVICE: StationService.java
// ============================
package co.ke.finsis.service;

import co.ke.finsis.entity.*;
import co.ke.finsis.payload.*;
import co.ke.finsis.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;
    private final FileStorageService fileStorageService;

    public StationDto createStation(StationDto dto, MultipartFile file) {
        if ("Main Office".equalsIgnoreCase(dto.getLevelType())) {
            stationRepository.findByLevelType("Main Office").ifPresent(existing -> {
                throw new RuntimeException("Only one Main Office is allowed.");
            });
        }

        if (file != null && !file.isEmpty()) {
            String fileName = fileStorageService.saveFileToServer(file, "stations");
            String fileUrl = fileStorageService.getFileUrl("stations", fileName);

            dto.setDocument(DocumentUploadDto.builder()
                    .fileName(fileName)
                    .fileUrl(fileUrl)
                    .contentType(file.getContentType())
                    .build());
        }

        Station station = mapToEntity(dto);
        return mapToDto(stationRepository.save(station));
    }


    public StationDto getStation(Long id) {
        return mapToDto(stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found")));
    }

    public List<StationDto> getAllStations() {
        return stationRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public StationDto updateStation(Long id, StationDto dto, MultipartFile file) {
        Station existingStation = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found with ID: " + id));

        if ("Main Office".equalsIgnoreCase(dto.getLevelType())) {
            stationRepository.findByLevelType("Main Office")
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new RuntimeException("Only one Main Office is allowed.");
                    });
        }

        // Handle document update
        if (file != null && !file.isEmpty()) {
            // Delete old file if it exists
            if (existingStation.getDocument() != null && existingStation.getDocument().getFileName() != null) {
                fileStorageService.deleteFileFromServer("stations", existingStation.getDocument().getFileName());
            }
            // Save new file
            String newFileName = fileStorageService.saveFileToServer(file, "stations");
            String newFileUrl = fileStorageService.getFileUrl("stations", newFileName);

            // Update document details in the DTO
            dto.setDocument(DocumentUploadDto.builder()
                    .fileName(newFileName)
                    .fileUrl(newFileUrl)
                    .contentType(file.getContentType())
                    .build());
        } else if (dto.getDocument() == null || (dto.getDocument().getFileName() == null && dto.getDocument().getFileUrl() == null)) {
            // If no new file is provided AND the DTO explicitly clears the document,
            // then remove the existing document.
            if (existingStation.getDocument() != null) {
                fileStorageService.deleteFileFromServer("stations", existingStation.getDocument().getFileName());
            }
            existingStation.setDocument(null); // Clear the document reference
        } else if (dto.getDocument() != null && existingStation.getDocument() != null &&
                   !dto.getDocument().getFileName().equals(existingStation.getDocument().getFileName())) {
            // This case handles if the frontend sends a *different* existing file,
            // which might mean a file was swapped without a new upload.
            // For now, we'll assume a new file is sent or the existing one is kept/removed.
            // If the DTO contains document info, and no file is uploaded, keep the existing document info
            // provided it's still present in the DTO.
            dto.setDocument(existingStation.getDocument() != null ? mapToDto(existingStation).getDocument() : null);
        } else if (file == null || file.isEmpty()) {
            // If no new file is uploaded and the DTO doesn't clear the document,
            // retain the existing document details.
            dto.setDocument(existingStation.getDocument() != null ? mapToDto(existingStation).getDocument() : null);
        }


        // Map DTO to entity, ensuring the ID is set for update
        Station updatedStation = mapToEntity(dto);
        updatedStation.setId(id); // Ensure the ID is set for the update operation on the existing entity

        // If a new file was uploaded, the DTO's document field is already updated.
        // If no new file, and the DTO implies keeping the old file, ensure it's set.
        // If the DTO explicitly sets document to null, it will clear it.
        // Otherwise, if no file update and DTO doesn't explicitly null out document, keep existing.
        if (file == null || file.isEmpty()) {
            if (dto.getDocument() != null && (dto.getDocument().getFileName() != null || dto.getDocument().getFileUrl() != null)) {
                // If DTO has document info, use it (e.g., if re-using an existing file without re-upload)
                updatedStation.setDocument(mapDocumentUploadDtoToEntity(dto.getDocument()));
            } else {
                // If DTO explicitly clears document or has no document info, and no file is uploaded
                updatedStation.setDocument(null);
            }
        }


        return mapToDto(stationRepository.save(updatedStation));
    }


    public void deleteStation(Long id) {
        // Optional: Implement logic to delete associated file when station is deleted
        stationRepository.findById(id).ifPresent(station -> {
            if (station.getDocument() != null && station.getDocument().getFileName() != null) {
                fileStorageService.deleteFileFromServer("stations", station.getDocument().getFileName());
            }
            stationRepository.delete(station);
        });
    }

    private StationDto mapToDto(Station station) {
        return StationDto.builder()
                .id(station.getId())
                .stationName(station.getStationName())
                .levelType(station.getLevelType())
                .parentId(station.getParent() != null ? station.getParent().getId() : null)
                .location(LocationDto.builder()
                        .county(station.getLocation().getCounty())
                        .subCounty(station.getLocation().getSubCounty())
                        .ward(station.getLocation().getWard())
                        .gps(station.getLocation().getGps())
                        .build())
                .economicActivities(station.getEconomicActivities())
                .electricity(station.getElectricity())
                .internet(station.getInternet())
                .roadAccess(station.getRoadAccess())
                .document(station.getDocument() != null ? DocumentUploadDto.builder()
                        .fileName(station.getDocument().getFileName())
                        .fileUrl(station.getDocument().getFileUrl())
                        .contentType(station.getDocument().getContentType())
                        .build() : null)
                .build();
    }

    private Station mapToEntity(StationDto dto) {
        Station parent = null;
        if (dto.getParentId() != null) {
            parent = stationRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent station not found"));
        }

        return Station.builder()
                .id(dto.getId())
                .stationName(dto.getStationName())
                .levelType(dto.getLevelType())
                .parent(parent)
                .location(Location.builder()
                        .county(dto.getLocation().getCounty())
                        .subCounty(dto.getLocation().getSubCounty())
                        .ward(dto.getLocation().getWard())
                        .gps(dto.getLocation().getGps())
                        .build())
                .economicActivities(dto.getEconomicActivities())
                .electricity(dto.getElectricity())
                .internet(dto.getInternet())
                .roadAccess(dto.getRoadAccess())
                .document(dto.getDocument() != null ? mapDocumentUploadDtoToEntity(dto.getDocument()) : null)
                .build();
    }

    // Helper method to map DocumentUploadDto to DocumentUpload entity
    private DocumentUpload mapDocumentUploadDtoToEntity(DocumentUploadDto dto) {
        return DocumentUpload.builder()
                .fileName(dto.getFileName())
                .fileUrl(dto.getFileUrl())
                .contentType(dto.getContentType())
                .build();
    }
}