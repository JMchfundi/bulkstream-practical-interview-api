package co.ke.tucode.accounting.entities;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transactions_table")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionType type; // DEBIT or CREDIT

    // Use explicit relationship to debit account
    @ManyToOne
    @JoinColumn(name = "debit_account_id")
    @JsonBackReference("debit-account")
    private Account debitAccount;

    // Use explicit relationship to credit account
    @ManyToOne
    @JoinColumn(name = "credit_account_id")
    @JsonBackReference("credit-account")
    private Account creditAccount;

    @ManyToOne
    @JoinColumn(name = "journal_entry_id")
    @JsonBackReference("journal-entry-transaction")
    private JournalEntry journalEntry;

    // Optional field for additional details (stored as text/blob)
    @Lob
    private String details;
}
