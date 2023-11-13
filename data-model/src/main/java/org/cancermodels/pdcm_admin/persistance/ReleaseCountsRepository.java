package org.cancermodels.pdcm_admin.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReleaseCountsRepository extends JpaRepository<ReleaseCounts, Long>,
    JpaSpecificationExecutor<ReleaseCounts> {
}
