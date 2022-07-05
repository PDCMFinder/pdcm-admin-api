package org.cancermodels.mappings.suggestions;

import static org.cancermodels.mappings.suggestions.SuggestionsConstants.JARO_WINKLER;

import org.cancermodels.prototype.JaroWinklerDistanceSimilarityComparator;
import org.cancermodels.prototype.SimilarityComparator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SimilarityConfigurationReader {

  public SimilarityConfigurationReader() {
  }

  @Value( "${similarity_perfect_match_score}" )
  private double similarityPerfectMatchScore;

  @Value( "${similarity_acceptable_match_score}" )
  private double similarityAcceptableMatchScore;

  @Value( "${similarity_algorithm}" )
  private String similarityAlgorithm;

  @Value( "${number_of_suggested_mappings}" )
  private int numberOfSuggestedMappingsPerEntity;

  public double getSimilarityPerfectMatchScore() {
    return similarityPerfectMatchScore;
  }

  public double getSimilarityAcceptableMatchScore() {
    if (similarityAcceptableMatchScore > similarityPerfectMatchScore) {
      throw new IllegalArgumentException(
          "similarityPerfectMatchScore must be greater than similarityAcceptableMatchScore");
    }
    return similarityAcceptableMatchScore;
  }

  public SimilarityComparator getSimilarityAlgorithm() {
    if (JARO_WINKLER.equalsIgnoreCase(similarityAlgorithm)) {
      return new JaroWinklerDistanceSimilarityComparator();
    } else {
      throw new IllegalArgumentException(
          "No valid similarityAlgorithm was provided in the configuration: " + similarityAlgorithm);
    }
  }

  public int getNumberOfSuggestedMappingsPerEntity() {
    return numberOfSuggestedMappingsPerEntity;
  }
}
