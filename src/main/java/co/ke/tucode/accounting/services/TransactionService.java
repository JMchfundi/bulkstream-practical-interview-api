package co.ke.tucode.accounting.services;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.ke.tucode.accounting.entities.Account;
import co.ke.tucode.accounting.entities.JournalEntry;
import co.ke.tucode.accounting.entities.Transaction;
import co.ke.tucode.accounting.payloads.ReceiptPayload;
import co.ke.tucode.accounting.repositories.AccountRepository;
import co.ke.tucode.accounting.repositories.JournalEntryRepository;
import co.ke.tucode.accounting.repositories.TransactionRepository;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private JournalEntryRepository journalEntryRepo;

    @Autowired
    private AccountRepository accountRepo;

    public Transaction createTransaction(Transaction transaction) {
        return transactionRepo.save(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepo.findAll();
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepo.findById(id).orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    public Transaction updateTransaction(Long id, Transaction updated) {
        Transaction tx = getTransactionById(id);
        tx.setAmount(updated.getAmount());
        tx.setType(updated.getType());
        return transactionRepo.save(tx);
    }

    public void deleteTransaction(Long id) {
        transactionRepo.deleteById(id);
    }

    /**
     * Unified method for saving single or multiple receipt transactions.
     */
    @Transactional
    public List<JournalEntry> saveBatchReceipt(List<ReceiptPayload> receiptPayloads) {
        List<JournalEntry> journalEntries = new ArrayList<>();

        for (ReceiptPayload receiptPayload : receiptPayloads) {
            JournalEntry journalEntry = new JournalEntry();
            journalEntry.setDate(receiptPayload.getReceiptDate() != null
                    ? receiptPayload.getReceiptDate()
                    : LocalDate.now());
            journalEntryRepo.save(journalEntry);

            Account creditAccount = accountRepo.findById(receiptPayload.getPaymentFor())
                    .orElseThrow(() -> new RuntimeException("Credit Account not found for ID: " + receiptPayload.getPaymentFor()));
            Account debitAccount = accountRepo.findById(receiptPayload.getAccount())
                    .orElseThrow(() -> new RuntimeException("Debit Account not found for ID: " + receiptPayload.getAccount()));

            Transaction transaction = new Transaction();
            transaction.setAmount(receiptPayload.getAmount());
            transaction.setDescription("From " + receiptPayload.getReceivedFrom() + " REF " + receiptPayload.getReferenceNumber());
            transaction.setJournalEntry(journalEntry);
            transaction.setDebitAccount(debitAccount);
            transaction.setCreditAccount(creditAccount);

            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("receiptDate", receiptPayload.getReceiptDate() != null ? receiptPayload.getReceiptDate().toString() : "");
            builder.add("receivedFrom", receiptPayload.getReceivedFrom() != null ? receiptPayload.getReceivedFrom() : "");
            builder.add("amountInWords", receiptPayload.getAmountInWords() != null ? receiptPayload.getAmountInWords() : "");
            builder.add("paymentFor", receiptPayload.getPaymentFor() != null ? receiptPayload.getPaymentFor().toString() : "");
            builder.add("paymentMethod", receiptPayload.getAccount() != null ? receiptPayload.getAccount().toString() : "");
            builder.add("referenceNumber", receiptPayload.getReferenceNumber() != null ? receiptPayload.getReferenceNumber() : "");
            builder.add("schemaVersion", 2);

            StringWriter stringWriter = new StringWriter();
            try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
                jsonWriter.writeObject(builder.build());
            }

            transaction.setDetails(stringWriter.toString());

            // Save transaction
            transactionRepo.save(transaction);

            // Update balances
            debitAccount.setBalance(debitAccount.getBalance().add(transaction.getAmount()));
            creditAccount.setBalance(creditAccount.getBalance().subtract(transaction.getAmount()));
            accountRepo.save(debitAccount);
            accountRepo.save(creditAccount);

            journalEntries.add(journalEntry);
        }

        return journalEntries;
    }

    /**
     * Optional single-receipt wrapper for convenience (calls batch method).
     */
    public JournalEntry saveReceipt(ReceiptPayload payload) {
        return saveBatchReceipt(List.of(payload)).get(0);
    }

    public Transaction getTransactionByIdWithDetails(Long id) {
        Transaction transaction = transactionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getDetails() != null) {
            try {
                JsonReader jsonReader = Json.createReader(new StringReader(transaction.getDetails()));
                var detailsJson = jsonReader.readObject();
                jsonReader.close();

                // Optional: reformat or validate JSON
                StringWriter sw = new StringWriter();
                JsonWriter writer = Json.createWriter(sw);
                writer.writeObject(detailsJson);
                writer.close();

                transaction.setDetails(sw.toString());

            } catch (JsonException e) {
                System.err.println("Error parsing JSON details: " + e.getMessage());
            }
        }

        return transaction;
    }
}
