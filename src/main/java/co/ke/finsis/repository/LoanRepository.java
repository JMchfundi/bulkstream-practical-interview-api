package co.ke.finsis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.ke.finsis.entity.Loan;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    @Query("""
                SELECT l FROM Loan l
                JOIN l.approvalRequest ar
                JOIN ar.steps s
                WHERE ar.status = 'PENDING'
                AND s.status = 'PENDING'
                AND s.stepOrder = (
                    SELECT MIN(s2.stepOrder) FROM ApprovalStep s2
                    WHERE s2.approvalRequest.id = ar.id AND s2.status = 'PENDING'
                )
                AND s.approver.id = :approverId
            """)
    List<Loan> findLoansPendingApprovalByApprover(@Param("approverId") Long approverId);

}
