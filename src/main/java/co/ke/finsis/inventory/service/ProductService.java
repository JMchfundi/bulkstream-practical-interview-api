package co.ke.finsis.inventory.service;

import co.ke.finsis.entity.DocumentUpload;
import co.ke.finsis.entity.LoanType;
import co.ke.finsis.inventory.entity.Category;
import co.ke.finsis.inventory.entity.CategoryAttribute;
import co.ke.finsis.inventory.entity.Product;
import co.ke.finsis.inventory.entity.ProductAttributeValue;
import co.ke.finsis.inventory.entity.ProductUnit;
import co.ke.finsis.inventory.payload.ProductAttributeValueDto;
import co.ke.finsis.inventory.payload.ProductDto;
import co.ke.finsis.inventory.payload.ProductUnitDto;
import co.ke.finsis.inventory.repository.CategoryAttributeRepository;
import co.ke.finsis.inventory.repository.CategoryRepository;
import co.ke.finsis.inventory.repository.ProductAttributeValueRepository;
import co.ke.finsis.inventory.repository.ProductRepository;
import co.ke.finsis.inventory.repository.ProductUnitRepository;
import co.ke.finsis.payload.DocumentUploadDto;
import co.ke.finsis.repository.DocumentUploadRepository;
import co.ke.finsis.repository.LoanTypeRepository;
import co.ke.tucode.accounting.entities.Account;
import co.ke.tucode.accounting.payloads.ReceiptPayload;
import co.ke.tucode.accounting.repositories.AccountRepository;
import co.ke.tucode.accounting.services.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

        private final ProductRepository productRepo;
        private final CategoryRepository categoryRepo;
        private final CategoryAttributeRepository attrRepo;
        private final ProductAttributeValueRepository valueRepo;
        private final DocumentUploadRepository documentRepo;
        private final AccountRepository coaRepo;
        private final TransactionService transactionService;
        private final ProductUnitRepository unitRepo;
        private final LoanTypeRepository loanTypeRepo;

        @Transactional
        public ProductDto createProduct(ProductDto dto) {
                Category category = categoryRepo.findById(dto.getCategoryId())
                                .orElseThrow(() -> new RuntimeException("Category not found"));

                List<DocumentUpload> images = documentRepo.findAllById(
                                dto.getImages().stream().map(DocumentUploadDto::getId).toList());

                Product product = Product.builder()
                                .name(dto.getName())
                                .sellingPrice(dto.getSellingPrice()) // selling price
                                .purchasePrice(dto.getPurchasePrice()) // optional, if needed
                                .stock(dto.getStock())
                                .category(category)
                                .images(images)
                                .build();
                product = productRepo.save(product);

                for (ProductAttributeValueDto attr : dto.getAttributes()) {
                        CategoryAttribute catAttr = attrRepo.findById(attr.getCategoryAttributeId())
                                        .orElseThrow(() -> new RuntimeException("Attribute not found"));

                        ProductAttributeValue val = ProductAttributeValue.builder()
                                        .product(product)
                                        .categoryAttribute(catAttr)
                                        .attributeValue(attr.getValue())
                                        .build();
                        valueRepo.save(val);
                }

                return fromEntity(productRepo.findById(product.getId()).orElseThrow());
        }

        public List<ProductDto> getAllProducts() {
                return productRepo.findAll().stream()
                                .map(this::fromEntity)
                                .collect(Collectors.toList());
        }

        public ProductDto getProduct(Long id) {
                Product product = productRepo.findById(id)
                                .orElseThrow(() -> new RuntimeException("Product not found"));
                return fromEntity(product);
        }

        public void deleteProduct(Long id) {
                productRepo.deleteById(id);
        }

        @Transactional
        public ProductDto updateStock(Long productId, int quantity, boolean isStockIn) {
                Product product = productRepo.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                int newStock = isStockIn ? product.getStock() + quantity : product.getStock() - quantity;
                if (newStock < 0)
                        throw new RuntimeException("Insufficient stock");

                product.setStock(newStock);
                Product updated = productRepo.save(product);

                if (isStockIn) {
                        // Get accounting accounts
                        Account productsValue = coaRepo.findByCode("1004")
                                        .orElseThrow(() -> new RuntimeException("Products Value account not found"));
                        Account retainedEarnings = coaRepo.findByCode("5001")
                                        .orElseThrow(() -> new RuntimeException("Retained Earnings account not found"));

                        // Determine total value of the stock being added
                        BigDecimal totalValue = product.getPurchasePrice().multiply(BigDecimal.valueOf(quantity));

                        ReceiptPayload stockReceipt = ReceiptPayload.builder()
                                        .amount(totalValue)
                                        .receivedFrom("Stock In: Product ID " + productId)
                                        .referenceNumber("STOCKIN-" + productId + "-" + System.currentTimeMillis())
                                        .receiptDate(LocalDate.now())
                                        .account(productsValue.getId()) // DEBIT: Product Asset
                                        .paymentFor(retainedEarnings.getId()) // CREDIT: Equity
                                        .build();

                        transactionService.saveReceipt(stockReceipt);

                        for (int i = 0; i < quantity; i++) {

                                ProductUnit unit = ProductUnit.builder()
                                                .product(product)
                                                .serialNumber(null) // Not yet assigned
                                                .assigned(false)
                                                .build();

                                unitRepo.save(unit);
                        }

                }

                return fromEntity(updated);
        }

        // ================================
        // fromEntity logic in service
        // ================================
        private ProductDto fromEntity(Product product) {
                return ProductDto.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .sellingPrice(product.getSellingPrice()) // selling price
                                .purchasePrice(product.getPurchasePrice()) // purchase price
                                .stock(product.getStock())
                                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                                .images(product.getImages() != null
                                                ? product.getImages().stream()
                                                                .map(img -> DocumentUploadDto.builder()
                                                                                .id(img.getId())
                                                                                .fileName(img.getFileName())
                                                                                .fileUrl(img.getFileUrl())
                                                                                .contentType(img.getContentType())
                                                                                .build())
                                                                .collect(Collectors.toList())
                                                : List.of())
                                .build();
        }

        public List<ProductUnitDto> getUnassignedUnits(Long productId) {
                List<ProductUnit> units = unitRepo.findByProductIdAndAssignedFalse(productId);
                return units.stream().map(unit -> {
                        ProductUnitDto dto = new ProductUnitDto();
                        dto.setId(unit.getId());
                        dto.setProductId(productId);
                        dto.setSerialNumber(unit.getSerialNumber());
                        dto.setAssigned(unit.isAssigned());
                        return dto;
                }).collect(Collectors.toList());
        }

        @Transactional
        public void updateSerialNumbers(List<ProductUnitDto> unitDtos) {
                for (ProductUnitDto dto : unitDtos) {
                        ProductUnit unit = unitRepo.findById(dto.getId())
                                        .orElseThrow(() -> new RuntimeException("Product Unit not found"));
                        unit.setSerialNumber(dto.getSerialNumber());
                        unit.setAssigned(true); // Mark as assigned
                        unitRepo.save(unit);
                }
        }

        public List<ProductUnitDto> getAllUnits() {
                return unitRepo.findAll().stream()
                                .map(unit -> ProductUnitDto.builder()
                                                .id(unit.getId())
                                                .productId(unit.getProduct().getId())
                                                .serialNumber(unit.getSerialNumber())
                                                .assigned(unit.isAssigned())
                                                .build())
                                .collect(Collectors.toList());
        }

        public List<ProductDto> getProductsWithUnassignedUnits() {
                List<Product> products = productRepo.findAll(); // optimize with a custom query if needed
                return products.stream()
                                .filter(p -> unitRepo.existsByProductAndAssignedFalse(p))
                                .map(this::fromEntity)
                                .collect(Collectors.toList());
        }

        public List<ProductDto> getByCategory(Long loanTypeId) {
                System.out.println("Fetching products for loan type ID: " + loanTypeId);
                LoanType loanType = loanTypeRepo.findById(loanTypeId)
                                .orElseThrow(() -> new RuntimeException("Loan type not found"));

                if (loanType.getProductCategory() == null) {
                        throw new RuntimeException("Loan type has no linked product category");
                }

                Long categoryId = loanType.getProductCategory().getId();

                return productRepo.findByCategoryId(categoryId)
                                .stream()
                                .map(this::fromEntity)
                                .collect(Collectors.toList());
        }

        public List<ProductDto> searchProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
                System.out.println("Searching products between prices: " + minPrice + " and " + maxPrice);
                List<Product> products = productRepo.findBySellingPriceBetween(minPrice, maxPrice);
                return products.stream()
                                .map(this::fromEntity)
                                .collect(Collectors.toList());
        }

}
