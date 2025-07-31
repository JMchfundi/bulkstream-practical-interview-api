package co.ke.bulkstream;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculationRequest {
    @NotNull(message = "Volume cannot be null")
    @Positive(message = "Volume must be a positive number")
    private Double volume;

    @NotNull(message = "Density cannot be null")
    @Positive(message = "Density must be a positive number")
    private Double density;

    @NotNull(message = "Temperature cannot be null")
    private Double temperature; // Temperature can be negative (e.g., in Celsius)
}