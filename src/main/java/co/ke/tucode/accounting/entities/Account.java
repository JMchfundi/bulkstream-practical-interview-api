package co.ke.tucode.accounting.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    private AccountType accountCategory;

    // @DecimalMin("0.00")
    private BigDecimal balance = BigDecimal.ZERO;

    // Transactions where this account is debited
    @OneToMany(mappedBy = "debitAccount", cascade = CascadeType.ALL)
    @JsonManagedReference("debit-account")
    private List<Transaction> debitTransactions = new ArrayList<>();

    // Transactions where this account is credited
    @OneToMany(mappedBy = "creditAccount", cascade = CascadeType.ALL)
    @JsonManagedReference("credit-account")
    private List<Transaction> creditTransactions = new ArrayList<>();
}
