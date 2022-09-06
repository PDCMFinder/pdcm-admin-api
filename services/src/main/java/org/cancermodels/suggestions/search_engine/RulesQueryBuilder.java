package org.cancermodels.suggestions.search_engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.cancermodels.persistance.MappingValue;
import org.cancermodels.suggestions.FieldsNames;
import org.cancermodels.suggestions.search_engine.util.QueryConstants;
import org.springframework.stereotype.Component;

/**
 * Helper class that builds the rule section of a query for a {@link IndexableSuggestion} document.
 */
@Component
public class RulesQueryBuilder {

  private final MappingValueConfHelper mappingValueConfHelper;
  private final QueryHelper queryHelper;

  public RulesQueryBuilder(
      MappingValueConfHelper mappingValueConfHelper,
      QueryHelper queryHelper) {
    this.mappingValueConfHelper = mappingValueConfHelper;
    this.queryHelper = queryHelper;
  }

  /**
   * Creates a query that will search in a {@link IndexableSuggestion} document using rules fields.
   * @param mappingValues Mapping values to use in the query building
   * @param mappingKey
   * @return A {@link Query} with the rule fields.
   * @throws IOException If the query cannot be built.
   */
  public Query buildRulesQuery(List<MappingValue> mappingValues, String mappingKey) throws IOException {
    List<Query> queries = new ArrayList<>();
    List<MappingValue> toProcess =
        mappingValueConfHelper.getValuesWeightGreaterZero(mappingValues);
    MappingValue mainValue = mappingValueConfHelper.getMainValue(mappingValues);
    List<MappingValue> secondaryValues = mappingValueConfHelper.getSecondaryValues(mappingValues);

    // Build boost fuzzy term query for each value to process
    for (MappingValue mappingValue : toProcess) {

      Query query = buildRulesTermsQuery(
          mappingValue, mappingValue.getValue(), QueryConstants.TERM_RELEVANCE_MULTIPLIER);
      queries.add(query);
    }

    // Build boost phrase query for the main value
    queries.add(buildBoostPhraseQuery(
        mainValue, mainValue.getValue(), QueryConstants.PHRASE_RELEVANCE_MULTIPLIER));

    String combinedText = mappingValueConfHelper.getTextForMultiFieldQuery(mainValue, secondaryValues);

    if (combinedText != null) {
      // Build boost fuzzy term query with [extra field values] + [main value] text
      // (to represent things like combination of origin tissue + diagnosis)
      queries.add(
          buildRulesTermsQuery(
              mainValue, combinedText, QueryConstants.MULTI_TERM_RELEVANCE_MULTIPLIER));

      // Build boost phrase query with [extra field values] + [main value] text
      // (to represent things like combination of origin tissue + diagnosis)
      queries.add(
          buildBoostPhraseQuery(
              mainValue, combinedText, QueryConstants.MULTI_TERM_PHRASE_RELEVANCE_MULTIPLIER));
    }

    Query partialQuery = queryHelper.joinQueriesShouldMode(queries);

    // The same rule shouldn't be a suggestion
    Query ignoreOwnId = queryHelper.getTermQuery("id", mappingKey);

    BooleanQuery.Builder builder = new Builder();
    return builder.add(partialQuery, Occur.SHOULD).add(ignoreOwnId, Occur.MUST_NOT).build();
  }

  private Query buildRulesTermsQuery(MappingValue mappingValue, String queryText, double relevance)
      throws IOException {

      double multiplier = relevance * QueryConstants.RULE_MULTIPLIER;

      var conf = mappingValue.getMappingKey().getKeySearchConfiguration();
      double weight = conf.getWeight() * multiplier;
      String fieldName = getFieldNameByValue(mappingValue);
      return queryHelper.buildBoostFuzzyQueryByTerm(fieldName, queryText, (float)weight);
  }

  private Query buildBoostPhraseQuery(
      MappingValue mappingValue, String queryText, double phraseRelevanceMultiplier)
      throws IOException {
    double multiplier = phraseRelevanceMultiplier * QueryConstants.RULE_MULTIPLIER;
    var conf = mappingValue.getMappingKey().getKeySearchConfiguration();
      double weight = conf.getWeight() * multiplier;
      String fieldName = getFieldNameByValue(mappingValue);
      return queryHelper.buildBoostPhraseQuery(fieldName, queryText, (float)weight);
  }

  private String getFieldNameByValue(MappingValue mappingValue) {
    String key = mappingValue.getMappingKey().getKey();
    // Make first letter lowercase to be consistent with the names of other fields
    key = Character.toLowerCase(key.charAt(0)) + key.substring(1);
    return FieldsNames.RULE_VALUE.getName() + key;
  }

}
