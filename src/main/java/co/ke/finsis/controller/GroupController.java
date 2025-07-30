package co.ke.finsis.controller;

import co.ke.finsis.payload.GroupDTO;
import co.ke.finsis.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(@RequestBody GroupDTO dto) {
        return ResponseEntity.ok(groupService.createGroup(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<GroupDTO> getGroup(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @GetMapping
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupDTO> updateGroup(@PathVariable Long id, @RequestBody GroupDTO dto) {
        return ResponseEntity.ok(groupService.updateGroup(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    // GET by group name
    @GetMapping("/by-name/{groupName}")
    public ResponseEntity<GroupDTO> getByGroupName(@PathVariable String groupName) {
        return groupService.getGroupByName(groupName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT by group name
    @PutMapping("/by-name/{groupName}")
    public ResponseEntity<GroupDTO> updateByGroupName(@PathVariable String groupName, @RequestBody GroupDTO dto) {
        return groupService.updateGroupByName(groupName, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE by group name
    @DeleteMapping("/by-name/{groupName}")
    public ResponseEntity<Void> deleteByGroupName(@PathVariable String groupName) {
        return groupService.deleteGroupByName(groupName)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/batch")
    public ResponseEntity<List<GroupDTO>> batchUpdateGroups(@RequestBody List<GroupDTO> dtos) {
        List<GroupDTO> updatedGroups = groupService.updateGroupsBatch(dtos);
        return ResponseEntity.ok(updatedGroups);
    }
}
