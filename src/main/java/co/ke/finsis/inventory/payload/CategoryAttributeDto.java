package co.ke.finsis.inventory.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryAttributeDto {
    private Long id;
    private String name;
    private String type;
}
