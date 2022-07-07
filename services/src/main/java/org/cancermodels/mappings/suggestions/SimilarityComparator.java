package org.cancermodels.mappings.suggestions;

public interface SimilarityComparator {
  int calculate(String termA, String termB);
}
