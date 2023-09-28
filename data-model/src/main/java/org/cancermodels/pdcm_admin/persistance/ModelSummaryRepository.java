package org.cancermodels.pdcm_admin.persistance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ModelSummaryRepository extends JpaRepository<ModelSummary, Long>,
    JpaSpecificationExecutor<ModelSummary> {
    List<ModelSummary> findAllByRelease(Release release);
    Page<ModelSummary> findByReleaseId(Long id, Pageable pageable);
}
