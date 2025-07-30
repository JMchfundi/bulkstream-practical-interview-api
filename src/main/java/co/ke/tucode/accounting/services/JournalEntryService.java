package co.ke.tucode.accounting.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.ke.tucode.accounting.entities.Account;
import co.ke.tucode.accounting.entities.JournalEntry;
import co.ke.tucode.accounting.entities.Transaction;
import co.ke.tucode.accounting.entities.TransactionType;
import co.ke.tucode.accounting.repositories.AccountRepository;
import co.ke.tucode.accounting.repositories.JournalEntryRepository;

@Service
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalRepo;

    @Autowired
    private AccountRepository accountRepo;

    public JournalEntry recordJournalEntry(List<Transaction> transactions) {
        BigDecimal debitTotal = BigDecimal.ZERO;
        BigDecimal creditTotal = BigDecimal.ZERO;

        for (Transaction tx : transactions) {
            if (tx.getType() == TransactionType.DEBIT) {
                debitTotal = debitTotal.add(tx.getAmount());
            } else if (tx.getType() == TransactionType.CREDIT) {
                creditTotal = creditTotal.add(tx.getAmount());
            } else {
                throw new IllegalArgumentException("Unknown transaction type");
            }
        }

        if (debitTotal.compareTo(creditTotal) != 0) {
            throw new IllegalArgumentException("Debits and Credits must match.");
        }

        JournalEntry journal = new JournalEntry();
        journal.setDate(LocalDate.now());

        List<Transaction> updatedTransactions = new ArrayList<>();

        for (Transaction tx : transactions) {
            Account debitAccount = null;
            Account creditAccount = null;

            if (tx.getDebitAccount() != null) {
                debitAccount = accountRepo.findById(tx.getDebitAccount().getId())
                        .orElseThrow(() -> new RuntimeException("Debit account not found"));
                debitAccount.setBalance(debitAccount.getBalance().add(tx.getAmount()));
                accountRepo.save(debitAccount);
                tx.setDebitAccount(debitAccount);
            }

            if (tx.getCreditAccount() != null) {
                creditAccount = accountRepo.findById(tx.getCreditAccount().getId())
                        .orElseThrow(() -> new RuntimeException("Credit account not found"));
                creditAccount.setBalance(creditAccount.getBalance().subtract(tx.getAmount()));
                accountRepo.save(creditAccount);
                tx.setCreditAccount(creditAccount);
            }

            tx.setJournalEntry(journal);
            updatedTransactions.add(tx);
        }

        journal.setTransactions(updatedTransactions);
        return journalRepo.save(journal);
    }

    public List<JournalEntry> getAllJournalEntries() {
        return journalRepo.findAll();
    }

    public JournalEntry getJournalEntry(Long id) {
        return journalRepo.findById(id).orElseThrow(() -> new RuntimeException("Journal entry not found"));
    }

    public void deleteJournalEntry(Long id) {
        journalRepo.deleteById(id);
    }
}
