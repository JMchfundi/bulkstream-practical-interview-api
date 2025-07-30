package co.ke.tucode.approval.repositories;


import org.springframework.data.jpa.repository.JpaRepository;

import co.ke.tucode.approval.entities.ApprovalRequest;

import java.util.List;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
    
    // Find all requests created by a specific user
    List<ApprovalRequest> findByRequestedById(Integer userId);
}
