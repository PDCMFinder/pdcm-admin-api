package org.cancermodels.suggestions.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.Query;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingKey;
import org.cancermodels.MappingValue;
import org.cancermodels.suggestions.FieldsNames;
import org.springframework.stereotype.Component;

/**
 * This class creates the queries that a Mapping Entity needs to search for suggestions
 * in a lucene index
 */
@Component
public class MappingEntityQueryBuilder {

  private final QueryHelper queryHelper;

  /**
   * Defines how much "important" a match in a rule is (with respect of an ontology)
   */
  private static final double RULE_WEIGHT_GENERAL_MULTIPLIER = 9;

  private static final float ONTOLOGY_SEARCH_WEIGHT = 1.0f;

  public MappingEntityQueryBuilder(QueryHelper queryHelper) {
    this.queryHelper = queryHelper;
  }

  /**
   * Builds the query to search for suggestions in rules and ontologies.
   * @param mappingEntity Mapping entity.
   * @return Query to be executed on the lucene index.
   */
  public Query buildSuggestionQuery(MappingEntity mappingEntity) throws IOException {

    List<MappingValue> mappingValues = mappingEntity.getMappingValues();
    Query ruleQuery = buildRulesQuery(mappingValues);
    Query ontologyQuery = buildOntologyQuery(mappingValues);

    return ensembleFinalQuery(ruleQuery, ontologyQuery);
  }

  Query ensembleFinalQuery(Query ruleQuery, Query ontologyQuery) {
    return new DisjunctionMaxQuery(Arrays.asList(ruleQuery, ontologyQuery), 1);
  }

  /**
   * Creates the section of the suggestion query that deals with the rule fields.
   * @param mappingValues Values of the {@code MappingEntity}.
   * @return A {@link Query} with the rule fields.
   * @throws IOException If the query cannot be built.
   */
  Query buildRulesQuery(List<MappingValue> mappingValues) throws IOException {
    List<Query> queries = new ArrayList<>();
    for (MappingValue mappingValue : mappingValues) {
      double weight = mappingValue.getMappingKey().getWeight() * RULE_WEIGHT_GENERAL_MULTIPLIER;
      // The value is only taken into account if it matters for the query
      if (weight > 0) {
        String key = mappingValue.getMappingKey().getKey();
        // Make first letter lowercase to be consistent with the names of other fields
        key = Character.toLowerCase(key.charAt(0)) + key.substring(1);
        String fieldName = FieldsNames.RULE_VALUE.getName() + key;
        Query query =
            queryHelper.buildBoostedQueryForText(fieldName, mappingValue.getValue(), (float)weight);
        queries.add(query);
      }
    }
    BooleanQuery.Builder builder = new Builder();
    for (Query query : queries) {
      builder.add(query, Occur.SHOULD);
    }
    return builder.build();
  }

  /**
   * Creates the section of the suggestion query that deals with the ontology fields.
   * @param mappingValues Values of the {@code MappingEntity}.
   * @return A {@link Query} with the ontology fields.
   * @throws IOException If the query cannot be built.
   */
  Query buildOntologyQuery(List<MappingValue> mappingValues) throws IOException {
    List<Query> queries = new ArrayList<>();

    String queryText = getQueryTextForSearchOnOntology(mappingValues);

    Query queryLabel = queryHelper.buildBoostedQueryForText(
        FieldsNames.ONTOLOGY_LABEL.getName(), queryText, ONTOLOGY_SEARCH_WEIGHT);
    Query queryDefinition = queryHelper.buildBoostedQueryForText(
        FieldsNames.ONTOLOGY_DEFINITION.getName(), queryText, ONTOLOGY_SEARCH_WEIGHT);
    Query querySynonym = queryHelper.buildBoostedQueryForText(
        FieldsNames.ONTOLOGY_SYNONYM.getName(), queryText, ONTOLOGY_SEARCH_WEIGHT);

    queries.add(queryLabel);
    queries.add(queryDefinition);
    queries.add(querySynonym);

    BooleanQuery.Builder builder = new Builder();
    for (Query query : queries) {
      builder.add(query, Occur.SHOULD);
    }
    return builder.build();
  }

  /**
   * Gets the text that should be used when searching on the indexed ontologies.
   * The final text is a concatenation of the values from the columns marked as
   * {@code searchOnOntology == true}, in the order specified by {@code searchOnOntologyPosition},
   * both attributes of the {@link MappingKey} entity.
   * @param mappingValues The values from a {@code MappingEntity}.
   * @return A text composed by the concatenation of some values in {@code MappingValues}.
   */
  private String getQueryTextForSearchOnOntology(List<MappingValue> mappingValues) {
    // Get all values that can be used on the ontology search
    List<MappingValue> toBeSearched = mappingValues.stream()
        .filter(x -> x.getMappingKey().getSearchOnOntology())
        .sorted(Comparator.comparingInt(a -> a.getMappingKey().getSearchOnOntologyPosition()))
        .collect(Collectors.toList());

    return toBeSearched.stream().map(MappingValue::getValue).collect(Collectors.joining(" "));
  }

}
