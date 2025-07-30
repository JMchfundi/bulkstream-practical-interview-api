package co.ke.finsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.ke.finsis.entity.LoanType;

public interface LoanTypeRepository extends JpaRepository<LoanType, Long> {
}
