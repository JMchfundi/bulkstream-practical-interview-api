package co.ke.tucode.logs.payloads;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityLogDto {
    private String username;
    private String module;
    private String activity;
    private String details;
    private String entityType;
    private Long entityId;
    private String clientTimestamp; // ADD THIS FIELD: Will hold the ISO 8601 string of the UTC Instant

}
