package co.ke.tucode.approval.entities;

import java.time.LocalDateTime;

import co.ke.tucode.systemuser.entities.TRES_User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "approval_request_id")
    private ApprovalRequest approvalRequest;

    @ManyToOne
    @JoinColumn(name = "approver__id") // explicitly specify
    private TRES_User approver;

    private int stepOrder;

    private String status; // PENDING, APPROVED, REJECTED

    private LocalDateTime actionDate;
    private String remarks;
}
