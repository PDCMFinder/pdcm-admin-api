package org.cancermodels.suggestions.search_engine;

import java.io.IOException;
import java.util.List;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;
import org.cancermodels.exceptions.SearchException;
import org.cancermodels.persistance.Suggestion;
import org.cancermodels.suggestions.FieldsNames;
import org.cancermodels.suggestions.search_engine.query_builder.QueryHelper;
import org.cancermodels.suggestions.search_engine.query_builder.SearchParameters;
import org.cancermodels.suggestions.search_engine.query_builder.OntologySearchInputBuilder;
import org.cancermodels.suggestions.search_engine.query_builder.QueryProcessor;
import org.cancermodels.suggestions.search_engine.query_builder.SearchInput;
import org.cancermodels.suggestions.search_engine.query_builder.SearchInputQueryBuilder;
import org.cancermodels.suggestions.search_engine.util.Constants;
import org.springframework.stereotype.Component;

@Component
public class OntologySearcherByText {

  private final SearchInputQueryBuilder searchInputQueryBuilder;

  private final OntologySearchInputBuilder ontologySearchInputBuilder;
  private final SearchParameters defaultCommonParameters;
  private final QueryProcessor queryProcessor;
  private final QueryHelper queryHelper;

  public OntologySearcherByText(
      SearchInputQueryBuilder searchInputQueryBuilder,
      OntologySearchInputBuilder ontologySearchInputBuilder,
      SearchParameters defaultCommonParameters,
      QueryProcessor queryProcessor,
      QueryHelper queryHelper) {
    this.searchInputQueryBuilder = searchInputQueryBuilder;
    this.ontologySearchInputBuilder = ontologySearchInputBuilder;
    this.defaultCommonParameters = defaultCommonParameters;
    this.queryProcessor = queryProcessor;
    this.queryHelper = queryHelper;
  }

  public List<Suggestion> searchWithDefaultParameters(String input, String entityTypeName) throws SearchException {
    SearchInput searchInput = ontologySearchInputBuilder.build(input, null, defaultCommonParameters, entityTypeName);
    Query ontologyQuery = searchInputQueryBuilder.buildQuery(searchInput);

    // Exclude helper documents
    Query sourceTypeQuery =
        queryHelper.getTermQuery(FieldsNames.SOURCE_TYPE.getName(), Constants.HELPER_DOCUMENT_TYPE);

    BooleanQuery.Builder builder = new Builder();

    Query finalQuery = builder.add(
        ontologyQuery, Occur.SHOULD).add(sourceTypeQuery, Occur.MUST_NOT).build();

    return queryProcessor.execute(finalQuery);
  }
}
