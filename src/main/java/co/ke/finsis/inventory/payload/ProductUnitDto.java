package co.ke.finsis.inventory.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductUnitDto {
    private Long id;
    private Long productId;
    private String serialNumber;
    private boolean assigned;
}
