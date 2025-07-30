package co.ke.finsis.payload;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPayload {
    private Long id;
    private String idNumber;

    // Changed: originally was just a string, now we carry the loanTypeId + name
    private Long loanTypeId;
    private String loanTypeName; // optional, for display only

    private Double principalAmount;
    private Double interestRate;
    private Integer loanTerm;
    private String termUnit;
    private String repaymentFrequency;
    private String purpose;
    private LocalDate creationDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate defaultEndDate;

    private Double lifFee;
    private Double lafFee;
    private Double insuranceFee;
    private Double processingFee;
    private Double penaltyRate;

    private List<LoanFeeDto> loanFees; // New: Itemized fees like LIF, LAF, etc.
    private Double totalPayableAmount; // New: Total calculated from principal + fees
    private Long selectedProductId; // New: In case of 'Product' classification

    private List<String> paymentMethods;
    private String repaymentAccount;

    // New: User applying for this loan
    private Long requestedByUserId;

    // New: To expose approval status (e.g., PENDING, APPROVED)
    private String approvalStatus;
}
