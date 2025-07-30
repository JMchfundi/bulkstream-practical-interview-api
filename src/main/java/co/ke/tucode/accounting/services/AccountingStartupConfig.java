package co.ke.tucode.accounting.services;

import co.ke.tucode.accounting.entities.Account;
import co.ke.tucode.accounting.entities.AccountType;
import co.ke.tucode.accounting.repositories.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class AccountingStartupConfig {

        @Bean
        public CommandLineRunner initializeDefaultAccounts(AccountRepository accountRepo,
                        AccountService accountService) {
                return args -> {
                        List<Account> defaultAccounts = List.of(
                                        Account.builder().name("Cash on Hand").type(AccountType.ASSET)
                                                        .accountCategory(AccountType.CASH)
                                                        .balance(BigDecimal.ZERO).build(),
                                        Account.builder().name("Bank").type(AccountType.ASSET)
                                                        .accountCategory(AccountType.BANK)
                                                        .balance(BigDecimal.ZERO).build(),
                                        Account.builder().name("Mpesa").type(AccountType.ASSET)
                                                        .accountCategory(AccountType.MPESA)
                                                        .balance(BigDecimal.ZERO).build(),
                                        Account.builder().name("Interest Income").type(AccountType.INCOME)
                                                        .accountCategory(AccountType.INCOME).balance(BigDecimal.ZERO)
                                                        .build(),
                                        Account.builder().name("Loan Processing Fees").type(AccountType.INCOME)
                                                        .accountCategory(AccountType.INCOME).balance(BigDecimal.ZERO)
                                                        .build(),
                                        Account.builder().name("Loan Loss Provision").type(AccountType.EXPENSE)
                                                        .accountCategory(AccountType.EXPENSE).balance(BigDecimal.ZERO)
                                                        .build(),
                                        Account.builder().name("Products Value").type(AccountType.ASSET)
                                                        .accountCategory(AccountType.PRODUCTS).balance(BigDecimal.ZERO)
                                                        .build(),
                                        Account.builder().name("Retained Earnings").type(AccountType.EQUITY)
                                                        .accountCategory(AccountType.EQUITY).balance(BigDecimal.ZERO)
                                                        .build());

                        for (Account acc : defaultAccounts) {
                                boolean exists = accountRepo.existsByNameAndAccountCategory(acc.getName(),
                                                acc.getAccountCategory());
                                if (!exists) {
                                        accountService.createAccount(acc);
                                        System.out.println("✔ Account created: " + acc.getName());
                                } else {
                                        System.out.println("ℹ Account already exists: " + acc.getName());
                                }
                        }
                };
        }
}
