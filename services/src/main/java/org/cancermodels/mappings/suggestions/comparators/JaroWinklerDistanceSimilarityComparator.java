package org.cancermodels.mappings.suggestions.comparators;

import org.apache.commons.text.similarity.JaroWinklerDistance;

public class JaroWinklerDistanceSimilarityComparator implements SimilarityComparator {

  @Override
  public int calculate(String termA, String termB) {
    JaroWinklerDistance distance = new JaroWinklerDistance();

    return (int)(distance.apply(termA.toLowerCase(), termB.toLowerCase()) * 100);
  }
}
