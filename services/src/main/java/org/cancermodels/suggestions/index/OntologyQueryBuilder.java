package org.cancermodels.suggestions.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.search.Query;
import org.cancermodels.MappingValue;
import org.cancermodels.suggestions.FieldsNames;
import org.springframework.stereotype.Component;

/**
 * Helper class that builds the ontology section of a query for a {@link IndexableSuggestion} document.
 */
@Component
public class OntologyQueryBuilder {

  private final MappingValueConfHelper mappingValueConfHelper;
  private final QueryHelper queryHelper;

  public OntologyQueryBuilder(
      MappingValueConfHelper mappingValueConfHelper,
      QueryHelper queryHelper) {
    this.mappingValueConfHelper = mappingValueConfHelper;
    this.queryHelper = queryHelper;
  }

  public Query buildOntologiesQuery(List<MappingValue> mappingValues) throws IOException {
    List<Query> queries = new ArrayList<>();
    List<MappingValue> toProcess =
        mappingValueConfHelper.getSearchOnOntologyValues(mappingValues);

    MappingValue mainValue = mappingValueConfHelper.getMainValue(toProcess);
    List<MappingValue> secondaryValues = mappingValueConfHelper.getSecondaryValues(toProcess);

    // Build a boost fuzzy term query for the main value
    queries.addAll(
        buildOntologyBoostFuzzyQueryByTerm(
            mainValue.getValue(), QueryConstants.TERM_RELEVANCE_MULTIPLIER));

    // Build a boost phrase query for the main value
    queries.addAll(
        buildOntologyBuildBoostPhraseQuery(
            mainValue.getValue(), QueryConstants.PHRASE_RELEVANCE_MULTIPLIER));

    String combinedText = mappingValueConfHelper.getTextForMultiFieldQuery(mainValue, secondaryValues);

    if (combinedText != null) {
      // Build a boost fuzzy term query with [extra field values] + [main value] text
      // (to represent things like combination of origin tissue + diagnosis)
      queries.addAll(
          buildOntologyBoostFuzzyQueryByTerm(
              combinedText, QueryConstants.MULTI_TERM_RELEVANCE_MULTIPLIER));

      // Build a boost phrase query with [extra field values] + [main value] text
      // (to represent things like combination of origin tissue + diagnosis)
      queries.addAll(
          buildOntologyBuildBoostPhraseQuery(
              combinedText, QueryConstants.MULTI_TERM_PHRASE_RELEVANCE_MULTIPLIER));
    }

    return queryHelper.joinQueriesShouldMode(queries);
  }

  private List<Query> buildOntologyBoostFuzzyQueryByTerm(String queryText, double multiplier)
      throws IOException {
    List<Query> queries = new ArrayList<>();

    double labelMultiplier = multiplier * QueryConstants.ONTOLOGY_SEARCH_LABEL_WEIGHT;
    Query queryLabel = queryHelper.buildBoostFuzzyQueryByTerm(
        FieldsNames.ONTOLOGY_LABEL.getName(), queryText, labelMultiplier);

    double definitionMultiplier = multiplier * QueryConstants.ONTOLOGY_SEARCH_DEFINITION_WEIGHT;
    Query queryDefinition = queryHelper.buildBoostFuzzyQueryByTerm(
        FieldsNames.ONTOLOGY_DEFINITION.getName(), queryText, definitionMultiplier);

    double synonymMultiplier = multiplier * QueryConstants.ONTOLOGY_SEARCH_SYNONYM_WEIGHT;
    Query querySynonym = queryHelper.buildBoostFuzzyQueryByTerm(
        FieldsNames.ONTOLOGY_SYNONYM.getName(), queryText, synonymMultiplier);

    queries.add(queryLabel);
    queries.add(queryDefinition);
    queries.add(querySynonym);

    return queries;
  }

  private List<Query> buildOntologyBuildBoostPhraseQuery(String queryText, double multiplier)
      throws IOException {
    List<Query> queries = new ArrayList<>();
    double labelMultiplier = multiplier * QueryConstants.ONTOLOGY_SEARCH_LABEL_WEIGHT;
    Query queryLabel = queryHelper.buildBoostPhraseQuery(
        FieldsNames.ONTOLOGY_LABEL.getName(), queryText, labelMultiplier);

    double definitionMultiplier = multiplier * QueryConstants.ONTOLOGY_SEARCH_DEFINITION_WEIGHT;
    Query queryDefinition = queryHelper.buildBoostPhraseQuery(
        FieldsNames.ONTOLOGY_DEFINITION.getName(), queryText, definitionMultiplier);

    double synonymMultiplier = multiplier * QueryConstants.ONTOLOGY_SEARCH_SYNONYM_WEIGHT;
    Query querySynonym = queryHelper.buildBoostPhraseQuery(
        FieldsNames.ONTOLOGY_SYNONYM.getName(), queryText, synonymMultiplier);

    queries.add(queryLabel);
    queries.add(queryDefinition);
    queries.add(querySynonym);

    return queries;
  }
}
