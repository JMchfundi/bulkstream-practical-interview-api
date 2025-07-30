package co.ke.finsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.ke.finsis.entity.DocumentUpload;
import co.ke.finsis.entity.NextOfKin;

public interface NextOfKinRepository extends JpaRepository<NextOfKin, Long> {
}
