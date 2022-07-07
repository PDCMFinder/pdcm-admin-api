package org.cancermodels.mappings.suggestions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.cancermodels.EntityType;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingKey;
import org.cancermodels.OntologySuggestion;
import org.cancermodels.OntologyTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** A class that suggests ontology terms for mapping entities. */
@Service
public class OntologySuggestionManager {

  private final SimilarityConfigurationReader similarityConfigurationReader;
  private final ScoreManager<OntologyTerm> scoreManager;

  private static final Logger LOG = LoggerFactory.getLogger(OntologySuggestionManager.class);

  static SimilarityComparator similarityComparator = null;

  public OntologySuggestionManager(SimilarityConfigurationReader similarityConfigurationReader,
      ScoreManager<OntologyTerm> scoreManager) {
    this.similarityConfigurationReader = similarityConfigurationReader;
    this.scoreManager = scoreManager;
  }

  private SimilarityComparator getSimilarityComparatorInstance() {
    if (similarityComparator == null) {
      similarityComparator = similarityConfigurationReader.getSimilarityAlgorithm();
    }
    return similarityComparator;
  }

  /**
   * Calculate ontologies suggestions.
   *
   * @param mappingEntities Mapping entities for which the suggestions are going to be calculated.
   * @return Map with the suggestions for each entity.
   */
  public Map<MappingEntity, List<OntologySuggestion>> calculateSuggestions(
      List<MappingEntity> mappingEntities, List<OntologyTerm> ontologyTerms, String type) {

    Map<MappingEntity, List<OntologySuggestion>> suggestionsByEntities = new HashMap<>();

    int totalEntitiesToProcess = mappingEntities.size();
    int processed = 0;
    for (MappingEntity mappingEntity : mappingEntities) {
      Set<OntologySuggestion> suggestions =
          calculateSuggestionsForEntity(mappingEntity, ontologyTerms);
      suggestionsByEntities.put(mappingEntity, asListOrderedByScoreDesc(suggestions));
      processed++;
      reportProgress(processed, totalEntitiesToProcess, type);
    }
    return suggestionsByEntities;
  }

  private List<OntologySuggestion> asListOrderedByScoreDesc(Set<OntologySuggestion> suggestions) {
    List<OntologySuggestion> suggestionsAsList = new ArrayList<>(suggestions);
    suggestionsAsList.sort(Comparator.comparing(OntologySuggestion::getScore).reversed());
    return suggestionsAsList;
  }

  private void reportProgress(int processed, int total, String type) {
    if (processed % 100 == 0 || processed == 1 || processed == total) {
      int percentage = processed * 100 / total;
      LOG.info(String.format(
          "Suggestion calculation (ontologies) %s. Processed %s of %s (%s%%)",
          type,
          processed,
          total,
          percentage));
    }
  }

  private Set<OntologySuggestion> calculateSuggestionsForEntity(
      MappingEntity mappingEntity, List<OntologyTerm> ontologyTerms) {

    List<MappingKey> keys = getKeysToEvaluate(mappingEntity.getEntityType());
    Map<String, String> entityValues = mappingEntity.getValuesAsMap();
    Map<String, String> valuesToEvaluate = getValuesToEvaluate(keys, entityValues);

    Map<Integer, Set<OntologyTerm>> perfectMatches = new TreeMap<>(Collections.reverseOrder());
    Map<Integer, Set<OntologyTerm>> acceptableMatches = new TreeMap<>(Collections.reverseOrder());
    Map<Integer, Set<OntologyTerm>> orderedSuggestions = new TreeMap<>(Collections.reverseOrder());

    // Needs to be wrapped so it can be passed as reference and updated inside scoreManager.processScore
    AtomicInteger numberOfPerfectMatches = new AtomicInteger(0);

    for (OntologyTerm ontologyTerm : ontologyTerms) {
      int score = calculateAverageScore(valuesToEvaluate, ontologyTerm);

      boolean shouldKeepSearching = scoreManager.processScore(
          score, numberOfPerfectMatches, perfectMatches, acceptableMatches, ontologyTerm);

      if (!shouldKeepSearching) {
        break;
      }
    }
    orderedSuggestions.putAll(perfectMatches);
    orderedSuggestions.putAll(acceptableMatches);
    return getBestSuggestions(orderedSuggestions);
  }

  /**
   * Gets the best suggestions based on the number of suggested mappings per entity
   * configured in the system.
   * @param allSuggestions All the calculated suggestions
   * @return A subset of the suggestions, only taking the first best n, where n is
   * the number of suggested mappings per entity
   */
  private Set<OntologySuggestion> getBestSuggestions(
      Map<Integer, Set<OntologyTerm>> allSuggestions) {

    Set<OntologySuggestion> mappingEntitySuggestions = new HashSet<>();

    outer:
    for (Integer key : allSuggestions.keySet()) {
      Set<OntologyTerm> suggestions = allSuggestions.get(key);
      for (OntologyTerm suggested : suggestions) {
        OntologySuggestion ontologySuggestion = new OntologySuggestion();
        ontologySuggestion.setOntologyTerm(suggested);
        ontologySuggestion.setScore(key);

        mappingEntitySuggestions.add(ontologySuggestion);
        if (mappingEntitySuggestions.size()
            >= similarityConfigurationReader.getNumberOfSuggestedMappingsPerEntity()) {
          break outer;
        }
      }
    }
    return mappingEntitySuggestions;
  }

  private int calculateAverageScore(
      Map<String, String> valuesToEvaluate, OntologyTerm ontologyTerm) {

    int score = 0;
    for (String k : valuesToEvaluate.keySet()) {
      String value = valuesToEvaluate.get(k);
      score += calculateTermScore(value, ontologyTerm);
    }
    return score / valuesToEvaluate.size();
  }

  /**
   * Calculates the similarity score between a string and an ontology term taking into account its
   * relevant fields to search: label and synonyms. The score is the highest of the scores
   * calculated for the label and each one of the synonyms
   *
   * @param value String to evaluate
   * @param ontologyTerm Ontology term
   */
  private double calculateTermScore(String value, OntologyTerm ontologyTerm) {
    int highestScore = 0;

    // First start calculating similarity with the ontology label
    highestScore = getSimilarityComparatorInstance().calculate(value, ontologyTerm.getLabel());

    // If the match is not "perfect", keep looking in the synonyms
    if (highestScore >= similarityConfigurationReader.getSimilarityPerfectMatchScore()) {
      return highestScore;
    }
    for (String synonym : ontologyTerm.getSynonyms()) {
      int synonymScore = getSimilarityComparatorInstance().calculate(value, synonym);
      if (synonymScore > highestScore) {
        highestScore = synonymScore;
      }
    }
    return highestScore;
  }

  private List<MappingKey> getKeysToEvaluate(EntityType entityType) {
    return entityType.getMappingKeys().stream()
        .filter(MappingKey::getToUseInOntologySuggestionCalculation)
        .collect(Collectors.toList());
  }

  private Map<String, String> getValuesToEvaluate(
      List<MappingKey> keys, Map<String, String> allValues) {

    Map<String, String> valuesToEvaluate = new HashMap<>();
    for (String allValuesKey : allValues.keySet()) {
      for (MappingKey mappingKey : keys) {
        if (mappingKey.getKey().equals(allValuesKey)) {
          valuesToEvaluate.put(allValuesKey, allValues.get(allValuesKey));
        }
      }
    }
    return valuesToEvaluate;
  }
}
