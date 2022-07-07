package org.cancermodels.mappings.suggestions;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class ScoreManager<T> {

  private final SimilarityConfigurationReader similarityConfigurationReader;

  public ScoreManager(
      SimilarityConfigurationReader similarityConfigurationReader) {
    this.similarityConfigurationReader = similarityConfigurationReader;
  }

  /**
   * Process the similarity score obtained when calculating mappings suggestions.
   * @param score Obtained score.
   * @param currentNumberOfPerfectMatches Current number of perfect matches. This helps to decide
   *                                      whether enough "perfect matches" are already processes in
   *                                      which case the search for the entity can stop.
   * @param perfectMatches                Map with the suggestions that have a perfect score.
   * @param acceptableMatches             Map with the suggestions that have an acceptable score.
   * @param element                       Suggestion
   * @return                              Whether or not the search process should continue
   *                                      for "element".
   */
  public boolean processScore(
      int score,
      AtomicInteger currentNumberOfPerfectMatches,
      Map<Integer, Set<T>> perfectMatches,
      Map<Integer, Set<T>> acceptableMatches,
      T element) {

    boolean shouldKeepSearching = true;

    if (score >= similarityConfigurationReader.getSimilarityPerfectMatchScore()) {
      addElementToMap(perfectMatches, element, score);
      currentNumberOfPerfectMatches.incrementAndGet();

      // If the number of perfect matches required to finish the process earlier is reached then
      // return false indicating no more searching for the entity is required
      if (currentNumberOfPerfectMatches.get() >=
          similarityConfigurationReader.getPerfectMatchesToFinishEarlier()) {
        shouldKeepSearching = false;
      }
    } else if (score >= similarityConfigurationReader.getSimilarityAcceptableMatchScore()) {
      addElementToMap(acceptableMatches, element, score);
    }

    return shouldKeepSearching;
  }

  private void addElementToMap(Map<Integer, Set<T>> map, T element, int key) {
    if (!map.containsKey(key)) {
      map.put(key, new HashSet<>());
    }
    map.get(key).add(element);
  }

}
