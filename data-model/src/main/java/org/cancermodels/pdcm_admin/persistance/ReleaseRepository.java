package org.cancermodels.pdcm_admin.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReleaseRepository extends JpaRepository<Release, Long>,
    JpaSpecificationExecutor<Release> {
    Optional<Release> findById(long id);
    Optional<Release> findByNameAndDate(String name, LocalDateTime dateTime);
}
