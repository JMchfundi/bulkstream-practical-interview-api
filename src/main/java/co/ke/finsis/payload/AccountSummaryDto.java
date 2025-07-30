package co.ke.finsis.payload;

import lombok.*;
import java.math.BigDecimal;

import co.ke.tucode.accounting.entities.AccountType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountSummaryDto {
    private Long Id;
    private String name;
    private AccountType accountCategory;
    private BigDecimal balance;
}
