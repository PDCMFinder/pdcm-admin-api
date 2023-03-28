package org.cancermodels.pdcm_admin.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ModelSummaryRepository extends JpaRepository<ModelSummary, Long>,
    JpaSpecificationExecutor<ModelSummary> {
    List<ModelSummary> findAllByRelease(Release release);
}
