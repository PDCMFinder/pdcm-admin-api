package org.cancermodels.mappings.automatic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntitySuggestion;
import org.cancermodels.OntologySuggestion;
import org.cancermodels.OntologyTerm;
import org.cancermodels.Suggestion;
import org.cancermodels.mappings.Status;
import org.cancermodels.mappings.suggestions.SimilarityConfigurationReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This class scans all unmapped entities and tries to automatically map the entity to an ontology
 * term using the suggestions that have been calculated in the system (either by rules or by
 * ontologies).
 */
@Component
public class AutomaticMappingManager {

  private final SimilarityConfigurationReader similarityConfigurationReader;

  private static final Logger LOG = LoggerFactory.getLogger(AutomaticMappingManager.class);

  Map<String, Integer> hits = new HashMap<>();
  Map<String, String> hitsSummary = new HashMap<>();

  private static final int THRESHOLD = 95;

  public AutomaticMappingManager(
      SimilarityConfigurationReader similarityConfigurationReader) {
    this.similarityConfigurationReader = similarityConfigurationReader;
  }

  /**
   * Tries to automatically map a list of unmapped mapping entities.
   * @param mappingEntities Unmapped entities.
   * @param type Entity type (for logging purposes).
   */
  public void calculateAutomaticMappings(List<MappingEntity> mappingEntities, String type) {
    hits.put(type, 0);
    hitsSummary.put(type, null);
    LOG.info(String.format("Start automatic mapping for %s %s entities:", mappingEntities.size(), type));
    for (MappingEntity unmappedMappingEntity : mappingEntities) {
      calculateAutomaticMappingsByEntity(unmappedMappingEntity);
    }

    int percentage = hits.get(type) * 100  / mappingEntities.size();
    hitsSummary.put(type, String.format(
        "Total: %s. Hits: %s. Percentage: %s%%", mappingEntities.size(), hits.get(type), percentage));
    printHitsSummary();
  }

  private void printHitsSummary() {
    for (String type : hitsSummary.keySet()) {
      System.out.println(type + ": " + hitsSummary.get(type));
    }
  }

  /**
   * Tries to automatically map a mapping entity.
   * @param mappingEntity Mapping entity to map.
   */
  private void calculateAutomaticMappingsByEntity(MappingEntity mappingEntity) {

//    if (!mappingEntity.getStatus().equals(Status.UNMAPPED.getLabel())) {
//      return;
//    }
    System.out.println();
    System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
    System.out.println("unmappedEntity values:" + mappingEntity.getValuesAsMap());

    int threshold = similarityConfigurationReader.getSimilarityPerfectMatchScore();
    Map<Integer, List<Suggestion<?>>> candidateAutomaticSuggestionsMappedByScore =
        getCandidatesForAutomaticSuggestionsMappedByScore(mappingEntity, threshold);
    Suggestion<?> bestSuggestion = getBestSuggestionIfConsensus(
        candidateAutomaticSuggestionsMappedByScore);
    if (bestSuggestion != null) {
      String typeName = mappingEntity.getEntityType().getName();

      if (!hits.containsKey(typeName)) {
        hits.put(typeName, 0);
      }
      hits.put(typeName, hits.get(typeName) + 1);
    }


    System.out.println("getBestScore: " + bestSuggestion);
    System.out.println(candidateAutomaticSuggestionsMappedByScore);
//    analyseSuggestions(candidateAutomaticSuggestionsMappedByScore);
  }

  private Suggestion<?> getBestSuggestionIfConsensus(
      Map<Integer, List<Suggestion<?>>> candidateAutomaticSuggestionsMappedByScore) {

    // Get first element of the map, which corresponds to the top suggestions
    List<Suggestion<?>> listWithTopSuggestions =
        candidateAutomaticSuggestionsMappedByScore.values().stream().findFirst().orElse(null);
    if (listWithTopSuggestions == null || listWithTopSuggestions.isEmpty()) {
      return null;
    }
    // There is consensus if all the top suggestions have the same ontology term url
    String firstUrl = listWithTopSuggestions.get(0).getTermUrl();
    for (var x : listWithTopSuggestions) {
      if (!x.getTermUrl().equals(firstUrl)) {
        return null;
      }
    }
    return listWithTopSuggestions.get(0);
  }

  private void analyseSuggestions(List<Suggestion<?>> orderedSuggestions) {

      for (Suggestion<?> s : orderedSuggestions) {
        System.out.println("************************************");
        System.out.println("score: " + s.getScore());
        System.out.println("source: " + s.getSource());
        if (s instanceof MappingEntitySuggestion) {
          MappingEntity mappingEntity = ((MappingEntitySuggestion) s).getSuggestedMappingEntity();
          System.out.println(mappingEntity.getValuesAsMap());
        }
        else if (s instanceof OntologySuggestion) {
          OntologyTerm ontologyTerm = ((OntologySuggestion) s).getOntologyTerm();
          System.out.println("term:" + ontologyTerm.getLabel());
          System.out.println("synonyms:" + ontologyTerm.getSynonyms());
        }
      }
  }

  private Map<Integer, List<Suggestion<?>>> getCandidatesForAutomaticSuggestionsMappedByScore(
      MappingEntity mappingEntity, int threshold) {

    Map<Integer, List<Suggestion<?>>> candidateAutomaticSuggestionsMappedByScore =
        new TreeMap<>(Collections.reverseOrder());

    List<Suggestion<?>> suggestions = new ArrayList<>();
    List<MappingEntitySuggestion> mappingEntitySuggestions =
        mappingEntity.getMappingEntitySuggestions();
    List<OntologySuggestion> ontologySuggestions = mappingEntity.getOntologySuggestions();
    suggestions.addAll(mappingEntitySuggestions);
    suggestions.addAll(ontologySuggestions);
    System.out.println("num tot sugg " + suggestions.size());
    Collections.sort(suggestions, (a, b) -> b.getScore() - a.getScore());
    analyseSuggestions(suggestions);

    List<Suggestion<?>> candidatesFotAutomaticSuggestions =  suggestions.stream()
        .filter(x -> x.getScore() >= threshold).collect(Collectors.toList());

    candidatesFotAutomaticSuggestions.forEach(x ->
        addElementToMap(candidateAutomaticSuggestionsMappedByScore, x, x.getScore()));

    return candidateAutomaticSuggestionsMappedByScore;
  }

  private void addElementToMap(Map<Integer, List<Suggestion<?>>> map, Suggestion<?> element, int key) {
    if (!map.containsKey(key)) {
      map.put(key, new ArrayList<>());
    }
    map.get(key).add(element);
  }

}
