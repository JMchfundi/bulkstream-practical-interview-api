package co.ke.finsis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.ke.finsis.entity.ClientInfo;

public interface ClientInfoRepository extends JpaRepository<ClientInfo, Long> {
    // You can add custom query methods here if needed
Optional<ClientInfo> findByIdNumber(String idNumber);
}
