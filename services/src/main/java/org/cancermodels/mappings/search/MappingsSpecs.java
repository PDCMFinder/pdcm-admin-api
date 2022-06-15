package org.cancermodels.mappings.search;

import java.util.List;
import javax.persistence.criteria.Path;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntity_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class MappingsSpecs {


  /**
   * Get all the projects which related genes have the marker symbols defined in parameter
   * markerSymbols.
   *
   * @param markerSymbols List of names of the marker symbols
   * @return The found projects. If markerSymbols is null then not filter is applied.
   */
  public static Specification<MappingEntity> withStatus(List<String> status)
  {
    Specification<MappingEntity> specification = Specification.where(null);
    if (status != null)
    {
      specification = (Specification<MappingEntity>) (root, query, criteriaBuilder) -> {
        Path<String> statusPath = root.get(MappingEntity_.status);
        query.distinct(true);
        return PredicateBuilder.addInPredicates(
            criteriaBuilder, statusPath, status);
      };
    }
    return specification;
  }
}
