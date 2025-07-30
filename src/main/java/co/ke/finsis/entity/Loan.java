package co.ke.finsis.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import co.ke.tucode.approval.entities.ApprovalRequest;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idNumber;

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

    @ElementCollection
    private List<String> paymentMethods;

    private String repaymentAccount;

    @ManyToOne
    @JoinColumn(name = "loan_type_id_")
    private LoanType loanType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "approval_request_id")
    private ApprovalRequest approvalRequest;

    // In Loan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonBackReference
    private ClientInfo client;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanFee> loanFees; // Actual fees charged to this loan instance
    private Double totalPayableAmount; // New: Total calculated from principal + fees
    private Long selectedProductId; // New: In case of 'Product' classification
}
