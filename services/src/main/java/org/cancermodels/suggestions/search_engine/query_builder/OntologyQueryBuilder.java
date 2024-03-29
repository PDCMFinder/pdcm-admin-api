package org.cancermodels.suggestions.search_engine.query_builder;

import java.util.List;
import org.apache.lucene.search.Query;
import org.cancermodels.exceptions.SearchException;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.pdcm_admin.persistance.MappingValue;
import org.cancermodels.suggestions.search_engine.IndexableSuggestion;
import org.cancermodels.suggestions.search_engine.MappingValueConfHelper;
import org.springframework.stereotype.Component;

/**
 * Helper class that builds the ontology section of a query for a {@link IndexableSuggestion} document.
 */
@Component
public class OntologyQueryBuilder {

  private final MappingValueConfHelper mappingValueConfHelper;

  private final OntologySearchInputBuilder ontologySearchInputBuilder;
  private final SearchParameters defaultSearchParameters;
  private final SearchInputQueryBuilder searchInputQueryBuilder;

  public OntologyQueryBuilder(
      MappingValueConfHelper mappingValueConfHelper,
      OntologySearchInputBuilder ontologySearchInputBuilder,
      SearchParameters defaultSearchParameters,
      SearchInputQueryBuilder searchInputQueryBuilder) {
    this.mappingValueConfHelper = mappingValueConfHelper;
    this.ontologySearchInputBuilder = ontologySearchInputBuilder;
    this.defaultSearchParameters = defaultSearchParameters;
    this.searchInputQueryBuilder = searchInputQueryBuilder;
  }

  public Query buildOntologiesQuery(MappingEntity mappingEntity) throws SearchException {

    List<MappingValue> toProcess =
        mappingValueConfHelper.getSearchOnOntologyValues(mappingEntity.getMappingValues());

    MappingValue mainValue = mappingValueConfHelper.getMainValue(toProcess);
    List<MappingValue> secondaryValues = mappingValueConfHelper.getSecondaryValues(toProcess);

    String combinedText = mappingValueConfHelper.getTextForMultiFieldQuery(mainValue, secondaryValues);
    SearchInput searchInput = ontologySearchInputBuilder.build(
        mainValue.getValue(), combinedText, defaultSearchParameters, mappingEntity.getEntityType().getName().toLowerCase());
    return searchInputQueryBuilder.buildQuery(searchInput);
  }

}
