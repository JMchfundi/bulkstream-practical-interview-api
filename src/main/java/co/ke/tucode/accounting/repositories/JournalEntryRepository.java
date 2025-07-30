package co.ke.tucode.accounting.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import co.ke.tucode.accounting.entities.JournalEntry;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
}
