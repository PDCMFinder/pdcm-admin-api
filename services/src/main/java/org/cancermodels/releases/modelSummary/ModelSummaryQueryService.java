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

    /**
     * Searches for ModelSummary entities based on the provided view name, pagination settings, and filtering criteria.
     *
     * @param viewName           An identifier to decide what data to show (not a real view in the db).
     *                           - "allModels": All models in the release
     *                           - "paediatricModels": Paediatric models in the release
     * @param pageable           Pageable object containing pagination settings (page number, page size, sort order).
     * @param modelSummaryFilter Filter criteria to apply to the search query.
     * @return A Page of ModelSummary entities that match the specified criteria.
     */
    public Page<ModelSummary> search(String viewName, Pageable pageable, ModelSummaryFilter modelSummaryFilter) {
        Specification<ModelSummary> specs = buildSpecifications(viewName, modelSummaryFilter);
        return modelSummaryRepository.findAll(specs, pageable);
    }

    private Specification<ModelSummary> buildSpecifications(String viewName, ModelSummaryFilter modelSummaryFilter) {
        Specification<ModelSummary> specification;
        specification = Specification.where(
            ModelSummarySpecs.withModelType(modelSummaryFilter.getModelTypes()).and(
                ModelSummarySpecs.withReleaseId(modelSummaryFilter.getReleaseIds())
            )
        );
        if ("paediatricModels".equals(viewName)) {
            specification = specification.and(ModelSummarySpecs.paediatricModels());
        }
        return specification;
    }
}
