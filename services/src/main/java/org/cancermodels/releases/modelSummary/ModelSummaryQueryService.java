package org.cancermodels.releases.modelSummary;

import org.cancermodels.pdcm_admin.persistance.ModelSummary;
import org.cancermodels.pdcm_admin.persistance.ModelSummaryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ModelSummaryQueryService {

    private final ModelSummaryRepository modelSummaryRepository;

    public ModelSummaryQueryService(ModelSummaryRepository modelSummaryRepository) {
        this.modelSummaryRepository = modelSummaryRepository;
    }

    public Page<ModelSummary> search(Pageable pageable, ModelSummaryFilter modelSummaryFilter) {
        Specification<ModelSummary> specs = buildSpecifications(modelSummaryFilter);
        return modelSummaryRepository.findAll(specs, pageable);
    }

    private Specification<ModelSummary> buildSpecifications(ModelSummaryFilter modelSummaryFilter) {
        return Specification.where(
            ModelSummarySpecs.withModelType(modelSummaryFilter.getModelTypes()).and(
                ModelSummarySpecs.withReleaseId(modelSummaryFilter.getReleaseIds())
            )
        );
    }
}
