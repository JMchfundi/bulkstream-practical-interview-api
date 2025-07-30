package co.ke.tucode.approval.repositories;


import org.springframework.data.jpa.repository.JpaRepository;

import co.ke.tucode.approval.entities.ApprovalStep;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
}
