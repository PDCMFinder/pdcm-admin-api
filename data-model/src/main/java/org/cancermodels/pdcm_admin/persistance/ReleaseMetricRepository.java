package org.cancermodels.pdcm_admin.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ReleaseMetricRepository extends JpaRepository<ReleaseMetric, Long>,
    JpaSpecificationExecutor<ReleaseMetric> {
    List<ReleaseMetric> findAllByRelease(Release release);
}
