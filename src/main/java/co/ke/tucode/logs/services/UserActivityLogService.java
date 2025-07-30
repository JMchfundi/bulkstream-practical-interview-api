package co.ke.tucode.logs.services;

import co.ke.tucode.logs.entities.UserActivityLog;
import co.ke.tucode.logs.payloads.UserActivityLogDto;
import co.ke.tucode.logs.repositories.UserActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserActivityLogService {

    private final UserActivityLogRepository logRepository;

    // ✅ Existing logging method
    public void log(String username, String module, String activity, String details, String entityType, Long entityId) {
        UserActivityLog log = UserActivityLog.builder()
                .username(username)
                .module(module)
                .activity(activity)
                .details(details)
                .timestamp(Instant.now())
                .entityType(entityType)
                .entityId(entityId)
                .build();

        logRepository.save(log);
    }

    // ✅ Private mapper from Entity to DTO
    private UserActivityLogDto mapToDto(UserActivityLog log) {
        return UserActivityLogDto.builder()
                .username(log.getUsername())
                .module(log.getModule())
                .activity(log.getActivity())
                .details(log.getDetails())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .clientTimestamp(log.getTimestamp().toString())

                .build();
    }

    // ✅ Get all logs (sorted by timestamp desc)
    public List<UserActivityLogDto> getAllLogs() {
        return logRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"))
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ✅ Get logs by user
    public List<UserActivityLogDto> getLogsByUser(String username) {
        return logRepository.findByUsernameOrderByTimestampDesc(username)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ✅ Get logs by module
    public List<UserActivityLogDto> getLogsByModule(String module) {
        return logRepository.findByModule(module)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ✅ Get logs by entity type and ID
    public List<UserActivityLogDto> getLogsByEntity(String entityType, Long entityId) {
        return logRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}
