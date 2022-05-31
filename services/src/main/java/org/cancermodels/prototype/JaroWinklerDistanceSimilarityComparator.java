package org.cancermodels.prototype;

import org.apache.commons.text.similarity.JaroWinklerDistance;

public class JaroWinklerDistanceSimilarityComparator implements SimilarityComparator {

  @Override
  public Double calculate(String termA, String termB) {
    JaroWinklerDistance distance = new JaroWinklerDistance();

    return distance.apply(termA.toLowerCase(), termB.toLowerCase());
  }
}
