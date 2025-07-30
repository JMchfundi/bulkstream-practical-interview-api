package co.ke.tucode.accounting.services;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.ke.tucode.accounting.entities.Account;
import co.ke.tucode.accounting.entities.AccountType;
import co.ke.tucode.accounting.entities.JournalEntry;
import co.ke.tucode.accounting.entities.Transaction;
import co.ke.tucode.accounting.payloads.AccountStatementEntry;
import co.ke.tucode.accounting.repositories.AccountRepository;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepo;

    public Account createAccount(Account account) {
        String nextCode = generateNextCode(account.getType());
        account.setCode(nextCode);
        return accountRepo.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepo.findAll();
    }

    public Account getAccountById(Long id) {
        return accountRepo.findById(id).orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public Account updateAccount(Long id, Account updated) {
        Account acc = getAccountById(id);
        acc.setName(updated.getName());
        acc.setType(updated.getType());
        return accountRepo.save(acc);
    }

    public void deleteAccount(Long id) {
        accountRepo.deleteById(id);
    }

    private String generateNextCode(AccountType type) {
        int prefix = switch (type) {
            case ASSET -> 1;
            case LIABILITY -> 2;
            case INCOME -> 3;
            case EXPENSE -> 4;
            case EQUITY -> 5;
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };

        List<Account> accounts = accountRepo.findTopByTypeOrderByCodeDesc(type);

        for (Account account : accounts) {
            String code = account.getCode();
            if (code.matches("\\d+")) { // Only process purely numeric codes
                int next = Integer.parseInt(code) + 1;
                return String.format("%04d", next);
            }
        }
        return prefix + "001"; // e.g., 1001

    }

    public List<AccountStatementEntry> generateAccountStatement(Long accountId, LocalDate startDate,
            LocalDate endDate) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Collect unique transactions the account is involved in
        Set<Transaction> uniqueTransactions = new HashSet<>();
        uniqueTransactions.addAll(account.getDebitTransactions());
        uniqueTransactions.addAll(account.getCreditTransactions());

        // Sort transactions by journal entry number, then by date (both null-safe)
        List<Transaction> transactions = new ArrayList<>(uniqueTransactions);
        transactions.sort(Comparator
                .comparing((Transaction tx) -> {
                    JournalEntry je = tx.getJournalEntry();
                    return je != null && je.getId() != null ? je.getId() : Long.MIN_VALUE;
                })
                .thenComparing(tx -> {
                    JournalEntry je = tx.getJournalEntry();
                    return je != null && je.getDate() != null ? je.getDate() : LocalDate.MIN;
                }));

        List<AccountStatementEntry> statement = new ArrayList<>();
        BigDecimal openingBalance = BigDecimal.ZERO;

        // Step 1: Calculate opening balance from transactions before startDate
        for (Transaction tx : transactions) {
            Long debitId = tx.getDebitAccount() != null ? tx.getDebitAccount().getId() : null;
            Long creditId = tx.getCreditAccount() != null ? tx.getCreditAccount().getId() : null;

            boolean isDebit = accountId.equals(debitId);
            boolean isCredit = accountId.equals(creditId);

            if (!isDebit && !isCredit)
                continue;
            if (isDebit && isCredit)
                continue;

            LocalDate date = tx.getJournalEntry() != null ? tx.getJournalEntry().getDate() : null;
            if (date == null || !date.isBefore(startDate))
                continue;

            BigDecimal debit = isDebit ? tx.getAmount() : BigDecimal.ZERO;
            BigDecimal credit = isCredit ? tx.getAmount() : BigDecimal.ZERO;

            openingBalance = openingBalance.add(debit).subtract(credit);
        }

        // Step 2: Add Opening Balance entry with null date
        BigDecimal runningBalance = openingBalance;
        statement.add(new AccountStatementEntry(
                null,
                "Opening Balance",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                runningBalance));

        // Step 3: Add statement entries within the selected date range
        for (Transaction tx : transactions) {
            Long debitId = tx.getDebitAccount() != null ? tx.getDebitAccount().getId() : null;
            Long creditId = tx.getCreditAccount() != null ? tx.getCreditAccount().getId() : null;

            boolean isDebit = accountId.equals(debitId);
            boolean isCredit = accountId.equals(creditId);

            if (!isDebit && !isCredit)
                continue;
            if (isDebit && isCredit)
                continue;

            LocalDate date = tx.getJournalEntry() != null ? tx.getJournalEntry().getDate() : null;
            if (date == null || date.isBefore(startDate) || date.isAfter(endDate))
                continue;

            String description = tx.getDescription();
            BigDecimal debit = isDebit ? tx.getAmount() : BigDecimal.ZERO;
            BigDecimal credit = isCredit ? tx.getAmount() : BigDecimal.ZERO;

            runningBalance = runningBalance.add(debit).subtract(credit);

            statement.add(new AccountStatementEntry(
                    date,
                    description,
                    debit,
                    credit,
                    runningBalance));
        }

        return statement;
    }

    public byte[] generateLedgerReport(List<AccountStatementEntry> statementEntries) throws Exception {
        try (
                InputStream ledgerReportStream = this.getClass().getResourceAsStream("/reports/ledger_report.jrxml");
                InputStream headerComponentStream = this.getClass()
                        .getResourceAsStream("/reports/HeaderComponent.jrxml")) {
            if (ledgerReportStream == null) {
                throw new Exception("Could not find ledger_report.jrxml in classpath!");
            }

            if (headerComponentStream == null) {
                throw new Exception("Could not find HeaderComponent.jrxml in classpath!");
            }

            // Compile both reports
            JasperReport ledgerReport = JasperCompileManager.compileReport(ledgerReportStream);
            JasperReport headerComponent = JasperCompileManager.compileReport(headerComponentStream);

            // Prepare data source
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(statementEntries);

            // Parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("createdBy", "Your Company");
            // parameters.put("reportTitle", "Ledger Report");
            parameters.put("reportDate", java.sql.Date.valueOf(LocalDate.now()));

            // ✅ Inject the compiled header into the main report
            parameters.put("SUBREPORT_HEADER", headerComponent);

            // Fill and export
            JasperPrint jasperPrint = JasperFillManager.fillReport(ledgerReport, parameters, dataSource);
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (JRException e) {
            System.err.println("JasperReports error: " + e.getMessage());
            throw e;
        }
    }

}
