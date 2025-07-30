package co.ke.tucode.accounting.payloads;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatementEntry {
    private LocalDate date;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal balance;
}
