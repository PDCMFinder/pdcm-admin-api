package org.cancermodels.mappings.suggestions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SimilarityConfigurationReader {

  public SimilarityConfigurationReader() {
  }

  @Value( "${similarity_perfect_match_score}" )
  private int similarityPerfectMatchScore;

  @Value( "${similarity_acceptable_match_score}" )
  private int similarityAcceptableMatchScore;

  @Value( "${number_of_suggested_mappings}" )
  private int numberOfSuggestedMappingsPerEntity;

  @Value( "${perfect_matches_to_finish_earlier}" )
  private int perfectMatchesToFinishEarlier;

  public int getSimilarityPerfectMatchScore() {
    return similarityPerfectMatchScore;
  }

  public double getSimilarityAcceptableMatchScore() {
    if (similarityAcceptableMatchScore > similarityPerfectMatchScore) {
      throw new IllegalArgumentException(
          "similarityPerfectMatchScore must be greater than similarityAcceptableMatchScore");
    }
    return similarityAcceptableMatchScore;
  }

  public int getNumberOfSuggestedMappingsPerEntity() {
    return numberOfSuggestedMappingsPerEntity;
  }

  public int getPerfectMatchesToFinishEarlier() {
    return perfectMatchesToFinishEarlier;
  }
}
