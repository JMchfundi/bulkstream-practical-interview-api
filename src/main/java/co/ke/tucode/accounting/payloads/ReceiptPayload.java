package co.ke.tucode.accounting.payloads;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ReceiptPayload {
    // private String receiptNo;
    private Long paymentFor;
    private LocalDate receiptDate;
    private String receivedFrom;
    private BigDecimal amount;
    private String amountInWords;
    private Long account;
    private String referenceNumber;
    // private String description; // General description for the transaction(s)
}