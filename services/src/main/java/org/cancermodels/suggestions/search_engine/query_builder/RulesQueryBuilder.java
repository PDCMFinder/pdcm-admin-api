package org.cancermodels.suggestions.search_engine.query_builder;

import java.io.IOException;
import org.apache.lucene.search.Query;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.suggestions.search_engine.IndexableSuggestion;
import org.cancermodels.suggestions.search_engine.query_builder.MappingEntitySearchInputBuilder;
import org.cancermodels.suggestions.search_engine.query_builder.SearchInput;
import org.cancermodels.suggestions.search_engine.query_builder.SearchInputQueryBuilder;
import org.cancermodels.suggestions.search_engine.query_builder.SearchParameters;
import org.springframework.stereotype.Component;

/**
 * Helper class that builds the rule section of a query for a {@link IndexableSuggestion} document.
 */
@Component
public class RulesQueryBuilder {


  private final MappingEntitySearchInputBuilder mappingEntitySearchInputBuilder;
  private final SearchParameters defaultSearchParameters;
  private final SearchInputQueryBuilder searchInputQueryBuilder;

  public RulesQueryBuilder(
      MappingEntitySearchInputBuilder mappingEntitySearchInputBuilder,
      SearchParameters defaultSearchParameters,
      SearchInputQueryBuilder searchInputQueryBuilder) {

    this.mappingEntitySearchInputBuilder = mappingEntitySearchInputBuilder;
    this.defaultSearchParameters = defaultSearchParameters;
    this.searchInputQueryBuilder = searchInputQueryBuilder;
  }

  /**
   * Creates a query that will search in a {@link IndexableSuggestion} document using rules fields.
   * @param mappingEntity MappingEntity used to create the query.
   * @return A {@link Query} with the rule fields.
   * @throws IOException If the query cannot be built.
   */
  public Query buildRulesQuery(MappingEntity mappingEntity) throws IOException {

    SearchInput searchInput =
        mappingEntitySearchInputBuilder.build(mappingEntity, defaultSearchParameters);
    return searchInputQueryBuilder.buildQuery(searchInput);
  }

}
