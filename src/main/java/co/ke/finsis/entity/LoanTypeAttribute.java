package co.ke.finsis.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "loan_type_attributes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanTypeAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "attribute_value", precision = 19, scale = 4) // Common precision for financial values
    private BigDecimal value;
    @Column(name = "is_percentage")
    private boolean percentage; // If value is a %

    private String chargeType; // One-Time or Recurring Monthly

    private String oneTimeTiming; // null or "Immediately" or "After Period"

    private Integer oneTimePeriodValue; // only if "After Period"
    private String oneTimePeriodUnit;   // Days, Weeks, Months

    @ManyToOne
    @JoinColumn(name = "loan_type_id_")
    private LoanType loanType;
}
