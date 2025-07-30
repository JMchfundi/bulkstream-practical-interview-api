package co.ke.finsis.inventory.payload;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CategoryDto {
    private Long id;
    private String name;
    private List<CategoryAttributeDto> attributes;
}
