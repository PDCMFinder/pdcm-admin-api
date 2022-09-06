package org.cancermodels.suggestions.search_engine;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.Query;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingValue;
import org.cancermodels.suggestions.search_engine.util.Constants;
import org.springframework.stereotype.Component;

/**
 * This class creates the queries that a Mapping Entity needs to search for suggestions
 * in a lucene index
 */
@Slf4j
@Component
public class MappingEntityQueryBuilder {

  private final RulesQueryBuilder rulesQueryBuilder;
  private final OntologyQueryBuilder ontologyQueryBuilder;
  private final QueryHelper queryHelper;

  public MappingEntityQueryBuilder(
      RulesQueryBuilder rulesQueryBuilder, OntologyQueryBuilder ontologyQueryBuilder,
      QueryHelper queryHelper) {
    this.rulesQueryBuilder = rulesQueryBuilder;
    this.ontologyQueryBuilder = ontologyQueryBuilder;
    this.queryHelper = queryHelper;
  }

  /**
   * Builds the query to search for suggestions in rules and ontologies.
   * @param mappingEntity Mapping entity.
   * @return Query to be executed on the lucene index.
   */
  public Query buildSuggestionQuery(MappingEntity mappingEntity) throws IOException {

    List<MappingValue> mappingValues = mappingEntity.getMappingValues();
    Query ruleQuery = rulesQueryBuilder.buildRulesQuery(mappingValues, mappingEntity.getMappingKey());
    Query ontologyQuery = ontologyQueryBuilder.buildOntologiesQuery(mappingValues);

    Query ruleAndOntologyCombinedQuery = combineRuleAndOntologyQuery(ruleQuery, ontologyQuery);

    Query sourceTypeQuery = queryHelper.getTermQuery("sourceType", Constants.HELPER_DOCUMENT_TYPE);

    BooleanQuery.Builder builder = new Builder();

    Query finalQuery = builder.add(
        ruleAndOntologyCombinedQuery, Occur.SHOULD).add(sourceTypeQuery, Occur.MUST_NOT).build();

    log.info("Suggestion query: {}", finalQuery.toString());

    return finalQuery;
  }

  public Query buildHelperDocumentQuery(MappingEntity mappingEntity) throws IOException {

    List<MappingValue> mappingValues = mappingEntity.getMappingValues();
    Query ruleQuery = rulesQueryBuilder.buildRulesQuery(mappingValues, mappingEntity.getMappingKey());
    Query ontologyQuery = ontologyQueryBuilder.buildOntologiesQuery(mappingValues);
    Query finalQuery = combineRuleAndOntologyQuery(ruleQuery, ontologyQuery);
    log.info("Helper query: {}", finalQuery.toString());
    return finalQuery;
  }

  Query combineRuleAndOntologyQuery(Query ruleQuery, Query ontologyQuery) {
    return new DisjunctionMaxQuery(Arrays.asList(ruleQuery, ontologyQuery), 0);
  }

}
