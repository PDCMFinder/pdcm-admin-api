package org.cancermodels.releases.modelSummary;

import org.cancermodels.filters.PredicateBuilder;
import org.cancermodels.pdcm_admin.persistance.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class helps to create Specifications with the needed predicates to filter records in the
 * ModelSummary table.
 */
@Component
public class ModelSummarySpecs {

    public static Specification<ModelSummary> allModels()
    {
        return Specification.where(null);
    }

    public static Specification<ModelSummary> paediatricModels() {
        Specification.where(null);
        Specification<ModelSummary> specification;
        specification = (root, query, criteriaBuilder) -> {
            Path<String> paediatricPath = root.get(ModelSummary_.PAEDIATRIC);
            query.distinct(true);
            return criteriaBuilder.equal(paediatricPath, true);
        };

        return specification;
    }
    public static Specification<ModelSummary> withModelType(List<String> modelType)
    {
        Specification<ModelSummary> specification = Specification.where(null);
        if (modelType != null)
        {
            specification = (root, query, criteriaBuilder) -> {
                Path<String> modelTypePath = root.get(ModelSummary_.MODEL_TYPE);
                query.distinct(true);
                return PredicateBuilder.addLowerInPredicates(
                    criteriaBuilder, modelTypePath, modelType);
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
