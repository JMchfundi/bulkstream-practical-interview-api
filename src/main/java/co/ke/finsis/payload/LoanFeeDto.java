package co.ke.finsis.payload;

import java.math.BigDecimal;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanFeeDto {
    private String name;
    private BigDecimal amount;
    private Boolean isPercentage;
    private BigDecimal originalValue;
}
