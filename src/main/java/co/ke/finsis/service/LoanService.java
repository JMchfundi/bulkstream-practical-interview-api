package co.ke.finsis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import co.ke.finsis.entity.ClientInfo;
import co.ke.finsis.entity.Loan;
import co.ke.finsis.entity.LoanFee;
import co.ke.finsis.entity.LoanType;
import co.ke.finsis.payload.LoanFeeDto;
import co.ke.finsis.payload.LoanPayload;
import co.ke.finsis.repository.ClientInfoRepository;
import co.ke.finsis.repository.LoanFeeRepository;
import co.ke.finsis.repository.LoanRepository;
import co.ke.finsis.repository.LoanTypeRepository;
import co.ke.tucode.accounting.entities.Account;
import co.ke.tucode.accounting.entities.AccountType;
import co.ke.tucode.accounting.payloads.ReceiptPayload;
import co.ke.tucode.accounting.repositories.AccountRepository;
import co.ke.tucode.accounting.services.AccountService;
import co.ke.tucode.accounting.services.TransactionService;
import co.ke.tucode.approval.entities.ApprovalRequest;
import co.ke.tucode.approval.entities.ApprovalStep;
import co.ke.tucode.approval.services.ApprovalService;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final ApprovalService approvalService;
    private final TransactionService transactionService;
    private final ClientInfoRepository clientInfoRepository;
    private final AccountRepository accountRepository;
    private final ClientInfoService clientInfoService;
    private final AccountService accountService;
    private final LoanFeeRepository loanFeeRepository;

    @Transactional
    public LoanPayload createLoan(LoanPayload payload) {
        LoanType loanType = loanTypeRepository.findById(payload.getLoanTypeId())
                .orElseThrow(() -> new RuntimeException("LoanType not found with ID: " + payload.getLoanTypeId()));

        List<Long> approverIds = loanType.getApprovers().stream()
                .map(user -> user.getId())
                .collect(Collectors.toList());

        for (Long approverId : approverIds) {
                System.out.println("Approver ID: " + approverId);
        }

        ApprovalRequest approvalRequest = approvalService.createApprovalRequest(
                "Loan Application: " + payload.getIdNumber(),
                "Approval for loan application for " + payload.getPrincipalAmount(),
                payload.getRequestedByUserId(),
                approverIds);

        // 👉 Automatically approve if no approvers
        if (approverIds.isEmpty()) {
            approvalRequest.setStatus("APPROVED");
        }

        Loan loan = mapToEntity(payload);
        loan.setLoanType(loanType);
        // ✅ 1. Fetch and set the group from the transient group ID
        if (payload.getIdNumber() != null) {
            ClientInfo clientInfo = clientInfoRepository.findByIdNumber(payload.getIdNumber())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Client not found with ID: " + payload.getIdNumber()));
            loan.setClient(clientInfo);
        } else {
            throw new IllegalArgumentException("Group ID is required");
        }
        loan.setApprovalRequest(approvalRequest);

        loan = loanRepository.save(loan);

        for (LoanFeeDto feeDto : payload.getLoanFees()) {
            LoanFee fee = LoanFee.builder()
                    .loan(loan)
                    .name(feeDto.getName())
                    .amount(feeDto.getAmount())
                    .isPercentage(feeDto.getIsPercentage())
                    .originalValue(feeDto.getOriginalValue())
                    .build();
            loanFeeRepository.save(fee);
        }

        return mapToPayload(loan);
    }

    public LoanPayload getLoanById(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + id));
        return mapToPayload(loan);
    }

    public List<LoanPayload> getAllLoans() {
        return loanRepository.findAll()
                .stream()
                .map(this::mapToPayload)
                .collect(Collectors.toList());
    }

    public LoanPayload updateLoan(Long id, LoanPayload payload) {
        Loan existing = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + id));

        Loan updated = mapToEntity(payload);
        updated.setId(existing.getId());
        updated.setLoanType(existing.getLoanType());
        updated.setApprovalRequest(existing.getApprovalRequest());
        updated.setClient(existing.getClient());
        return mapToPayload(loanRepository.save(updated));
    }

    public LoanPayload updateLoanApprovalStatus(Long id, LoanPayload payload) {
        Loan existing = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + id));

        ApprovalRequest approvalRequest = existing.getApprovalRequest();
        approvalRequest.setStatus(payload.getApprovalStatus());

        // existing.setLoanType(existing.getLoanType());
        existing.setApprovalRequest(approvalRequest);
        return mapToPayload(loanRepository.save(existing));
    }

    public void deleteLoan(Long id) {
        loanRepository.deleteById(id);
    }

    public List<LoanPayload> getLoansPendingApprovalByUser(Long approverId) {
        return loanRepository.findAll().stream()
                .filter(loan -> {
                    ApprovalRequest request = loan.getApprovalRequest();
                    if (request == null || !"PENDING".equalsIgnoreCase(request.getStatus()))
                        return false;

                    return request.getSteps().stream()
                            .filter(step -> "PENDING".equalsIgnoreCase(step.getStatus()))
                            .sorted(Comparator.comparingInt(ApprovalStep::getStepOrder))
                            .findFirst()
                            .map(step -> step.getApprover() != null && step.getApprover().getId().equals(approverId))
                            .orElse(false);
                })
                .map(this::mapToPayload)
                .collect(Collectors.toList());
    }

    public List<LoanPayload> getFullyApprovedLoans() {
        return loanRepository.findAll().stream()
                .filter(loan -> loan.getApprovalRequest() != null &&
                        "APPROVED".equalsIgnoreCase(loan.getApprovalRequest().getStatus()))
                .map(this::mapToPayload)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanPayload disburseLoan(Long loanId, Long payingAccoutId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));

        if (loan.getApprovalRequest() == null || !"APPROVED".equalsIgnoreCase(loan.getApprovalRequest().getStatus())) {
            throw new IllegalStateException("Loan is not fully approved for disbursement");
        }

        ClientInfo clientInfo = clientInfoRepository.findByIdNumber(loan.getIdNumber())
                .orElseThrow(() -> new RuntimeException("Client not found with ID Number: " + loan.getIdNumber()));

        ReceiptPayload receiptPayloadLoanAccounts = ReceiptPayload.builder()
                .amount(BigDecimal.valueOf(loan.getPrincipalAmount()))
                .receivedFrom("Loan Client: " + loan.getIdNumber())
                .referenceNumber("LOAN-" + loan.getId())
                .receiptDate(loan.getStartDate() != null ? loan.getStartDate() : loan.getCreationDate())
                .account(getOrCreateClientLoanAccount(clientInfo, loan.getLoanType())) // CREDIT: Loan type GL account
                .paymentFor(payingAccoutId) // DEBIT: Client's loan
                                            // account
                .build();

        transactionService.saveReceipt(receiptPayloadLoanAccounts);

        loan.getApprovalRequest().setStatus("DISBURSED");
        loanRepository.save(loan);

        return mapToPayload(loan);
    }

    private Long getOrCreateClientLoanAccount(ClientInfo client, LoanType loanType) {
        String accountName = client.getFullName();

        return accountRepository.findByNameAndAccountCategory(accountName, AccountType.RECEIVABLE)
                .map(account -> {
                    if (!client.getAccounts().contains(account)) {
                        client.getAccounts().add(account);
                        clientInfoRepository.save(client);
                    }
                    return account.getId();
                })
                .orElseGet(() -> {
                    Account account = Account.builder()
                            .name(accountName)
                            .type(AccountType.ASSET)
                            .accountCategory(AccountType.RECEIVABLE)
                            .balance(BigDecimal.ZERO)
                            .build();
                    Account savedAccount = accountService.createAccount(account);
                    client.getAccounts().add(savedAccount);
                    clientInfoRepository.save(client);
                    return savedAccount.getId();
                });
    }

    private Loan mapToEntity(LoanPayload payload) {
        return Loan.builder()
                .idNumber(payload.getIdNumber())
                .principalAmount(payload.getPrincipalAmount())
                .interestRate(payload.getInterestRate())
                .loanTerm(payload.getLoanTerm())
                .termUnit(payload.getTermUnit())
                .repaymentFrequency(payload.getRepaymentFrequency())
                .purpose(payload.getPurpose())
                .creationDate(payload.getCreationDate() != null ? payload.getCreationDate() : LocalDate.now())
                .startDate(payload.getStartDate())
                .endDate(payload.getEndDate())
                .defaultEndDate(payload.getDefaultEndDate())
                .lifFee(payload.getLifFee())
                .lafFee(payload.getLafFee())
                .insuranceFee(payload.getInsuranceFee())
                .processingFee(payload.getProcessingFee())
                .penaltyRate(payload.getPenaltyRate())
                .repaymentAccount(payload.getRepaymentAccount())
                .totalPayableAmount(payload.getTotalPayableAmount())
                .selectedProductId(payload.getSelectedProductId())
                .build();
    }

    private LoanPayload mapToPayload(Loan loan) {
    List<LoanFeeDto> feeDtos = loan.getLoanFees() != null
            ? loan.getLoanFees().stream()
                    .map(fee -> LoanFeeDto.builder()
                            .name(fee.getName())
                            .amount(fee.getAmount())
                            .originalValue(fee.getOriginalValue())
                            .isPercentage(fee.getIsPercentage())
                            .build())
                    .collect(Collectors.toList())
            : Collections.emptyList();

    // Guard against null LoanType
    LoanType loanType = loan.getLoanType();

    return LoanPayload.builder()
            .id(loan.getId())
            .idNumber(loan.getIdNumber())
            .loanTypeId(loanType != null ? loanType.getId() : null)
            .loanTypeName(loanType != null ? loanType.getName() : null)
            .principalAmount(loan.getPrincipalAmount())
            .loanTerm(loan.getLoanTerm())
            .termUnit(loan.getTermUnit())
            .repaymentFrequency(loan.getRepaymentFrequency())
            .purpose(loan.getPurpose())
            .creationDate(loan.getCreationDate())
            .startDate(loan.getStartDate())
            .endDate(loan.getEndDate())
            .defaultEndDate(loan.getDefaultEndDate())
            .approvalStatus(
                loan.getApprovalRequest() != null
                    ? loan.getApprovalRequest().getStatus()
                    : "UNKNOWN"
            )
            .totalPayableAmount(loan.getTotalPayableAmount())
            .loanFees(feeDtos)
            .build();
}

}
