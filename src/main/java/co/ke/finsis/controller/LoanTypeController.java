package co.ke.finsis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.ke.finsis.payload.LoanAttributeDto;
import co.ke.finsis.payload.LoanTypeDto;
import co.ke.finsis.service.LoanTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/loan-types")
@RequiredArgsConstructor
public class LoanTypeController {

    private final LoanTypeService service;

    @PostMapping
    public ResponseEntity<LoanTypeDto> create(@RequestBody LoanTypeDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanTypeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<LoanTypeDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanTypeDto> update(@PathVariable Long id, @RequestBody LoanTypeDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/attributes")
    public ResponseEntity<List<LoanAttributeDto>> getLoanTypeAttributes(@PathVariable Long id) {
        List<LoanAttributeDto> attributes = service.getAttributesByLoanTypeId(id);
        return ResponseEntity.ok(attributes);
    }

}
