package org.cancermodels.mappings.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.cancermodels.EntityType;
import org.cancermodels.EntityType_;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntity_;
import org.cancermodels.MappingKey;
import org.cancermodels.MappingKey_;
import org.cancermodels.MappingValue;
import org.cancermodels.MappingValue_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * This class helps to create Specifications with the needed predicates to filter records in a
 * database search.
 */
@Component
public class MappingsSpecs {

  /**
   * Creates the conditions in the WHERE to filter using a list of status
   * @param status List of status to use in the filter
   * @return Specification with the predicate: MappingEntity_.status in (status)
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

  /**
   * Creates the conditions in the WHERE to filter using a list of entity type names
   * @param entityTypeNames List of status to use in the filter
   * @return Specification with the predicate: MappingEntity_.entityType.name in (entityTypeNames)
   */
  public static Specification<MappingEntity> withEntityTypeNames(List<String> entityTypeNames)
  {
    Specification<MappingEntity> specification = Specification.where(null);
    if (entityTypeNames != null)
    {
      specification =
          (Specification<MappingEntity>)
              (root, query, criteriaBuilder) -> {
                Path<EntityType> entityPath = root.get(MappingEntity_.entityType);
                Path<String> entityTypeName = entityPath.get(EntityType_.name);
                query.distinct(true);
                return PredicateBuilder.addInPredicates(criteriaBuilder, entityTypeName, entityTypeNames);
              };
    }
    return specification;
  }

  /**
   * Creates the conditions in the WHERE to filter using a labels(keys) and values.
   * Because those elements are not columns but records, it's a bit more complex to use them in a
   * query. This solution creates the predicated as subqueries.
   * The main query gets the MappingEntity and each subquery brings the ids of the entities that
   * match every pair of label-value. That allows us to have a query asking for two labels at the
   * same time. Example
   *    select * from mapping_entity me
   *    where me.id in (... select mapping entity id where it's label/key is 'DataSource' and its value is 'trace')
   *    AND   me.id in (... select mapping entity id where it's label/key is 'TumorType' and its value is 'primary')
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

                // For every key and its list of values, let's create a subquery
                for (String key : mappingQuery.keySet()) {

                  Subquery<Long> subQuery = query.subquery(Long.class);
                  Root<MappingEntity> subRoot = subQuery.from(MappingEntity.class);

                  // Joins for each subquery
                  ListJoin<MappingEntity, MappingValue> subEntityValuesJoin =
                      subRoot.join(MappingEntity_.mappingValues);
                  Path<String> subMappingValuePath = subEntityValuesJoin.get(MappingValue_.value);
                  Path<MappingKey> subMappingKeyPath = subEntityValuesJoin.get(MappingValue_.mappingKey);
                  Path<String> subKeyValuePath = subMappingKeyPath.get(MappingKey_.key);

                  // Apply the conditions to key and value
                  Predicate subKeyValuesPredicate = subKeyValuePath.in(key);
                  Predicate subMappingValuesPredicate = subMappingValuePath.in(mappingQuery.get(key));
                  Predicate subKeyAndValuePredicate = criteriaBuilder.and(
                      subKeyValuesPredicate, subMappingValuesPredicate);

                  subQuery = subQuery.select(subRoot.get(MappingEntity_.ID)).where(subKeyAndValuePredicate);
                  // Add the subquery to the main query using an "in" statement
                  predicates.add(root.get(MappingEntity_.ID).in(subQuery));
                }
                query.distinct(true);
               return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
              };
    }
    return specification;
  }
}
