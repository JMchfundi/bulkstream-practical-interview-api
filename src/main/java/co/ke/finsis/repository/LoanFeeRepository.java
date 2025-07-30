package co.ke.finsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.ke.finsis.entity.LoanFee;

@Repository
public interface LoanFeeRepository extends JpaRepository<LoanFee, Long> {
}
