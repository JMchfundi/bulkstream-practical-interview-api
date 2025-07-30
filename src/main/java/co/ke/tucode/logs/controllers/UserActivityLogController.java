package co.ke.tucode.logs.controllers;

import co.ke.tucode.logs.payloads.UserActivityLogDto;
import co.ke.tucode.logs.services.UserActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
@RequiredArgsConstructor
public class UserActivityLogController {

    private final UserActivityLogService logService;

    /**
     * Get all logs (latest first)
     */
    @GetMapping
    public ResponseEntity<List<UserActivityLogDto>> getAllLogs() {
        return ResponseEntity.ok(logService.getAllLogs());
    }

    /**
     * Get logs performed by a specific username
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<UserActivityLogDto>> getLogsByUser(@PathVariable String username) {
        return ResponseEntity.ok(logService.getLogsByUser(username));
    }

    /**
     * Get logs related to a specific controller/module
     * Example: "UserController", "GroupController"
     */
    @GetMapping("/module/{module}")
    public ResponseEntity<List<UserActivityLogDto>> getLogsByModule(@PathVariable String module) {
        return ResponseEntity.ok(logService.getLogsByModule(module));
    }

    /**
     * Get logs associated with a specific entity (e.g., a user, group, loan etc.)
     * entityType should match the return type's simple class name, e.g. "UserDto"
     */
    @GetMapping("/entity")
    public ResponseEntity<List<UserActivityLogDto>> getLogsByEntity(
            @RequestParam String entityType,
            @RequestParam Long entityId) {
        return ResponseEntity.ok(logService.getLogsByEntity(entityType, entityId));
    }
}
