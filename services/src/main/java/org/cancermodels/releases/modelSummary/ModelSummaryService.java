package org.cancermodels.releases.modelSummary;

import lombok.extern.slf4j.Slf4j;
import org.cancermodels.filters.Facet;
import org.cancermodels.mappings.search.MappingsFilter;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.pdcm_admin.persistance.ModelSummary;
import org.cancermodels.pdcm_admin.persistance.ModelSummaryRepository;
import org.cancermodels.pdcm_admin.persistance.Release;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class ModelSummaryService {
    private final ModelSummaryRepository modelSummaryRepository;
    private final ModelSummaryQueryService modelSummaryQueryService;

    public ModelSummaryService(ModelSummaryRepository modelSummaryRepository, ModelSummaryQueryService modelSummaryQueryService) {
        this.modelSummaryRepository = modelSummaryRepository;
        this.modelSummaryQueryService = modelSummaryQueryService;
    }

    public List<Facet> getFacetsForModels(Release release) {
        List<Facet> facets = new ArrayList<>();
        Map<String, Set<String>> facetsMap = new LinkedHashMap<>();
        List<ModelSummary> models = modelSummaryRepository.findAllByRelease(release);
        for (ModelSummary modelSummary : models) {
            updateUniqueValuesMap(facetsMap, ModelSummaryFilterTypes.MODEL_TYPE.getName(), modelSummary.getModelType());
            updateUniqueValuesMap(facetsMap, ModelSummaryFilterTypes.DATA_SOURCE.getName(), modelSummary.getDataSource());
            updateUniqueValuesMap(facetsMap, ModelSummaryFilterTypes.PROJECT.getName(), modelSummary.getProjectName());
        }
        for (var entry : facetsMap.entrySet()) {
            Facet facet = new Facet(entry.getKey(), entry.getValue());
            facets.add(facet);
        }
        return facets;
    }

    private void updateUniqueValuesMap(Map<String, Set<String>> facetsMap, String facetName, String value) {
        facetsMap.putIfAbsent(facetName, new HashSet<>());
        facetsMap.get(facetName).add(value);
    }

    public void saveAll(List<ModelSummary> modelSummaries) {
        modelSummaryRepository.saveAll(modelSummaries);
    }

    public List<ModelSummary> findAllByRelease(Release release) {
        return modelSummaryRepository.findAllByRelease(release);
    }

    public void deleteAll(List<ModelSummary> modelSummaries) {
        modelSummaryRepository.deleteAll(modelSummaries);
    }

    public Page<ModelSummary> findByReleaseId(Long releaseId, Pageable pageable) {
        return modelSummaryRepository.findByReleaseId(releaseId, pageable);
    }

    public Page<ModelSummary> search(Pageable pageable, ModelSummaryFilter modelSummaryFilter) {
        return modelSummaryQueryService.search(pageable, modelSummaryFilter);
    }
}
