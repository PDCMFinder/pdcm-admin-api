package org.cancermodels.mappings.suggestions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntityRepository;
import org.cancermodels.MappingEntitySuggestion;
import org.cancermodels.MappingEntitySuggestionRepository;
import org.cancermodels.mappings.Status;
import org.cancermodels.prototype.SimilarityComparator;
import org.cancermodels.prototype.TermsWeightedSimilarityCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This class calculates the mapping suggestions using existing mapping entities and searching on
 * them for the ones that are similar so can be assigned as suggestions.
 */

@Service
public class MappingEntitiesSuggestionManager {
  private final SimilarityConfigurationReader similarityConfigurationReader;
  private final TermsWeightedSimilarityCalculator termsWeightedSimilarityCalculator;
  private final MappingEntitySuggestionRepository mappingEntitySuggestionRepository;
  private final MappingEntityRepository mappingEntityRepository;
  private final ScoreManager<MappingEntity> scoreManager;

  private static final Logger LOG = LoggerFactory.getLogger(MappingEntitiesSuggestionManager.class);

  public MappingEntitiesSuggestionManager(
      SimilarityConfigurationReader similarityConfigurationReader,
      MappingEntitySuggestionRepository mappingEntitySuggestionRepository,
      MappingEntityRepository mappingEntityRepository,
      ScoreManager<MappingEntity> scoreManager) {

    this.similarityConfigurationReader = similarityConfigurationReader;
    this.scoreManager = scoreManager;
    SimilarityComparator similarityComparator = similarityConfigurationReader
        .getSimilarityAlgorithm();
    this.mappingEntitySuggestionRepository = mappingEntitySuggestionRepository;
    this.mappingEntityRepository = mappingEntityRepository;
    termsWeightedSimilarityCalculator = new TermsWeightedSimilarityCalculator(similarityComparator);
  }

  /**
   * Calculate mapping entities based suggestions.
   *
   * @param mappingEntities Mapping entities for which the suggestions are going to be calculated.
   * @return Map with the suggestions for each entity.
   */
  public Map<MappingEntity, Set<MappingEntitySuggestion>> calculateSuggestions(
      List<MappingEntity> mappingEntities) {

    Map<MappingEntity, Set<MappingEntitySuggestion>> suggestionsByEntities = new HashMap<>();

    // Suggestions are only searched on mapped entities
    List<MappingEntity> mappedEntities = mappingEntities.stream()
        .filter(x -> x.getStatus().equalsIgnoreCase(Status.MAPPED.getLabel()))
        .collect(Collectors.toList());
    LOG.info("Number of mapped entities: " + mappedEntities.size());

    for (MappingEntity mappingEntity : mappingEntities) {
      List<MappingEntity> mappingsToSearchOn = new ArrayList<>(mappedEntities);
      mappingsToSearchOn.remove(mappingEntity);
      Set<MappingEntitySuggestion> suggestions =
          calculateSuggestionsForEntity(mappingEntity, mappingsToSearchOn);
      suggestionsByEntities.put(mappingEntity, suggestions);
    }
    return suggestionsByEntities;
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
  public Set<MappingEntitySuggestion> calculateSuggestionsForEntity(
      MappingEntity mappingEntity, List<MappingEntity> mappingEntities) {

    Map<String, String> leftValues = mappingEntity.getValuesAsMap();
    Map<String, Double> weights = mappingEntity.getEntityType().getWeightsAsMap();

    Map<Double, Set<MappingEntity>> perfectMatches = new TreeMap<>(Collections.reverseOrder());
    Map<Double, Set<MappingEntity>> acceptableMatches = new TreeMap<>(Collections.reverseOrder());
    Map<Double, Set<MappingEntity>> orderedSuggestions = new TreeMap<>(Collections.reverseOrder());

    int numberOfPerfectMatches = 0;

    for (MappingEntity element : mappingEntities) {
      Map<String, String> rightValues = element.getValuesAsMap();
      double score =
          termsWeightedSimilarityCalculator.calculateTermsWeightedSimilarity(
              leftValues, rightValues, weights);

      boolean shouldKeepSearching = scoreManager.processScore(
          score, numberOfPerfectMatches, perfectMatches, acceptableMatches, element);

      if (!shouldKeepSearching) {
        break;
      }

//      if (score >= similarityConfigurationReader.getSimilarityPerfectMatchScore()) {
//        if (!perfectMatches.containsKey(score)) {
//          perfectMatches.put(score, new HashSet<>());
//        }
//        perfectMatches.get(score).add(element);
//        numberOfPerfectMatches++;
//        if (numberOfPerfectMatches
//            >= similarityConfigurationReader.getPerfectMatchesToFinishEarlier()) {
//          break;
//        }
//      } else if (score >= similarityConfigurationReader.getSimilarityAcceptableMatchScore()) {
//        if (!acceptableMatches.containsKey(score)) {
//          acceptableMatches.put(score, new HashSet<>());
//        }
//        acceptableMatches.get(score).add(element);
//      }
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
  private Set<MappingEntitySuggestion> getBestSuggestions(
      Map<Double, Set<MappingEntity>> allSuggestions) {

    Set<MappingEntitySuggestion> mappingEntitySuggestions = new HashSet<>();

    outer:
    for (Double key : allSuggestions.keySet()) {
      Set<MappingEntity> mappingEntities = allSuggestions.get(key);
      for (MappingEntity suggested : mappingEntities) {
        MappingEntitySuggestion mappingEntitySuggestion = new MappingEntitySuggestion();
        mappingEntitySuggestion.setSuggestedMappingEntity(suggested);
        mappingEntitySuggestion.setScore(key);

        mappingEntitySuggestions.add(mappingEntitySuggestion);
        if (mappingEntitySuggestions.size()
            >= similarityConfigurationReader.getNumberOfSuggestedMappingsPerEntity()) {
          break outer;
        }
      }
    }
    return mappingEntitySuggestions;
  }

  public void testOne(MappingEntity mappingEntity, List<MappingEntity> mappingEntities) {
    System.out.println(mappingEntity);
    System.out.println("Current number of suggestions: " +
        mappingEntity.getMappingEntitySuggestions().size());

    Set<MappingEntitySuggestion> suggestions = calculateSuggestionsForEntity(mappingEntity, mappingEntities);
    System.out.println("Calculated: " + suggestions.size() + " suggestions");

    mappingEntity.getMappingEntitySuggestions().clear();
    mappingEntityRepository.save(mappingEntity);
    System.out.println("After deleting number of suggestions: " +
        mappingEntity.getMappingEntitySuggestions().size());
//    mappingEntity.setMappingEntitySuggestions(suggestions);
    mappingEntity.getMappingEntitySuggestions().addAll(suggestions);
    System.out.println("After manual assignation number of suggestions: " +
        mappingEntity.getMappingEntitySuggestions().size());
   mappingEntitySuggestionRepository.saveAll(suggestions);
  }

}
