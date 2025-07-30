package co.ke.finsis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.ke.finsis.entity.Station;

public interface StationRepository extends JpaRepository<Station, Long> {

    Optional<Station> findByLevelType(String string);
}
