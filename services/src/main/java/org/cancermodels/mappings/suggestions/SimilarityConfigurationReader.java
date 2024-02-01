package org.cancermodels.mappings.suggestions;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SimilarityConfigurationReader {

  public SimilarityConfigurationReader() {
  }

  @Value( "${candidateThreshold}" )
  private int candidateThreshold;

  @Value( "${automaticWithRevisionThreshold}" )
  private int automaticWithRevisionThreshold;

  @Value( "${automaticDirectThreshold}" )
  private int automaticDirectThreshold;

  @Value( "${number_of_suggested_mappings}" )
  private int numberOfSuggestedMappingsPerEntity;

  @Value( "${requiredConsensusNumber}" )
  private int requiredConsensusNumber;

}
