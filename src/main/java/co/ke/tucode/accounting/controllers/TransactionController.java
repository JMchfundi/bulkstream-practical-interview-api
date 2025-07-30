package co.ke.tucode.accounting.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.ke.tucode.accounting.entities.JournalEntry;
import co.ke.tucode.accounting.entities.Transaction;
import co.ke.tucode.accounting.payloads.ReceiptPayload;
import co.ke.tucode.accounting.services.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody Transaction tx) {
        return ResponseEntity.ok(transactionService.createTransaction(tx));
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAll() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> update(@PathVariable Long id, @RequestBody Transaction tx) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, tx));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Endpoint for single receipt
    @PostMapping("/receipt")
    public ResponseEntity<JournalEntry> saveReceipt(@RequestBody ReceiptPayload receiptPayload) {
        return ResponseEntity.ok(transactionService.saveReceipt(receiptPayload));
    }

    // ✅ Endpoint for batch receipts
    @PostMapping("/receipt/batch")
    public ResponseEntity<List<JournalEntry>> saveBatchReceipt(@RequestBody List<ReceiptPayload> receiptPayloads) {
        return ResponseEntity.ok(transactionService.saveBatchReceipt(receiptPayloads));
    }
}
