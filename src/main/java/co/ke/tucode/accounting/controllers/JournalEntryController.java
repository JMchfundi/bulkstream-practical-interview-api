package co.ke.tucode.accounting.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.ke.tucode.accounting.entities.JournalEntry;
import co.ke.tucode.accounting.entities.Transaction;
import co.ke.tucode.accounting.services.JournalEntryService;

@RestController
@RequestMapping("/api/journals")
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalService;

    @PostMapping
    public ResponseEntity<JournalEntry> create(@RequestBody List<Transaction> txList) {
        return ResponseEntity.ok(journalService.recordJournalEntry(txList));
    }

    @GetMapping
    public ResponseEntity<List<JournalEntry>> getAll() {
        return ResponseEntity.ok(journalService.getAllJournalEntries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalEntry> getById(@PathVariable Long id) {
        return ResponseEntity.ok(journalService.getJournalEntry(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        journalService.deleteJournalEntry(id);
        return ResponseEntity.noContent().build();
    }
}
