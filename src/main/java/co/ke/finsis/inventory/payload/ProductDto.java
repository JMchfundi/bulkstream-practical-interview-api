package co.ke.finsis.inventory.payload;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

import co.ke.finsis.payload.DocumentUploadDto;

@Data
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private BigDecimal sellingPrice; // selling price
    private BigDecimal purchasePrice; // purchase price
    private int stock;
    private Long categoryId;
    private List<ProductAttributeValueDto> attributes;
    private List<DocumentUploadDto> images;
}
