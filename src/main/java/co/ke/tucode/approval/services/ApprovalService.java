package co.ke.tucode.approval.services;


import co.ke.tucode.approval.entities.ApprovalRequest;
import co.ke.tucode.approval.entities.ApprovalStep;
import co.ke.tucode.approval.repositories.ApprovalRequestRepository;
import co.ke.tucode.approval.repositories.ApprovalStepRepository;
import co.ke.tucode.systemuser.entities.TRES_User;
import co.ke.tucode.systemuser.repositories.Africana_UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final Africana_UserRepository userRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalStepRepository approvalStepRepository;

    @Transactional
    public ApprovalRequest createApprovalRequest(String title, String description, Long requestedByUserId, List<Long> approverUserIds) {
        TRES_User requestedBy = userRepository.findById(requestedByUserId)
                .orElseThrow(() -> new NoSuchElementException("Requesting user not found"));

        ApprovalRequest request = new ApprovalRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setRequestedBy(requestedBy);
        request.setStatus("PENDING");

        List<ApprovalStep> steps = new ArrayList<>();

        for (int i = 0; i < approverUserIds.size(); i++) {
            TRES_User approver = userRepository.findById(approverUserIds.get(i))
                    .orElseThrow(() -> new NoSuchElementException("Approver user not found"));

            ApprovalStep step = new ApprovalStep();
            step.setApprovalRequest(request);
            step.setApprover(approver);
            step.setStepOrder(i);
            step.setStatus("PENDING");

            steps.add(step);
        }

        request.setSteps(steps);
        return approvalRequestRepository.save(request);
    }

    @Transactional
    public void approveStep(Long requestId, Integer approverId, String remarks) {
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Approval request not found"));

        List<ApprovalStep> steps = request.getSteps();

        ApprovalStep currentStep = steps.stream()
                .filter(s -> s.getApprover().getId().equals(approverId) && s.getStatus().equals("PENDING"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No pending step for this user"));

        Optional<ApprovalStep> nextPending = steps.stream()
                .filter(s -> s.getStatus().equals("PENDING"))
                .findFirst();

        if (nextPending.isEmpty() || !nextPending.get().getId().equals(currentStep.getId())) {
            throw new IllegalStateException("Approval not allowed out of sequence");
        }

        currentStep.setStatus("APPROVED");
        currentStep.setActionDate(LocalDateTime.now());
        currentStep.setRemarks(remarks);

        boolean allApproved = steps.stream().allMatch(s -> s.getStatus().equals("APPROVED"));
        if (allApproved) {
            request.setStatus("APPROVED");
        }

        approvalRequestRepository.save(request);
    }

    @Transactional
    public void rejectStep(Long requestId, Integer approverId, String remarks) {
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Approval request not found"));

        ApprovalStep currentStep = request.getSteps().stream()
                .filter(step -> step.getApprover().getId().equals(approverId) && step.getStatus().equals("PENDING"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No pending step for this user"));

        currentStep.setStatus("REJECTED");
        currentStep.setActionDate(LocalDateTime.now());
        currentStep.setRemarks(remarks);

        request.setStatus("REJECTED");

        approvalRequestRepository.save(request);
    }

    public ApprovalRequest getApprovalRequest(Long requestId) {
        return approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Approval request not found"));
    }

    public List<ApprovalRequest> getRequestsByUser(Integer userId) {
        return approvalRequestRepository.findByRequestedById(userId);
    }
}
