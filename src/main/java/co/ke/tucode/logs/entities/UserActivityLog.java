package co.ke.tucode.logs.entities;

import lombok.*;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String module;

    private String activity;

    private String details;

    private Instant timestamp;

        // Dynamic polymorphic target
    private String entityType; // e.g., "User", "Loan"
    private Long entityId;     // e.g., 123L
}
