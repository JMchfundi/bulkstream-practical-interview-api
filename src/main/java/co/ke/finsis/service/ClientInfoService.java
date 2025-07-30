package co.ke.finsis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import co.ke.finsis.entity.*;
import co.ke.finsis.payload.*; // This now implies NextOfKinDto and DocumentUploadDto have 'id'
import co.ke.finsis.repository.ClientInfoRepository;
import co.ke.finsis.repository.DocumentUploadRepository;
import co.ke.finsis.repository.GroupRepository;
import co.ke.finsis.repository.NextOfKinRepository;
import co.ke.tucode.accounting.entities.Account;
import co.ke.tucode.accounting.entities.AccountType;
import co.ke.tucode.accounting.repositories.AccountRepository;
import co.ke.tucode.accounting.services.AccountService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class ClientInfoService {

    @Autowired
    private ClientInfoRepository clientInfoRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private NextOfKinRepository nextOfKinRepository;

    @Autowired
    private DocumentUploadRepository documentUploadRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Transactional
    public ClientDto createClient(ClientDto request, List<MultipartFile> files) throws IOException {
        ClientInfo client = mapToEntity(request);

        // 1. Handle Group
        if (request.getGroupId() != null) {
            Group group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Group not found with ID: " + request.getGroupId()));
            client.setClientGroup(group);
        } else {
            throw new IllegalArgumentException("Group ID is required");
        }

        // 2. Handle Locations (Replace strategy on create)
        if (request.getLocations() != null && !request.getLocations().isEmpty()) {
            List<Location> locations = request.getLocations().stream()
                    .map(locDto -> new Location(locDto.getCounty(), locDto.getSubCounty(), locDto.getWard(),
                            locDto.getGps()))
                    .collect(Collectors.toList());
            client.setLocations(locations);
        } else {
            client.setLocations(new ArrayList<>());
        }

        // 3. Handle Next of Kins (Create new)
        if (request.getNextOfKins() != null && !request.getNextOfKins().isEmpty()) {
            List<NextOfKin> newNOKs = request.getNextOfKins().stream()
                    // Pass null for ID as these are new entities being created
                    .map(nokDto -> new NextOfKin(null, nokDto.getName(), nokDto.getPhone(), nokDto.getRelationship()))
                    .collect(Collectors.toList());
            List<NextOfKin> savedNOKs = nextOfKinRepository.saveAll(newNOKs);
            client.setNextOfKins(newNOKs); // Set the list to the client
        } else {
            client.setNextOfKins(new ArrayList<>());
        }

        // 4. Handle Documents (New uploads and metadata)
        List<DocumentUpload> allDocuments = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            List<DocumentUpload> uploadedDocs = files.stream().map(file -> {
                String fileName = fileStorageService.saveFileToServer(file, "client_documents");
                String fileUrl = fileStorageService.getFileUrl("client_documents", fileName);
                return new DocumentUpload(null, fileName, fileUrl, file.getContentType()); // Null ID for new docs
            }).collect(Collectors.toList());
            allDocuments.addAll(uploadedDocs);
        }

        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            List<DocumentUpload> metaDocs = request.getDocuments().stream()
                    // Null ID for new metadata docs on create
                    .map(docDto -> new DocumentUpload(null, docDto.getFileName(), docDto.getFileUrl(),
                            docDto.getContentType()))
                    .collect(Collectors.toList());
            allDocuments.addAll(metaDocs);
        }

        if (!allDocuments.isEmpty()) {
            List<DocumentUpload> savedAllDocuments = documentUploadRepository.saveAll(allDocuments);
            client.setDocuments(savedAllDocuments); // Set the list to the client
        } else {
            client.setDocuments(new ArrayList<>());
        }

        // 5. Get or Create Accounts (Before saving client, ensure accounts are linked)
        // These methods modify the 'client' object by adding accounts to its collection
        getOrCreateClientCurrentAccount(client);
        getOrCreateClientSavingAccount(client);

        // 6. Save the ClientInfo entity
        ClientInfo savedClient = clientInfoRepository.save(client);

        return mapToClientDto(savedClient);
    }

    @Transactional
    public ClientInfo updateClient(Long id, ClientDto request, List<MultipartFile> files) throws IOException {
        ClientInfo existingClient = clientInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));

        // --- Update basic fields ---
        existingClient.setFullName(request.getFullName());
        existingClient.setEmail(request.getEmail());
        existingClient.setPhoneNumber(request.getPhoneNumber());
        existingClient.setIdNumber(request.getIdNumber());
        existingClient.setDob(request.getDob());
        existingClient.setGender(request.getGender());

        // --- Update Group ---
        if (request.getGroupId() != null) {
            Group group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Group not found with ID: " + request.getGroupId()));
            existingClient.setClientGroup(group);
        } else {
            existingClient.setClientGroup(null); // Dissociate if group ID is null
        }

        // --- Update Locations (Replace strategy) ---
        existingClient.getLocations().clear();
        if (request.getLocations() != null && !request.getLocations().isEmpty()) {
            List<Location> locations = request.getLocations().stream()
                    .map(locDto -> new Location(locDto.getCounty(), locDto.getSubCounty(), locDto.getWard(),
                            locDto.getGps()))
                    .collect(Collectors.toList());
            existingClient.getLocations().addAll(locations);
        }

        // --- Update Next of Kins (Robust update logic using ID) ---
        Set<Long> nokIdsInDb = existingClient.getNextOfKins().stream()
                                    .map(NextOfKin::getId)
                                    .collect(Collectors.toSet());
        Set<Long> nokIdsFromRequest = new HashSet<>(); // IDs of NOKs sent from frontend

        List<NextOfKin> newAndUpdatedNOKs = new ArrayList<>();

        if (request.getNextOfKins() != null) {
            for (NextOfKinDto nokDto : request.getNextOfKins()) {
                if (nokDto.getId() != null) { // If DTO has an ID, try to find and update existing NOK
                    Optional<NextOfKin> existingNokOpt = existingClient.getNextOfKins().stream()
                                                        .filter(nok -> nok.getId().equals(nokDto.getId()))
                                                        .findFirst();
                    if (existingNokOpt.isPresent()) {
                        NextOfKin nokToUpdate = existingNokOpt.get();
                        nokToUpdate.setName(nokDto.getName());
                        nokToUpdate.setPhone(nokDto.getPhone());
                        nokToUpdate.setRelationship(nokDto.getRelationship());
                        newAndUpdatedNOKs.add(nextOfKinRepository.save(nokToUpdate)); // Save updated existing NOK
                        nokIdsFromRequest.add(nokDto.getId());
                    } else {
                        // DTO has an ID but it's not found in existingClient's NOKs.
                        // Treat as new, but this might indicate a frontend issue or an attempt to link a non-existent NOK.
                        System.err.println("Warning: NextOfKinDto with ID " + nokDto.getId() + " not found for client " + id + ". Treating as new.");
                        NextOfKin newNok = new NextOfKin(null, nokDto.getName(), nokDto.getPhone(), nokDto.getRelationship());
                        newAndUpdatedNOKs.add(nextOfKinRepository.save(newNok));
                    }
                } else { // No ID in DTO, it's a new NOK
                    NextOfKin newNok = new NextOfKin(null, nokDto.getName(), nokDto.getPhone(), nokDto.getRelationship());
                    newAndUpdatedNOKs.add(nextOfKinRepository.save(newNok));
                }
            }
        }

        // Delete NOKs that are in the database but were not in the request DTO (i.e., not in nokIdsFromRequest)
        existingClient.getNextOfKins().stream()
                .filter(nok -> nok.getId() != null && !nokIdsFromRequest.contains(nok.getId())) // Ensure ID exists before checking set
                .forEach(nextOfKinRepository::delete);
        existingClient.setNextOfKins(newAndUpdatedNOKs); // Update the entity's collection

        // --- Update Documents (Robust update logic using ID, and file management) ---
        Set<Long> docIdsInDb = existingClient.getDocuments().stream()
                                        .map(DocumentUpload::getId)
                                        .collect(Collectors.toSet());
        Set<Long> docIdsFromRequest = new HashSet<>(); // IDs of documents sent from frontend
        List<DocumentUpload> newAndUpdatedDocuments = new ArrayList<>();

        // Process documents specified in the DTO (existing ones with potential updates)
        if (request.getDocuments() != null) {
            for (DocumentUploadDto docDto : request.getDocuments()) {
                if (docDto.getId() != null) { // This implies an existing document to be kept/updated
                    Optional<DocumentUpload> existingDocOpt = existingClient.getDocuments().stream()
                                                            .filter(doc -> doc.getId().equals(docDto.getId()))
                                                            .findFirst();
                    if (existingDocOpt.isPresent()) {
                        DocumentUpload docToUpdate = existingDocOpt.get();
                        // Update metadata fields if they can change via DTO
                        docToUpdate.setFileName(docDto.getFileName());
                        docToUpdate.setFileUrl(docDto.getFileUrl());
                        docToUpdate.setContentType(docDto.getContentType());
                        newAndUpdatedDocuments.add(documentUploadRepository.save(docToUpdate));
                        docIdsFromRequest.add(docDto.getId());
                    } else {
                        // DTO has an ID but not found in existingClient's docs. Treat as new metadata doc.
                        System.err.println("Warning: DocumentUploadDto with ID " + docDto.getId() + " not found for client " + id + ". Treating as new metadata document.");
                        DocumentUpload newDoc = new DocumentUpload(null, docDto.getFileName(), docDto.getFileUrl(), docDto.getContentType());
                        newAndUpdatedDocuments.add(documentUploadRepository.save(newDoc));
                    }
                } else { // No ID in DTO, it's a new metadata-only document
                    DocumentUpload newDoc = new DocumentUpload(null, docDto.getFileName(), docDto.getFileUrl(), docDto.getContentType());
                    newAndUpdatedDocuments.add(documentUploadRepository.save(newDoc));
                }
            }
        }

        // Process new file uploads
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String fileName = fileStorageService.saveFileToServer(file, "client_documents");
                String fileUrl = fileStorageService.getFileUrl("client_documents", fileName);
                DocumentUpload newDoc = new DocumentUpload(null, fileName, fileUrl, file.getContentType());
                newAndUpdatedDocuments.add(documentUploadRepository.save(newDoc));
            }
        }

        // Delete documents that were in the database but are no longer in the request DTO
        // Also delete their corresponding files from storage.
        existingClient.getDocuments().stream()
                .filter(doc -> doc.getId() != null && !docIdsFromRequest.contains(doc.getId()))
                .forEach(docToDelete -> {
                    if (docToDelete.getFileName() != null && !docToDelete.getFileName().isEmpty()) {
                         fileStorageService.deleteFileFromServer("client_documents", docToDelete.getFileName());
                    }
                    documentUploadRepository.delete(docToDelete);
                });

        existingClient.setDocuments(newAndUpdatedDocuments); // Set the updated collection of documents

        return clientInfoRepository.save(existingClient);
    }

    public List<ClientDto> getAllClients() {
        return clientInfoRepository.findAll().stream()
                .map(this::mapToClientDto)
                .collect(Collectors.toList());
    }

    public Optional<ClientInfo> getClientById(Long id) {
        Optional<ClientInfo> clientOptional = clientInfoRepository.findById(id);
        clientOptional.ifPresent(client -> {
            // Ensure lazy collections are initialized if accessed outside a transaction
            // These calls force loading if they are LAZY
            client.getLocations().size();
            client.getNextOfKins().size();
            client.getDocuments().size();
            client.getAccounts().size();
            client.getLoans().size();
            if (client.getClientGroup() != null) {
                client.getClientGroup().getId();
            }
        });
        return clientOptional;
    }

    @Transactional
    public void deleteClientInfo(Long id) {
        ClientInfo client = clientInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));

        // Delete associated documents and their files
        if (client.getDocuments() != null) {
            for (DocumentUpload doc : client.getDocuments()) {
                if (doc.getFileName() != null && !doc.getFileName().isEmpty()) {
                    fileStorageService.deleteFileFromServer("client_documents", doc.getFileName());
                }
                documentUploadRepository.delete(doc);
            }
        }

        // Delete associated NextOfKin records
        if (client.getNextOfKins() != null) {
            nextOfKinRepository.deleteAll(client.getNextOfKins());
        }

        // Note: Client Accounts are typically managed by the Accounting module.
        // Decide if deleting a client should also delete their accounts.
        // If accounts are tied only to client and can be deleted:
        // if (client.getAccounts() != null) {
        //     accountRepository.deleteAll(client.getAccounts());
        // }

        clientInfoRepository.delete(client);
    }

    private ClientInfo mapToEntity(ClientDto request) {
        ClientInfo client = new ClientInfo();
        // ID is not set here for new entities, it's generated by the DB.
        client.setFullName(request.getFullName());
        client.setEmail(request.getEmail());
        client.setPhoneNumber(request.getPhoneNumber());
        client.setIdNumber(request.getIdNumber());
        client.setDob(request.getDob());
        client.setGender(request.getGender());

        // Initialize collections to avoid NullPointerExceptions later
        client.setLocations(new ArrayList<>());
        client.setNextOfKins(new ArrayList<>());
        client.setDocuments(new ArrayList<>());
        client.setAccounts(new ArrayList<>());
        client.setLoans(new ArrayList<>());

        return client;
    }

    /**
     * Maps a ClientInfo entity to a ClientDto.
     * This method is public so it can be used by other services (e.g.,
     * GroupService).
     *
     * @param client The ClientInfo entity to map.
     * @return A new ClientDto.
     */
    public ClientDto mapToClientDto(ClientInfo client) {
        List<AccountSummaryDto> accountSummaries = client.getAccounts() != null ? client.getAccounts().stream()
                .map(account -> new AccountSummaryDto(account.getId(), account.getName(), account.getAccountCategory(), account.getBalance()))
                .collect(Collectors.toList()) : new ArrayList<>();

        List<LocationDto> locationDtos = client.getLocations() != null ? client.getLocations().stream()
                .map(loc -> new LocationDto(loc.getCounty(), loc.getSubCounty(), loc.getWard(),
                        loc.getGps()))
                .collect(Collectors.toList()) : new ArrayList<>();

        List<NextOfKinDto> nextOfKinDtos = client.getNextOfKins() != null ? client.getNextOfKins().stream()
                // Now passing the ID from the entity to the DTO
                .map(nok -> new NextOfKinDto(nok.getId(), nok.getName(), nok.getPhone(), nok.getRelationship()))
                .collect(Collectors.toList()) : new ArrayList<>();

        List<DocumentUploadDto> documentDtos = client.getDocuments() != null ? client.getDocuments().stream()
                // Now passing the ID from the entity to the DTO
                .map(doc -> new DocumentUploadDto(doc.getId(), doc.getFileName(), doc.getFileUrl(),
                        doc.getContentType()))
                .collect(Collectors.toList()) : new ArrayList<>();

        return new ClientDto(
                client.getId(),
                client.getFullName(),
                client.getEmail(),
                client.getPhoneNumber(),
                client.getIdNumber(),
                client.getDob(),
                client.getGender(),
                client.getClientGroup() != null ? client.getClientGroup().getId() : null,
                client.getClientGroup() != null ? client.getClientGroup().getGroupName() : null,
                locationDtos,
                nextOfKinDtos,
                documentDtos,
                accountSummaries);
    }

    public Long getOrCreateClientCurrentAccount(ClientInfo client) {
        String accountName = client.getFullName();

        return accountRepository.findByNameAndAccountCategory(accountName, AccountType.CURRENT)
                .map(account -> {
                    // Check if the account is already linked to the client's collection
                    // This is important for ManyToMany or OneToMany relationships
                    if (!client.getAccounts().contains(account)) {
                        client.getAccounts().add(account);
                        // No need to save client here, will be saved at the end of createClient
                        // clientInfoRepository.save(client); // REMOVE: Avoid multiple saves within one transaction
                    }
                    return account.getId();
                })
                .orElseGet(() -> {
                    Account account = Account.builder()
                            .name(accountName)
                            .type(AccountType.LIABILITY)
                            .accountCategory(AccountType.CURRENT)
                            .balance(BigDecimal.ZERO)
                            .build();
                    Account savedAccount = accountService.createAccount(account); // This account is now managed by JPA
                    client.getAccounts().add(savedAccount);
                    // No need to save client here, will be saved at the end of createClient
                    // clientInfoRepository.save(client); // REMOVE: Avoid multiple saves within one transaction
                    return savedAccount.getId();
                });
    }

    public Long getOrCreateClientSavingAccount(ClientInfo client) {
        String accountName = client.getFullName();

        return accountRepository.findByNameAndAccountCategory(accountName, AccountType.SAVING)
                .map(account -> {
                    // Check if the account is already linked to the client's collection
                    if (!client.getAccounts().contains(account)) {
                        client.getAccounts().add(account);
                        // clientInfoRepository.save(client); // REMOVE
                    }
                    return account.getId();
                })
                .orElseGet(() -> {
                    Account account = Account.builder()
                            .name(accountName)
                            .type(AccountType.LIABILITY)
                            .accountCategory(AccountType.SAVING)
                            .balance(BigDecimal.ZERO)
                            .build();
                    Account savedAccount = accountService.createAccount(account);
                    client.getAccounts().add(savedAccount);
                    // clientInfoRepository.save(client); // REMOVE
                    return savedAccount.getId();
                });
    }

}