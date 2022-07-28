package org.cancermodels.suggestions.index;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.Query;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingValue;
import org.springframework.stereotype.Component;

/**
 * This class creates the queries that a Mapping Entity needs to search for suggestions
 * in a lucene index
 */
@Component
public class MappingEntityQueryBuilder {

  private final RulesQueryBuilder rulesQueryBuilder;
  private final OntologyQueryBuilder ontologyQueryBuilder;

  public MappingEntityQueryBuilder(
      RulesQueryBuilder rulesQueryBuilder, OntologyQueryBuilder ontologyQueryBuilder) {
    this.rulesQueryBuilder = rulesQueryBuilder;
    this.ontologyQueryBuilder = ontologyQueryBuilder;
  }

  /**
   * Builds the query to search for suggestions in rules and ontologies.
   * @param mappingEntity Mapping entity.
   * @return Query to be executed on the lucene index.
   */
  public Query buildSuggestionQuery(MappingEntity mappingEntity) throws IOException {

    List<MappingValue> mappingValues = mappingEntity.getMappingValues();
    mappingValues = MappingValueConfHelper.getFormattedValues(mappingValues);
    Query ruleQuery = rulesQueryBuilder.buildRulesQuery(mappingValues);
    Query ontologyQuery = ontologyQueryBuilder.buildOntologiesQuery(mappingValues);

    return ensembleFinalQuery(ruleQuery, ontologyQuery);
  }

  Query ensembleFinalQuery(Query ruleQuery, Query ontologyQuery) {
    return new DisjunctionMaxQuery(Arrays.asList(ruleQuery, ontologyQuery), 1);
  }

}
