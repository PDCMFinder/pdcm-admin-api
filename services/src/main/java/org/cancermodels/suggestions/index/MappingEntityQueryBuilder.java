package org.cancermodels.suggestions.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.PhraseQuery;
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
  private static final double RULE_WEIGHT_GENERAL_MULTIPLIER = 8;
  private static final double EXTRA_RULE_WEIGHT_MULTIPLIER = 1.5;

  private static final float ONTOLOGY_SEARCH_LABEL_WEIGHT = 2f;
  private static final float ONTOLOGY_SEARCH_DEFINITION_WEIGHT = 0.3f;
  private static final float ONTOLOGY_SEARCH_SYNONYM_WEIGHT = 1.2f;

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
    Query ontologyQuery = buildOntologiesQuery(mappingValues);

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

    List<MappingValue> valuesWithValidWeight =
        mappingValues.stream()
            .filter(x -> x.getMappingKey().getWeight() > 0).collect(Collectors.toList());

    queries.addAll(buildRulesTermsQuery(valuesWithValidWeight));
    queries.addAll(buildRulesPhraseQuery(valuesWithValidWeight));
    Query q = buildRulesMultiTermPhraseQuery(valuesWithValidWeight);
    queries.add(q);

//    List<MappingValue> valuesToUseInQuery = getValuesToUseInRuleQuery(mappingValues);
//
//    for (MappingValue mappingValue : valuesToUseInQuery ) {
//      String queryText = mappingValue.getValue();
//      Query query = buildBoostFuzzyQueryWithWeightMultiplier(mappingValue, queryText, RULE_WEIGHT_GENERAL_MULTIPLIER);
//      queries.add(query);
//    }
//
//    Query extraQuery = buildExtraQuery(valuesToUseInQuery);
//    if (extraQuery != null) {
//      queries.add(extraQuery);
//    }

//    for (MappingValue mappingValue : mappingValues) {
//      Query query = buildRuleQueryFromValue(mappingValue, mappingValue.getValue());
//      if (query != null) {
//        queries.add(query);
//      }




//      double weight = mappingValue.getMappingKey().getWeight() * RULE_WEIGHT_GENERAL_MULTIPLIER;
//      // The value is only taken into account if it matters for the query
//      if (weight > 0) {
//        String key = mappingValue.getMappingKey().getKey();
//        // Make first letter lowercase to be consistent with the names of other fields
//        key = Character.toLowerCase(key.charAt(0)) + key.substring(1);
//        String fieldName = FieldsNames.RULE_VALUE.getName() + key;
//        Query query =
//            queryHelper.buildBoostedQueryForText(fieldName, mappingValue.getValue(), (float)weight);
//        queries.add(query);
//      }

//    }

    // Build extrs queries:
//    List<Query> extra = buildExtraRuleQuery(mappingValues);
//    System.out.println(extra);
//    queries.addAll(extra);
//    BooleanQuery.Builder builder = new Builder();
//    for (Query query : queries) {
//      builder.add(query, Occur.SHOULD);
//    }
    return joinQueriesShouldMode(queries);
  }

  /**
   * Creates a list of boosted fuzzy query for every term in each value in mappingValues.
   * For instance, if the value is OriginTissue:central nervous system this will create:
   *  (OriginTissue:central~2 OriginTissue:nervous~2  OriginTissue:system~2)^a_weight
   * @param mappingValues List of {@code MappingValue} to process.
   * @return A list of queries.
   */
  private List<Query> buildRulesTermsQuery(List<MappingValue> mappingValues) throws IOException {
    List<Query> queries = new ArrayList<>();
    for (MappingValue mappingValue : mappingValues ) {
      String queryText = mappingValue.getValue();
      Query query = buildBoostFuzzyQueryWithWeightMultiplier(
          mappingValue, queryText, RULE_WEIGHT_GENERAL_MULTIPLIER);
      queries.add(query);
    }
    return queries;
  }

  /**
   * Creates an list of phrase query for values marked as {@code useAlsoAsPhrase}.
   * @param mappingValues List of {@link MappingValue} to process.
   * @return A list of queries.
   */
  private List<Query> buildRulesPhraseQuery(List<MappingValue> mappingValues) throws IOException {
    List<Query> queries = new ArrayList<>();

    for (MappingValue mappingValue : mappingValues) {
      MappingKey mappingKey = mappingValue.getMappingKey();
      // Check if the key is configured to be used in a phrase query
      if (mappingKey.getKeySearchConfiguration().getUseAlsoAsPhrase()) {
        String queryText = mappingValue.getValue();
        Query query = buildBoostPhraseQueryWithWeightMultiplier(
            mappingValue, queryText, RULE_WEIGHT_GENERAL_MULTIPLIER);
        queries.add(query);
      }
    }
    return queries;
  }


  private Query buildRulesMultiTermPhraseQuery(List<MappingValue> mappingValues)
      throws IOException {
    Query query = null;

    MappingValue mainFieldInMultiTermPhrase = null;
    List<MappingValue> otherFieldsToBeUsedInMultiTermPhrase = new ArrayList<>();

    for (MappingValue mappingValue : mappingValues) {
      MappingKey mappingKey = mappingValue.getMappingKey();
      // Check if the key is configured to be used in a phrase query
      if (mappingKey.getKeySearchConfiguration().getUseAlsoAsMultiTermPhrase()) {
        if (mappingKey.getKeySearchConfiguration().getIsMultiTermPhraseMainField()) {
          mainFieldInMultiTermPhrase = mappingValue;
        }
        else {
          otherFieldsToBeUsedInMultiTermPhrase.add(mappingValue);
        }
      }
    }

    if (mainFieldInMultiTermPhrase != null && !otherFieldsToBeUsedInMultiTermPhrase.isEmpty()) {
      String prefix =

      otherFieldsToBeUsedInMultiTermPhrase.stream().map(
          MappingValue::getValue).collect(Collectors.joining(" "));

      String queryText = prefix + " " + mainFieldInMultiTermPhrase.getValue();
      System.out.println("query text multiterm phrase ["+queryText+"]");
      query = buildBoostPhraseQueryWithWeightMultiplier(
          mainFieldInMultiTermPhrase, queryText, RULE_WEIGHT_GENERAL_MULTIPLIER);
    }
    return query;
  }

  private Query joinQueriesShouldMode(List<Query> queries) {
    BooleanQuery.Builder builder = new Builder();
    for (Query query : queries) {
      if (query != null) {
        builder.add(query, Occur.SHOULD);
      }
    }
    return builder.build();
  }

  private Query buildExtraQuery(List<MappingValue> mappingValues) throws IOException {
    Query query = null;
    MappingValue valueToUseAsField = getValueToUseAsFieldInExtraQuery(mappingValues).orElse(null);
    if (valueToUseAsField != null) {
      List<MappingValue> fieldsToAddToExtraQuery = getFieldsToAddToExtraQuery(mappingValues);
      fieldsToAddToExtraQuery.remove(valueToUseAsField);

      if (!fieldsToAddToExtraQuery.isEmpty()) {
        String queryText = fieldsToAddToExtraQuery.stream().map(
            MappingValue::getValue).collect(
            Collectors.joining(" ")) + " " + valueToUseAsField.getValue();
        System.out.println("new query string[" + queryText + "]");
        query = buildBoostFuzzyQueryWithWeightMultiplier(valueToUseAsField, queryText, EXTRA_RULE_WEIGHT_MULTIPLIER);
        double weight = valueToUseAsField.getMappingKey().getWeight() * EXTRA_RULE_WEIGHT_MULTIPLIER;
        String fieldName = getFieldNameByValue(valueToUseAsField);
        String[] terms = queryText.split(" ");
        PhraseQuery phraseQuery = new PhraseQuery(1, fieldName, terms);
        BoostQuery boostQuery = new BoostQuery (phraseQuery, (float)weight);
        System.out.println("boosted query: " + boostQuery.toString());
        query = boostQuery;
      }
    }
    return query;
  }

  private Optional<MappingValue> getValueToUseAsFieldInExtraQuery(List<MappingValue> mappingValues) {
    return mappingValues.stream().filter(x -> x.getMappingKey().getAdditionalQueryDriver()).findFirst();
  }

  private List<MappingValue> getFieldsToAddToExtraQuery(List<MappingValue> mappingValues) {
    return mappingValues.stream().filter(x -> x.getMappingKey().getUseInAdditionalQuery()).collect(
        Collectors.toList());
  }

  private Query buildBoostFuzzyQueryWithWeightMultiplier(
      MappingValue mappingValue, String queryText, double weightMultiplier) throws IOException {

    double weight = mappingValue.getMappingKey().getWeight() * weightMultiplier;
    String fieldName = getFieldNameByValue(mappingValue);
    return queryHelper.buildBoostFuzzyQueryByTerm(fieldName, queryText, (float)weight);
  }

  private Query buildBoostPhraseQueryWithWeightMultiplier(
      MappingValue mappingValue, String queryText, double weightMultiplier) throws IOException {

    double weight = mappingValue.getMappingKey().getWeight() * weightMultiplier;
    String fieldName = getFieldNameByValue(mappingValue);
    return queryHelper.buildBoostPhraseQuery(fieldName, queryText, (float)weight);
  }

  private List<MappingValue> getValuesToUseInRuleQuery(List<MappingValue> mappingValues) {
    return mappingValues.stream()
        .filter(x -> x.getMappingKey().getWeight() > 0).collect(Collectors.toList());
  }

  private String getFieldNameByValue(MappingValue mappingValue) {
    String key = mappingValue.getMappingKey().getKey();
    // Make first letter lowercase to be consistent with the names of other fields
    key = Character.toLowerCase(key.charAt(0)) + key.substring(1);
    return FieldsNames.RULE_VALUE.getName() + key;
  }

  /**
   * Creates the section of the suggestion query that deals with the ontology fields.
   * @param mappingValues Values of the {@code MappingEntity}.
   * @return A {@link Query} with the ontology fields.
   * @throws IOException If the query cannot be built.
   */
  Query buildOntologyQuery(List<MappingValue> mappingValues) throws IOException {
    List<Query> queries = new ArrayList<>();

    List<MappingValue> valuesToProcess = getValuesToUseInOntologyQuery(mappingValues);

    for (MappingValue value : valuesToProcess) {

    }

    String queryText = getQueryTextForSearchOnOntology(mappingValues);

//    Query queryLabel = queryHelper.buildBoostedQueryForText(
//        FieldsNames.ONTOLOGY_LABEL.getName(), queryText, ONTOLOGY_SEARCH_LABEL_WEIGHT);
//    Query queryDefinition = queryHelper.buildBoostedQueryForText(
//        FieldsNames.ONTOLOGY_DEFINITION.getName(), queryText, ONTOLOGY_SEARCH_DEFINITION_WEIGHT);
//    Query querySynonym = queryHelper.buildBoostedQueryForText(
//        FieldsNames.ONTOLOGY_SYNONYM.getName(), queryText, ONTOLOGY_SEARCH_SYNONYM_WEIGHT);

    Query queryLabel = queryHelper.buildBoostPhraseQuery(
        FieldsNames.ONTOLOGY_LABEL.getName(), queryText, ONTOLOGY_SEARCH_LABEL_WEIGHT);
    Query queryDefinition = queryHelper.buildBoostPhraseQuery(
        FieldsNames.ONTOLOGY_DEFINITION.getName(), queryText, ONTOLOGY_SEARCH_DEFINITION_WEIGHT);
    Query querySynonym = queryHelper.buildBoostPhraseQuery(
        FieldsNames.ONTOLOGY_SYNONYM.getName(), queryText, ONTOLOGY_SEARCH_SYNONYM_WEIGHT);

    queries.add(queryLabel);
    queries.add(queryDefinition);
    queries.add(querySynonym);

    BooleanQuery.Builder builder = new Builder();
    for (Query query : queries) {
      builder.add(query, Occur.SHOULD);
    }
    return builder.build();
  }

  private List<MappingValue> getValuesToUseInOntologyQuery(List<MappingValue> mappingValues) {
    return mappingValues.stream()
        .filter(x -> x.getMappingKey().getSearchOnOntology()).collect(Collectors.toList());
  }

  /**
   * Gets the text that should be used when searching on the indexed ontologies.
   * The final text is a concatenation of the values from the columns marked as
   * {@code searchOnOntology == true}, both attributes of the {@link MappingKey} entity.
   * @param mappingValues The values from a {@code MappingEntity}.
   * @return A text composed by the concatenation of some values in {@code MappingValues}.
   */
  private String getQueryTextForSearchOnOntology(List<MappingValue> mappingValues) {
    // Get all values that can be used on the ontology search
    List<MappingValue> toBeSearched = mappingValues.stream()
        .filter(x -> x.getMappingKey().getSearchOnOntology())
        .collect(Collectors.toList());

    return toBeSearched.stream().map(MappingValue::getValue).collect(Collectors.joining(" "));
  }





  Query buildOntologiesQuery(List<MappingValue> mappingValues) throws IOException {
    List<Query> queries = new ArrayList<>();

    List<MappingValue> valuesToUse =
        mappingValues.stream()
            .filter(x -> x.getMappingKey().getSearchOnOntology())
            .collect(Collectors.toList());

//    MappingValue mainValue = valuesToUse.stream()
//        .filter(x -> x.getMappingKey().getKeySearchConfiguration().getIsMultiTermPhraseMainField())
//        .findFirst().orElse(null);

    MappingValue mainValue = null;
    List<MappingValue> secondaryValues = new ArrayList<>();

    for (MappingValue mappingValue : mappingValues) {
      MappingKey mappingKey = mappingValue.getMappingKey();
      var conf = mappingValue.getMappingKey().getKeySearchConfiguration();
      // Check if the key is configured to be used in a phrase query
      if (conf.getUseAlsoAsMultiTermPhrase()) {
        if (conf.getIsMultiTermPhraseMainField()) {
          mainValue = mappingValue;
        }
        else {
          secondaryValues.add(mappingValue);
        }
      }
    }


    queries.addAll(buildOntologyTermsQuery(mainValue));
    queries.addAll(buildOntologyMultiKeyTermQuery(mainValue, secondaryValues));
    queries.addAll(buildOntologyPhraseQuery(mainValue));
    queries.addAll(buildOntologyMultiKeyPhraseQuery(mainValue, secondaryValues));
    return joinQueriesShouldMode(queries);
  }



  private List<Query> buildOntologyTermsQuery(MappingValue mainValue) throws IOException {
    List<Query> queries = new ArrayList<>();

    String queryText = mainValue.getValue();
    Query queryLabel = queryHelper.buildBoostFuzzyQueryByTerm(
        FieldsNames.ONTOLOGY_LABEL.getName(), queryText, ONTOLOGY_SEARCH_LABEL_WEIGHT);
    Query queryDefinition = queryHelper.buildBoostFuzzyQueryByTerm(
        FieldsNames.ONTOLOGY_DEFINITION.getName(), queryText, ONTOLOGY_SEARCH_DEFINITION_WEIGHT);
    Query querySynonym = queryHelper.buildBoostFuzzyQueryByTerm(
        FieldsNames.ONTOLOGY_SYNONYM.getName(), queryText, ONTOLOGY_SEARCH_SYNONYM_WEIGHT);
    queries.add(queryLabel);
    queries.add(queryDefinition);
    queries.add(querySynonym);

    return queries;
  }

  private List<Query> buildOntologyMultiKeyTermQuery(MappingValue mainValue, List<MappingValue> secondaryValues)
      throws IOException {
    List<Query> queries = new ArrayList<>();
    if (mainValue != null && !secondaryValues.isEmpty()) {
      String prefix =
          secondaryValues.stream().map(
              MappingValue::getValue).collect(Collectors.joining(" "));

      String queryText = prefix + " " + mainValue.getValue();
      System.out.println("query text multiterm phrase onto ["+queryText+"]");

      Query queryLabel = queryHelper.buildBoostFuzzyQueryByTerm(
          FieldsNames.ONTOLOGY_LABEL.getName(), queryText, ONTOLOGY_SEARCH_LABEL_WEIGHT);
      Query queryDefinition = queryHelper.buildBoostFuzzyQueryByTerm(
          FieldsNames.ONTOLOGY_DEFINITION.getName(), queryText, ONTOLOGY_SEARCH_DEFINITION_WEIGHT);
      Query querySynonym = queryHelper.buildBoostFuzzyQueryByTerm(
          FieldsNames.ONTOLOGY_SYNONYM.getName(), queryText, ONTOLOGY_SEARCH_SYNONYM_WEIGHT);
      queries.add(queryLabel);
      queries.add(queryDefinition);
      queries.add(querySynonym);
    }

    return queries;
  }


  private List<Query> buildOntologyPhraseQuery(MappingValue mainValue)
      throws IOException {
    List<Query> queries = new ArrayList<>();

    String queryText = mainValue.getValue();
    Query queryLabel = queryHelper.buildBoostPhraseQuery(
        FieldsNames.ONTOLOGY_LABEL.getName(), queryText, ONTOLOGY_SEARCH_LABEL_WEIGHT);
    Query queryDefinition = queryHelper.buildBoostPhraseQuery(
        FieldsNames.ONTOLOGY_DEFINITION.getName(), queryText, ONTOLOGY_SEARCH_DEFINITION_WEIGHT);
    Query querySynonym = queryHelper.buildBoostPhraseQuery(
        FieldsNames.ONTOLOGY_SYNONYM.getName(), queryText, ONTOLOGY_SEARCH_SYNONYM_WEIGHT);
    queries.add(queryLabel);
    queries.add(queryDefinition);
    queries.add(querySynonym);


    return queries;
  }

  //idea: also term query with others + main

  private List<Query> buildOntologyMultiKeyPhraseQuery(
      MappingValue mainValue, List<MappingValue> secondaryFields)
      throws IOException {

    List<Query> queries = new ArrayList<>();

    if (mainValue != null && !secondaryFields.isEmpty()) {
      String prefix =
          secondaryFields.stream().map(
              MappingValue::getValue).collect(Collectors.joining(" "));

      String queryText = prefix + " " + mainValue.getValue();
      System.out.println("query text multiterm phrase onto ["+queryText+"]");

        Query queryLabel = queryHelper.buildBoostPhraseQuery(
            FieldsNames.ONTOLOGY_LABEL.getName(), queryText, ONTOLOGY_SEARCH_LABEL_WEIGHT);
        Query queryDefinition = queryHelper.buildBoostPhraseQuery(
            FieldsNames.ONTOLOGY_DEFINITION.getName(), queryText, ONTOLOGY_SEARCH_DEFINITION_WEIGHT);
        Query querySynonym = queryHelper.buildBoostPhraseQuery(
            FieldsNames.ONTOLOGY_SYNONYM.getName(), queryText, ONTOLOGY_SEARCH_SYNONYM_WEIGHT);
        queries.add(queryLabel);
        queries.add(queryDefinition);
        queries.add(querySynonym);
      }

    return queries;
  }

}
