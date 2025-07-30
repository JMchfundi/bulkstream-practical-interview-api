package co.ke.tucode.logs.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.ke.tucode.logs.entities.UserActivityLog;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    List<UserActivityLog> findByUsernameOrderByTimestampDesc(String username);

    List<UserActivityLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<UserActivityLog> findByModule(String module);
}
