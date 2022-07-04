package org.cancermodels.mappings.suggestions;

import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntitySuggestion;
import org.cancermodels.prototype.JaroWinklerDistanceSimilarityComparator;
import org.cancermodels.prototype.SimilarityComparator;
import org.cancermodels.prototype.TermsWeightedSimilarityCalculator;
import org.springframework.stereotype.Service;

@Service
public class SuggestionsManager {
  private final SimilarityConfigurationReader similarityConfigurationReader;
  private final SimilarityComparator similarityComparator;
  private final TermsWeightedSimilarityCalculator termsWeightedSimilarityCalculator;

  public SuggestionsManager(SimilarityConfigurationReader similarityConfigurationReader) {
    this.similarityConfigurationReader = similarityConfigurationReader;
    similarityComparator = similarityConfigurationReader.getSimilarityAlgorithm();
    termsWeightedSimilarityCalculator = new TermsWeightedSimilarityCalculator(similarityComparator);
  }

  public void updateSuggestedMappingsByExistingRules(
      List<MappingEntity> mappingEntities) {
    for (MappingEntity mappingEntity : mappingEntities) {
      List<MappingEntity> rest = new ArrayList<>(mappingEntities);
      rest.remove(mappingEntity);
      Set<MappingEntitySuggestion> suggestions = getSuggestions(mappingEntity, rest);
      mappingEntity.setMappingEntitySuggestions(suggestions);
    }
  }

  /**
   * Calculates a list of entities that are similar to a given Mapping entity object. A mapping "A"
   * is similar to a mapping "B" if the values for their key attributes are similar. That similarity
   * is calculated by a string similarity algorithm and uses the weights that each key attribute has
   * configured.
   *
   * @param mappingEntity Entity for which similar entities are wanted.
   * @param mappingEntities Entities where the similar entities are going to be searched.
   * @return List of {@link MappingEntity} that represent the most similar entities to the given
   *     mappingEntity.
   */
  public Set<MappingEntitySuggestion> getSuggestions(
      MappingEntity mappingEntity, List<MappingEntity> mappingEntities) {

    Map<String, String> leftValues = mappingEntity.getValuesAsMap();
    Map<String, Double> weights = mappingEntity.getEntityType().getWeightsAsMap();
    System.out.println("leftValues: " + leftValues);
    System.out.println("weights: " + weights);

    Map<Double, List<MappingEntity>> perfectMatches = new TreeMap<>(Collections.reverseOrder());
    Map<Double, List<MappingEntity>> acceptableMatches = new TreeMap<>(Collections.reverseOrder());
    Map<Double, List<MappingEntity>> orderedSuggestions = new TreeMap<>(Collections.reverseOrder());

    int numberOfPerfectMatches = 0;

    for (MappingEntity element : mappingEntities) {
      Map<String, String> rightValues = element.getValuesAsMap();
      System.out.println("rightValues: " + rightValues);
      double score =
          termsWeightedSimilarityCalculator.calculateTermsWeightedSimilarity(
              leftValues, rightValues, weights);
      if (score >= similarityConfigurationReader.getSimilarityPerfectMatchScore()) {
        if (!perfectMatches.containsKey(score)) {
          perfectMatches.put(score, new ArrayList<>());
        }
        perfectMatches.get(score).add(element);
        numberOfPerfectMatches++;
        if (numberOfPerfectMatches
            >= similarityConfigurationReader.getNumberOfSuggestedMappingsPerEntity()) {
          break;
        }
      } else if (score >= similarityConfigurationReader.getSimilarityAcceptableMatchScore()) {
        if (!acceptableMatches.containsKey(score)) {
          acceptableMatches.put(score, new ArrayList<>());
        }
        acceptableMatches.get(score).add(element);
      }
    }
    orderedSuggestions.putAll(perfectMatches);
    orderedSuggestions.putAll(acceptableMatches);
    return buildMappingEntitySuggestion(mappingEntity, orderedSuggestions);
  }

  private Set<MappingEntitySuggestion> buildMappingEntitySuggestion(
      MappingEntity mappingEntity, Map<Double, List<MappingEntity>> allSuggestions) {

    Set<MappingEntitySuggestion> mappingEntitySuggestions = new HashSet<>();

    outer:
    for (Double keySet : allSuggestions.keySet()) {
      List<MappingEntity> mappingEntities = allSuggestions.get(keySet);
      for (MappingEntity suggested : mappingEntities) {
        MappingEntitySuggestion mappingEntitySuggestion = new MappingEntitySuggestion();
        mappingEntitySuggestion.setMappingEntity(mappingEntity);
        mappingEntitySuggestion.setSuggestedMappingEntity(suggested);
        mappingEntitySuggestion.setScore(keySet);

        mappingEntitySuggestions.add(mappingEntitySuggestion);
        if (mappingEntitySuggestions.size()
            >= similarityConfigurationReader.getNumberOfSuggestedMappingsPerEntity()) {
          break outer;
        }
      }
    }
    return mappingEntitySuggestions;
  }

}
