package co.ke.tucode.approval.entities;

import java.util.List;

import co.ke.tucode.systemuser.entities.TRES_User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "approval_requests")
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String status; // PENDING, APPROVED, REJECTED

    @ManyToOne
    @JoinColumn(name = "requested_by_id")
    private TRES_User requestedBy;

    @OneToMany(mappedBy = "approvalRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<ApprovalStep> steps;
}
