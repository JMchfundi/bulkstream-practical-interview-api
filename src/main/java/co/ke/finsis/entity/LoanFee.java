// src/main/java/co/ke/finsis/entity/LoanFee.java
package co.ke.finsis.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "loan_fees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private Loan loan;

    private String name;
    private BigDecimal amount; // Calculated amount for this specific loan
    private Boolean isPercentage;
    private BigDecimal originalValue; // The original percentage or fixed value from LoanAttribute
}