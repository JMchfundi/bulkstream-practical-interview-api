package co.ke.finsis.payload;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanAttributeDto {
    private String name;
    private BigDecimal value;

    @JsonProperty("isPercentage") // 👈 THIS is the fix
    private boolean percentage;
    private String valueType; // e.g., '%', 'Ksh', 'USD'
    private String chargeType;
    private String oneTimeTiming;
    private Integer oneTimePeriodValue;
    private String oneTimePeriodUnit;

}
