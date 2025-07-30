package co.ke.finsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.ke.finsis.entity.DocumentUpload;

public interface DocumentUploadRepository extends JpaRepository<DocumentUpload, Long> {
}
