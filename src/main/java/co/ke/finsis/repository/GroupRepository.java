package co.ke.finsis.repository;

import co.ke.finsis.entity.Group;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByGroupName(String groupName);
}
