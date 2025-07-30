package co.ke.finsis.service;

import co.ke.finsis.entity.*;
import co.ke.finsis.payload.*;
import co.ke.finsis.repository.DocumentUploadRepository;
import co.ke.finsis.repository.NextOfKinRepository;
import co.ke.finsis.repository.OfficerRegistrationRepository;
import co.ke.finsis.repository.StationRepository;
import co.ke.mail.services.MailService; // Will be used for direct call (though event listener is preferred)
import co.ke.tucode.systemuser.entities.TRES_User;
import co.ke.tucode.systemuser.entities.Role;
import co.ke.tucode.systemuser.repositories.Africana_UserRepository;
import co.ke.finsis.events.OfficerCreatedEvent; // New import for the event

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher; // New import for event publisher

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfficerRegistrationService {

    private final OfficerRegistrationRepository officerRepo;
    private final FileStorageService fileStorageService;
    private final DocumentUploadRepository documentUploadRepository;
    private final NextOfKinRepository nextOfKinRepository;
    private final StationRepository stationRepository;
    private final MailService mailService; // Still injected, but direct call is removed from createOfficer
    private final PasswordEncoder passwordEncoder;
    private final Africana_UserRepository africanaUserRepository;
    private final ApplicationEventPublisher eventPublisher; // Inject ApplicationEventPublisher

    /**
     * Creates a new officer based on the provided DTO and optional files.
     * Ensures atomic creation of officer, related entities, and system user.
     *
     * @param request The DTO containing officer registration details.
     * @param files   Optional list of MultipartFile for document uploads.
     * @return The DTO representation of the newly created officer.
     * @throws RuntimeException if the station is not found or other errors occur.
     */
    @Transactional // Ensures atomicity: all operations succeed or all roll back
    public OfficerRegistrationDto createOfficer(OfficerRegistrationDto request, List<MultipartFile> files) {
        OfficerRegistration officer = mapToEntity(request);

        // 1. Set Assigned Station
        if (request.getStationId() != null) {
            Station station = stationRepository.findById(request.getStationId())
                    .orElseThrow(() -> new RuntimeException("Station not found with ID: " + request.getStationId()));
            officer.setAssignedStation(station);
        }

        // 2. Persist Locations (Assuming Location is typically embedded or replaced as
        // a whole, no 'id' needed here for creation)
        if (request.getLocations() != null && !request.getLocations().isEmpty()) {
            List<Location> locations = request.getLocations().stream()
                    .map(locDto -> new Location(locDto.getCounty(), locDto.getSubCounty(), locDto.getWard(),
                            locDto.getGps()))
                    .collect(Collectors.toList());
            officer.setLocations(locations);
        } else {
            officer.setLocations(new ArrayList<>());
        }

        // 3. Persist Next of Kin (save individually, then set to officer)
        if (request.getNextOfKins() != null && !request.getNextOfKins().isEmpty()) {
            List<NextOfKin> savedNOKs = request.getNextOfKins().stream()
                    .map(nokDto -> new NextOfKin(null, nokDto.getName(), nokDto.getPhone(), nokDto.getRelationship()))
                    .map(nextOfKinRepository::save)
                    .collect(Collectors.toList());
            officer.setNextOfKins(savedNOKs);
        } else {
            officer.setNextOfKins(new ArrayList<>());
        }

        // 4. Handle Document Uploads and Metadata (save individually, then set to
        // officer)
        List<DocumentUpload> allDocuments = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            List<DocumentUpload> uploadedDocs = files.stream().map(file -> {
                String fileName = fileStorageService.saveFileToServer(file, "officers");
                String fileUrl = fileStorageService.getFileUrl("officers", fileName);
                DocumentUpload doc = new DocumentUpload(null, fileName, fileUrl, file.getContentType());
                return documentUploadRepository.save(doc);
            }).collect(Collectors.toList());
            allDocuments.addAll(uploadedDocs);
        }

        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            List<DocumentUpload> savedMetaDocs = request.getDocuments().stream()
                    .filter(docDto -> docDto.getFileName() != null && !docDto.getFileName().isEmpty())
                    .map(docDto -> {
                        DocumentUpload doc = new DocumentUpload(null, docDto.getFileName(), docDto.getFileUrl(),
                                docDto.getContentType());
                        return documentUploadRepository.save(doc);
                    })
                    .collect(Collectors.toList());
            allDocuments.addAll(savedMetaDocs);
        }
        officer.setDocuments(allDocuments);

        // 5. Save the OfficerRegistration entity FIRST
        OfficerRegistration savedOfficer = officerRepo.save(officer);

        // 6. Create and link System User ONLY AFTER Officer is successfully saved
        TRES_User systemUser = new TRES_User();
        systemUser.setUsername(savedOfficer.getEmail());
        systemUser.setEmail(savedOfficer.getEmail());
        systemUser.setPassword(passwordEncoder.encode("Password@2906")); // Consider generating a random password
        systemUser.setUser_signature("OFFICER");
        systemUser.setRole(Role.OFFICER);

        TRES_User savedAfricanaUser = africanaUserRepository.save(systemUser);
        savedOfficer.setSystemUser(savedAfricanaUser);

        // 7. Re-save officer to update the systemUser link
        officerRepo.save(savedOfficer);

        // 8. Publish event to send credentials AFTER transaction commits
        eventPublisher.publishEvent(new OfficerCreatedEvent(this, savedOfficer.getId()));

        return mapToDto(savedOfficer);
    }

    /**
     * Retrieves an officer by ID.
     *
     * @param id The ID of the officer to retrieve.
     * @return The DTO representation of the retrieved officer.
     * @throws RuntimeException if the officer is not found.
     */
    public OfficerRegistrationDto getOfficerById(Long id) {
        OfficerRegistration officer = officerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Officer not found with ID: " + id));
        return mapToDto(officer);
    }

    /**
     * Retrieves all officers.
     *
     * @return A list of DTO representations of all officers.
     */
    public List<OfficerRegistrationDto> getAllOfficers() {
        return officerRepo.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing officer's details.
     * This method handles updates for basic fields, associated station,
     * next of kin, locations, documents (both new uploads and existing metadata),
     * and the linked system user.
     *
     * @param id      The ID of the officer to update.
     * @param request The DTO containing updated officer details.
     * @param files   Optional list of new MultipartFiles to be uploaded.
     * @return The DTO representation of the updated officer.
     * @throws RuntimeException if the officer or assigned station is not found.
     */
    @Transactional
    public OfficerRegistrationDto updateOfficer(Long id, OfficerRegistrationDto request, List<MultipartFile> files) {
        OfficerRegistration existingOfficer = officerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Officer not found with ID: " + id));

        // --- Update basic fields ---
        existingOfficer.setFullName(request.getFullName());
        existingOfficer.setEmail(request.getEmail());
        existingOfficer.setPhoneNumber(request.getPhoneNumber());
        existingOfficer.setIdNumber(request.getIdNumber());
        existingOfficer.setDob(request.getDob());
        existingOfficer.setGender(request.getGender());
        existingOfficer.setBankDetails(request.getBankDetails());

        // --- Update Station ---
        if (request.getStationId() != null) {
            Station station = stationRepository.findById(request.getStationId())
                    .orElseThrow(() -> new RuntimeException("Station not found with ID: " + request.getStationId()));
            existingOfficer.setAssignedStation(station);
        } else {
            existingOfficer.setAssignedStation(null); // Dissociate if stationId is null
        }

        // --- Update Locations (Replace strategy) ---
        if (request.getLocations() != null) {
            existingOfficer.getLocations().clear(); // Clear existing locations
            List<Location> updatedLocations = request.getLocations().stream()
                    .map(locDto -> new Location(locDto.getCounty(), locDto.getSubCounty(), locDto.getWard(),
                            locDto.getGps()))
                    .collect(Collectors.toList());
            existingOfficer.setLocations(updatedLocations);
        } else {
            existingOfficer.setLocations(new ArrayList<>()); // Ensure collection is not null
        }

        // --- Update Next of Kin (More robust update logic leveraging NOK ID) ---
        Set<Long> existingNokIdsInDb = existingOfficer.getNextOfKins().stream()
                .map(NextOfKin::getId)
                .collect(Collectors.toSet());
        Set<Long> nokIdsFromRequest = new HashSet<>();

        List<NextOfKin> newAndUpdatedNOKs = new ArrayList<>();

        if (request.getNextOfKins() != null) {
            for (NextOfKinDto nokDto : request.getNextOfKins()) {
                if (nokDto.getId() != null) {
                    Optional<NextOfKin> existingNokOpt = existingOfficer.getNextOfKins().stream()
                            .filter(nok -> nok.getId().equals(nokDto.getId()))
                            .findFirst();
                    if (existingNokOpt.isPresent()) {
                        NextOfKin nokToUpdate = existingNokOpt.get();
                        nokToUpdate.setName(nokDto.getName());
                        nokToUpdate.setPhone(nokDto.getPhone());
                        nokToUpdate.setRelationship(nokDto.getRelationship());
                        newAndUpdatedNOKs.add(nextOfKinRepository.save(nokToUpdate));
                        nokIdsFromRequest.add(nokDto.getId());
                    } else {
                        System.err.println("Warning: NextOfKinDto with ID " + nokDto.getId() + " not found for officer "
                                + id + ". Treating as new.");
                        NextOfKin newNok = new NextOfKin(null, nokDto.getName(), nokDto.getPhone(),
                                nokDto.getRelationship());
                        newAndUpdatedNOKs.add(nextOfKinRepository.save(newNok));
                    }
                } else {
                    NextOfKin newNok = new NextOfKin(null, nokDto.getName(), nokDto.getPhone(),
                            nokDto.getRelationship());
                    newAndUpdatedNOKs.add(nextOfKinRepository.save(newNok));
                }
            }
        }

        existingOfficer.getNextOfKins().stream()
                .filter(nok -> !nokIdsFromRequest.contains(nok.getId()))
                .forEach(nextOfKinRepository::delete);
        existingOfficer.setNextOfKins(newAndUpdatedNOKs);

        // --- Update Documents ---
        Set<Long> existingDocumentIdsInDb = existingOfficer.getDocuments().stream()
                .map(DocumentUpload::getId)
                .collect(Collectors.toSet());
        Set<Long> documentIdsFromRequest = new HashSet<>();
        List<DocumentUpload> newAndUpdatedDocuments = new ArrayList<>();

        if (request.getDocuments() != null) {
            for (DocumentUploadDto docDto : request.getDocuments()) {
                if (docDto.getId() != null) {
                    Optional<DocumentUpload> existingDocOpt = existingOfficer.getDocuments().stream()
                            .filter(doc -> doc.getId().equals(docDto.getId()))
                            .findFirst();
                    if (existingDocOpt.isPresent()) {
                        DocumentUpload docToUpdate = existingDocOpt.get();
                        docToUpdate.setFileName(docDto.getFileName());
                        docToUpdate.setFileUrl(docDto.getFileUrl());
                        docToUpdate.setContentType(docDto.getContentType());
                        newAndUpdatedDocuments.add(documentUploadRepository.save(docToUpdate));
                        documentIdsFromRequest.add(docDto.getId());
                    } else {
                        System.err.println("Warning: DocumentUploadDto with ID " + docDto.getId()
                                + " not found for officer " + id + ". Treating as new metadata document.");
                        DocumentUpload newDoc = new DocumentUpload(null, docDto.getFileName(), docDto.getFileUrl(),
                                docDto.getContentType());
                        newAndUpdatedDocuments.add(documentUploadRepository.save(newDoc));
                    }
                } else {
                    DocumentUpload newDoc = new DocumentUpload(null, docDto.getFileName(), docDto.getFileUrl(),
                            docDto.getContentType());
                    newAndUpdatedDocuments.add(documentUploadRepository.save(newDoc));
                }
            }
        }

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String fileName = fileStorageService.saveFileToServer(file, "officers");
                String fileUrl = fileStorageService.getFileUrl("officers", fileName);
                DocumentUpload newDoc = new DocumentUpload(null, fileName, fileUrl, file.getContentType());
                newAndUpdatedDocuments.add(documentUploadRepository.save(newDoc));
            }
        }

        existingOfficer.getDocuments().stream()
                .filter(doc -> !documentIdsFromRequest.contains(doc.getId()))
                .forEach(docToDelete -> {
                    if (docToDelete.getFileName() != null && !docToDelete.getFileName().isEmpty()) {
                        fileStorageService.deleteFileFromServer("officers", docToDelete.getFileName());
                    }
                    documentUploadRepository.delete(docToDelete);
                });

        existingOfficer.setDocuments(newAndUpdatedDocuments);

        // --- Update System User (TRES_User) ---
        TRES_User systemUser = existingOfficer.getSystemUser();
        if (systemUser != null) {
            boolean userDetailsChanged = false;
            if (!systemUser.getEmail().equals(request.getEmail())) {
                systemUser.setEmail(request.getEmail());
                userDetailsChanged = true;
            }
            if (!systemUser.getUsername().equals(request.getEmail())) {
                systemUser.setUsername(request.getEmail());
                userDetailsChanged = true;
            }

            if (userDetailsChanged) {
                africanaUserRepository.save(systemUser);
            }
        } else {
            System.err.println(
                    "CRITICAL WARNING: Existing officer " + id + " has no associated system user during update.");
        }

        OfficerRegistration updatedOfficer = officerRepo.save(existingOfficer);
        return mapToDto(updatedOfficer);
    }

    /**
     * Deletes an officer by ID.
     * This includes deleting associated documents from storage and database,
     * NextOfKin records, and the SystemUser.
     *
     * @param id The ID of the officer to delete.
     * @throws RuntimeException if the officer is not found.
     */
    @Transactional
    public void deleteOfficer(Long id) {
        OfficerRegistration officer = officerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Officer not found with ID: " + id));

        if (officer.getDocuments() != null) {
            for (DocumentUpload doc : officer.getDocuments()) {
                if (doc.getFileName() != null && !doc.getFileName().isEmpty()) {
                    fileStorageService.deleteFileFromServer("officers", doc.getFileName());
                }
                documentUploadRepository.delete(doc);
            }
        }

        if (officer.getNextOfKins() != null) {
            nextOfKinRepository.deleteAll(officer.getNextOfKins());
        }

        if (officer.getSystemUser() != null) {
            africanaUserRepository.delete(officer.getSystemUser());
        }

        officerRepo.delete(officer);
    }

    private OfficerRegistration mapToEntity(OfficerRegistrationDto request) {
        OfficerRegistration officer = new OfficerRegistration();
        officer.setFullName(request.getFullName());
        officer.setEmail(request.getEmail());
        officer.setPhoneNumber(request.getPhoneNumber());
        officer.setIdNumber(request.getIdNumber());
        officer.setDob(request.getDob());
        officer.setGender(request.getGender());
        officer.setBankDetails(request.getBankDetails());

        officer.setNextOfKins(new ArrayList<>());
        officer.setDocuments(new ArrayList<>());
        officer.setLocations(new ArrayList<>());

        return officer;
    }

    private OfficerRegistrationDto mapToDto(OfficerRegistration officer) {
        OfficerRegistrationDto dto = new OfficerRegistrationDto();
        dto.setId(officer.getId());
        dto.setFullName(officer.getFullName());
        dto.setEmail(officer.getEmail());
        dto.setPhoneNumber(officer.getPhoneNumber());
        dto.setIdNumber(officer.getIdNumber());
        dto.setDob(officer.getDob());
        dto.setGender(officer.getGender());
        dto.setBankDetails(officer.getBankDetails());

        if (officer.getAssignedStation() != null) {
            dto.setStationId(officer.getAssignedStation().getId());
            dto.setStationName(officer.getAssignedStation().getStationName());
        }

        if (officer.getNextOfKins() != null) {
            dto.setNextOfKins(officer.getNextOfKins().stream()
                    .map(nok -> new NextOfKinDto(nok.getId(), nok.getName(), nok.getPhone(), nok.getRelationship()))
                    .collect(Collectors.toList()));
        }

        if (officer.getDocuments() != null) {
            dto.setDocuments(officer.getDocuments().stream()
                    .map(doc -> new DocumentUploadDto(doc.getId(), doc.getFileName(), doc.getFileUrl(),
                            doc.getContentType()))
                    .collect(Collectors.toList()));
        }

        if (officer.getLocations() != null) {
            dto.setLocations(officer.getLocations().stream()
                    .map(loc -> new LocationDto(loc.getCounty(), loc.getSubCounty(), loc.getWard(), loc.getGps()))
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}