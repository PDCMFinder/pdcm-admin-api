package org.cancermodels.suggestions.search_engine;

import java.io.IOException;
import java.util.List;
import org.apache.lucene.search.Query;
import org.cancermodels.persistance.Suggestion;
import org.cancermodels.suggestions.search_engine.query_builder.SearchParameters;
import org.cancermodels.suggestions.search_engine.query_builder.OntologySearchInputBuilder;
import org.cancermodels.suggestions.search_engine.query_builder.QueryProcessor;
import org.cancermodels.suggestions.search_engine.query_builder.SearchInput;
import org.cancermodels.suggestions.search_engine.query_builder.SearchInputQueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class OntologySearcherByText {

  private final SearchInputQueryBuilder searchInputQueryBuilder;

  private final OntologySearchInputBuilder ontologySearchInputBuilder;
  private final SearchParameters defaultCommonParameters;
  private final QueryProcessor queryProcessor;

  public OntologySearcherByText(
      SearchInputQueryBuilder searchInputQueryBuilder,
      OntologySearchInputBuilder ontologySearchInputBuilder,
      SearchParameters defaultCommonParameters,
      QueryProcessor queryProcessor) {
    this.searchInputQueryBuilder = searchInputQueryBuilder;
    this.ontologySearchInputBuilder = ontologySearchInputBuilder;
    this.defaultCommonParameters = defaultCommonParameters;
    this.queryProcessor = queryProcessor;
  }

  public List<Suggestion> searchWithDefaultParameters(String input) throws IOException {
    SearchInput searchInput = ontologySearchInputBuilder.build(input, null, defaultCommonParameters);
    Query query = searchInputQueryBuilder.buildQuery(searchInput);
    return queryProcessor.execute(query);
  }
}
