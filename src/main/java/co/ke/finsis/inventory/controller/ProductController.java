package co.ke.finsis.inventory.controller;

import co.ke.finsis.inventory.payload.ProductDto;
import co.ke.finsis.inventory.payload.ProductUnitDto;
import co.ke.finsis.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDto> create(@RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.createProduct(dto));
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> get() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/stock-in")
    public ResponseEntity<ProductDto> stockIn(@PathVariable Long id, @RequestParam int quantity) {
        return ResponseEntity.ok(productService.updateStock(id, quantity, true));
    }

    @PutMapping("/{id}/stock-out")
    public ResponseEntity<ProductDto> stockOut(@PathVariable Long id, @RequestParam int quantity) {
        return ResponseEntity.ok(productService.updateStock(id, quantity, false));
    }

    // ✅ Fetch unassigned product units
    @GetMapping("/{id}/units/unassigned")
    public ResponseEntity<List<ProductUnitDto>> getUnassignedUnits(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getUnassignedUnits(id));
    }

    // ✅ Update serial numbers for units
    @PutMapping("/units/update-serials")
    public ResponseEntity<Void> updateSerials(@RequestBody List<ProductUnitDto> unitDtos) {
        productService.updateSerialNumbers(unitDtos);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/units")
    public ResponseEntity<List<ProductUnitDto>> getAllUnits() {
        return ResponseEntity.ok(productService.getAllUnits());
    }

    @GetMapping("/units/unassigned-products")
    public ResponseEntity<List<ProductDto>> getProductsWithUnassignedUnits() {
        return ResponseEntity.ok(productService.getProductsWithUnassignedUnits());
    }

    @GetMapping("/by-category/{loanTypeId}")
    public ResponseEntity<List<ProductDto>> getByCategory(@PathVariable Long loanTypeId) {
        return ResponseEntity.ok(productService.getByCategory(loanTypeId));
    }

    @GetMapping("/search-by-price")
    public ResponseEntity<List<ProductDto>> getByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(productService.searchProductsByPriceRange(minPrice, maxPrice));
    }

}
