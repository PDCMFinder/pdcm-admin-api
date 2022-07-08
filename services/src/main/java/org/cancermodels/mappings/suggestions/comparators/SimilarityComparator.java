package org.cancermodels.mappings.suggestions.comparators;

public interface SimilarityComparator {
  int calculate(String termA, String termB);
}
