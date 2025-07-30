package co.ke.tucode.accounting.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import co.ke.tucode.accounting.entities.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
