package org.cancermodels.releases.modelSummary;

import org.cancermodels.filters.PredicateBuilder;
import org.cancermodels.pdcm_admin.persistance.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class helps to create Specifications with the needed predicates to filter records in the
 * ModelSummary table.
 */
@Component
public class ModelSummarySpecs {
    public static Specification<ModelSummary> withModelType(List<String> modelType)
    {
        Specification<ModelSummary> specification = Specification.where(null);
        if (modelType != null)
        {
            specification = (Specification<ModelSummary>) (root, query, criteriaBuilder) -> {
                Path<String> statusPath = root.get(ModelSummary_.MODEL_TYPE);
                query.distinct(true);
                return PredicateBuilder.addLowerInPredicates(
                    criteriaBuilder, statusPath, modelType);
            };
        }
        return specification;
    }

    public static Specification<ModelSummary> withReleaseId(List<String> releaseIds) {
        Specification<ModelSummary> specification = Specification.where(null);
        if (releaseIds != null)
        {
            List<Long> idsAsLongs = releaseIds.stream().map(Long::parseLong).collect(Collectors.toList());
            specification = (root, query, criteriaBuilder) -> {
                Path<Release> releasePath = root.get(ModelSummary_.RELEASE);
                Path<Long> releaseIdPath = releasePath.get(Release_.id);
                query.distinct(true);
                return PredicateBuilder.addLongInPredicates(
                    criteriaBuilder, releaseIdPath, idsAsLongs);
            };
        }
        return specification;
    }
}
