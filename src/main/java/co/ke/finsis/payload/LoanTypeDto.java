package co.ke.finsis.payload;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.Column;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanTypeDto {
    private Long id;
    private String name;
    private String description;
    private Integer maxTerm;
    private String termUnit;

    @Column(name = "account_id")
    private Long accountId;

    private List<Long> approverUserIds;
    private Long productCategoryId;

    private String classification;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private List<LoanAttributeDto> customAttributes;

}
