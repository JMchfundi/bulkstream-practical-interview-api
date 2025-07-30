// ============================
// CONTROLLER: StationController.java
// ============================
package co.ke.finsis.controller;

import co.ke.finsis.payload.StationDto;
import co.ke.finsis.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<StationDto> create(
            @RequestPart("station") StationDto dto,
            @RequestPart(value = "document", required = false) MultipartFile document) {
        return ResponseEntity.ok(stationService.createStation(dto, document));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StationDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(stationService.getStation(id));
    }

    @GetMapping
    public ResponseEntity<List<StationDto>> getAll() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<StationDto> update(
            @PathVariable Long id,
            @RequestPart("station") StationDto dto,
            @RequestPart(value = "document", required = false) MultipartFile document) {
        return ResponseEntity.ok(stationService.updateStation(id, dto, document));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stationService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }
}