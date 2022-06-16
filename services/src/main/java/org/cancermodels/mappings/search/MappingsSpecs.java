package org.cancermodels.mappings.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntity_;
import org.cancermodels.MappingKey;
import org.cancermodels.MappingKey_;
import org.cancermodels.MappingValue;
import org.cancermodels.MappingValue_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class MappingsSpecs {

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

  /**
   *
   * @param mappingQuery Map of labels and values to use in the search. For instance:
   *                     {
   *                        "DataSource": ['trace'],
   *                        "TumorType" : ['Primary']
   *                     }
   * @return A specification that can be used to search by the label/values information given
   * as parameter
   */
  public static Specification<MappingEntity> withMappingQuery(Map<String, List<String>> mappingQuery)
  {
    Specification<MappingEntity> specification = Specification.where(null);
    if (mappingQuery != null)
    {
      specification =
          (Specification<MappingEntity>)
              (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                for (String key : mappingQuery.keySet()) {

                  Subquery<Long> subQuery = query.subquery(Long.class);
                  Root<MappingEntity> subRoot = subQuery.from(MappingEntity.class);

                  ListJoin<MappingEntity, MappingValue> subEntityValuesJoin =
                      subRoot.join(MappingEntity_.mappingValues);
                  Path<String> subMappingValuePath = subEntityValuesJoin.get(MappingValue_.value);
                  Path<MappingKey> subMappingKeyPath = subEntityValuesJoin.get(MappingValue_.mappingKey);
                  Path<String> subKeyValuePath = subMappingKeyPath.get(MappingKey_.key);

                  Predicate subKeyValuesPredicate = subKeyValuePath.in(key);
                  Predicate subMappingValuesPredicate = subMappingValuePath.in(mappingQuery.get(key));
                  Predicate subKeyAndValuePredicate = criteriaBuilder.and(
                      subKeyValuesPredicate, subMappingValuesPredicate);

                  subQuery = subQuery.select(subRoot.get(MappingEntity_.ID)).where(subKeyAndValuePredicate);
                  predicates.add(root.get(MappingEntity_.ID).in(subQuery));
                }
                query.distinct(true);
               return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
              };
    }
    return specification;
  }
}
