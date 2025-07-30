// src/main/java/co/ke/finsis/controller/ClientInfoController.java
package co.ke.finsis.controller;

import co.ke.finsis.entity.ClientInfo; // Keep this import for potential direct entity usage if needed
import co.ke.finsis.payload.ClientDto;
import co.ke.finsis.service.ClientInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clients")
public class ClientInfoController {

    @Autowired
    private ClientInfoService clientInfoService;

    /**
     * Creates a new client with their details and optional associated files.
     * Consumes multipart/form-data.
     *
     * @param clientDto The ClientDto containing client information (as JSON part named "request").
     * @param files Optional list of MultipartFile for documents (named "files").
     * @return ResponseEntity with the created ClientDto and HTTP status 201 (Created).
     * @throws IOException If there's an issue with file handling.
     */
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClientDto> createClient(
            @RequestPart("request") ClientDto clientDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {

        ClientDto createdDto = clientInfoService.createClient(clientDto, files);
        return new ResponseEntity<>(createdDto, HttpStatus.CREATED);
    }

    /**
     * Updates an existing client's details and optional associated files by ID.
     * Consumes multipart/form-data.
     *
     * @param id The ID of the client to update.
     * @param clientDto The updated ClientDto containing client information (as JSON part named "request").
     * @param files Optional list of MultipartFile for new or updated documents (named "files").
     * @return ResponseEntity with the updated ClientDto and HTTP status 200 (OK).
     * @throws IOException If there's an issue with file handling.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClientDto> updateClient(
            @PathVariable Long id,
            @RequestPart("request") ClientDto clientDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {

        ClientInfo updatedClient = clientInfoService.updateClient(id, clientDto, files);
        return ResponseEntity.ok(clientInfoService.mapToClientDto(updatedClient));
    }

    /**
     * Retrieves a list of all clients.
     *
     * @return ResponseEntity with a list of ClientDto and HTTP status 200 (OK).
     */
    @GetMapping("/all")
    public ResponseEntity<List<ClientDto>> getAllClients() {
        List<ClientDto> clients = clientInfoService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    /**
     * Retrieves a single client by ID.
     *
     * @param id The ID of the client to retrieve.
     * @return ResponseEntity with the ClientDto and HTTP status 200 (OK) if found,
     * or HTTP status 404 (Not Found) if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClientById(@PathVariable Long id) {
        Optional<ClientInfo> clientOptional = clientInfoService.getClientById(id);
        return clientOptional.map(client -> ResponseEntity.ok(clientInfoService.mapToClientDto(client)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a client by ID.
     *
     * @param id The ID of the client to delete.
     * @return ResponseEntity with no content and HTTP status 204 (No Content).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientInfoService.deleteClientInfo(id);
        return ResponseEntity.noContent().build();
    }
}