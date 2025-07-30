package co.ke.finsis.inventory.payload;

import lombok.Data;

@Data
public class ProductAttributeValueDto {
    private Long categoryAttributeId;
    private String value;
}
