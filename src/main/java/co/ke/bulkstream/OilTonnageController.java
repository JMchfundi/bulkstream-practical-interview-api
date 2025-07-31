package co.ke.bulkstream;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oil-tonnages")
@CrossOrigin(origins = "http://localhost:5173") // Adjust for your Vue.js dev server port
public class OilTonnageController {

    @Autowired
    private OilTonnageService oilTonnageService;

    @PostMapping("/calculate")
    public ResponseEntity<OilTonnage> calculateTonnage(@Valid @RequestBody CalculationRequest request) {
        try {
            OilTonnage result = oilTonnageService.calculateAndSaveTonnage(
                    request.getVolume(), request.getDensity(), request.getTemperature());
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<Page<OilTonnage>> getAllCalculations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "calculationDate,desc") String[] sort) {

        Sort sorting = Sort.by(sort[0]);
        if (sort.length > 1 && sort[1].equalsIgnoreCase("desc")) {
            sorting = sorting.descending();
        } else {
            sorting = sorting.ascending();
        }
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<OilTonnage> calculations = oilTonnageService.getAllCalculations(pageable);
        return new ResponseEntity<>(calculations, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<OilTonnage>> searchCalculations(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "calculationDate,desc") String[] sort) {

        Sort sorting = Sort.by(sort[0]);
        if (sort.length > 1 && sort[1].equalsIgnoreCase("desc")) {
            sorting = sorting.descending();
        } else {
            sorting = sorting.ascending();
        }
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<OilTonnage> calculations = oilTonnageService.searchCalculations(searchTerm, pageable);
        return new ResponseEntity<>(calculations, HttpStatus.OK);
    }
}