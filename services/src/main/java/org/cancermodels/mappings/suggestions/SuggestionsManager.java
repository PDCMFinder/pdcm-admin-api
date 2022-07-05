package org.cancermodels.mappings.suggestions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntityRepository;
import org.cancermodels.MappingEntitySuggestion;
import org.cancermodels.MappingEntitySuggestionRepository;
import org.cancermodels.prototype.SimilarityComparator;
import org.cancermodels.prototype.TermsWeightedSimilarityCalculator;
import org.springframework.stereotype.Service;

@Service
public class SuggestionsManager {
  private final SimilarityConfigurationReader similarityConfigurationReader;
  private final SimilarityComparator similarityComparator;
  private final TermsWeightedSimilarityCalculator termsWeightedSimilarityCalculator;
  private final MappingEntitySuggestionRepository mappingEntitySuggestionRepository;
  private MappingEntityRepository mappingEntityRepository;

  public SuggestionsManager(SimilarityConfigurationReader similarityConfigurationReader,
      MappingEntitySuggestionRepository mappingEntitySuggestionRepository,
      MappingEntityRepository mappingEntityRepository) {
    this.similarityConfigurationReader = similarityConfigurationReader;
    similarityComparator = similarityConfigurationReader.getSimilarityAlgorithm();
    this.mappingEntitySuggestionRepository = mappingEntitySuggestionRepository;
    this.mappingEntityRepository = mappingEntityRepository;
    termsWeightedSimilarityCalculator = new TermsWeightedSimilarityCalculator(similarityComparator);
  }

  public void updateSuggestedMappingsByExistingRules(List<MappingEntity> mappingEntities) {
    for (MappingEntity mappingEntity : mappingEntities) {
      List<MappingEntity> rest = new ArrayList<>(mappingEntities);
      rest.remove(mappingEntity);

      mappingEntity.getMappingEntitySuggestions().clear();
      mappingEntityRepository.save(mappingEntity);

      Set<MappingEntitySuggestion> suggestions = getSuggestions(mappingEntity, rest);
      mappingEntity.getMappingEntitySuggestions().addAll(suggestions);

      mappingEntitySuggestionRepository.saveAll(suggestions);
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

    Map<Double, List<MappingEntity>> perfectMatches = new TreeMap<>(Collections.reverseOrder());
    Map<Double, List<MappingEntity>> acceptableMatches = new TreeMap<>(Collections.reverseOrder());
    Map<Double, List<MappingEntity>> orderedSuggestions = new TreeMap<>(Collections.reverseOrder());

    int numberOfPerfectMatches = 0;

    for (MappingEntity element : mappingEntities) {
      Map<String, String> rightValues = element.getValuesAsMap();
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
    return buildMappingEntitySuggestion(orderedSuggestions);
  }

  private Set<MappingEntitySuggestion> buildMappingEntitySuggestion(
      Map<Double, List<MappingEntity>> allSuggestions) {

    Set<MappingEntitySuggestion> mappingEntitySuggestions = new HashSet<>();

    outer:
    for (Double keySet : allSuggestions.keySet()) {
      List<MappingEntity> mappingEntities = allSuggestions.get(keySet);
      for (MappingEntity suggested : mappingEntities) {
        MappingEntitySuggestion mappingEntitySuggestion = new MappingEntitySuggestion();
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

  public void testOne(MappingEntity mappingEntity, List<MappingEntity> mappingEntities) {
    System.out.println(mappingEntity);
    System.out.println("Current number of suggestions: " +
        mappingEntity.getMappingEntitySuggestions().size());

    Set<MappingEntitySuggestion> suggestions = getSuggestions(mappingEntity, mappingEntities);
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
