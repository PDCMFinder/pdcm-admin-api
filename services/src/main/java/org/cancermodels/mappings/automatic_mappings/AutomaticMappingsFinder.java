package org.cancermodels.mappings.automatic_mappings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.Suggestion;
import org.cancermodels.suggestions.search_engine.SuggestionsSearcher;
import org.springframework.stereotype.Component;

/**
 * A class to find automatic mappings when possible
 */
@Component
public class AutomaticMappingsFinder {

  private final SuggestionsSearcher suggestionsSearcher;

  // To even be considered in the process, a suggestion should have a relative score higher than this.
  private static final double acceptableThreshold = 75;

  // A suggestion with a relative score equal or higher than this should be considered a perfect
  // match therefore can be used in an automatic mapping.
  private static final double perfectThreshold = 95;

  // Number of acceptable suggestions that must agree in their ontology term url to consider that
  // ontology term the good one for the automatic mapping
  private static final int requiredConsensusNumber = 3;

  public AutomaticMappingsFinder(
      SuggestionsSearcher suggestionsSearcher) {
    this.suggestionsSearcher = suggestionsSearcher;
  }

  /**
   * Find the best {@link Suggestion} for a {@link MappingEntity}. The process follows a set of
   * criteria to determine when a suggestion is good enough and then return this as a suitable
   * result to later take the suggestion and from it extract the ontology term and do the mapping
   * automatically.
   * @param mappingEntity {@link MappingEntity} to analyze
   * @return A {@link Suggestion} that represents a match that is so good in terms of similarity
   * to {@code mappingEntity} that it could be used to automatically do the mapping of the entity.
   */
  public Optional<Suggestion> findBestSuggestion(
      MappingEntity mappingEntity) {
    Optional<Suggestion> answer;
    List<Suggestion> suggestions = suggestionsSearcher.searchTopSuggestions(mappingEntity);
    List<Suggestion> processedSuggestions = filterOnlyAcceptableSuggestions(suggestions);
    processedSuggestions = sortDescByRelativeScore(processedSuggestions);

    answer = searchPerfectSuggestion(processedSuggestions);
    if (answer.isEmpty()) {
      answer = searchForConsensusAmongAcceptableSuggestions(processedSuggestions);
    }

    return answer;
  }

  private Optional<Suggestion> searchPerfectSuggestion(List<Suggestion> suggestions) {
    for (Suggestion suggestion : suggestions) {
      if (suggestion.getRelativeScore() >= perfectThreshold) {
        return Optional.of(suggestion);
      }
    }
    return Optional.empty();
  }

  // Evaluate if the first <<requiredConsensusNumber>> acceptable suggestions agree on their
  // suggestedTermUrl value. If true, return any of those acceptable suggestions.
  private Optional<Suggestion> searchForConsensusAmongAcceptableSuggestions(
      List<Suggestion> processedSuggestions) {
    if (processedSuggestions.size() < requiredConsensusNumber) {
      return Optional.empty();
    }
    boolean consensus = true;
    for (int i = 1; i < requiredConsensusNumber; i++) {
      consensus = consensus &&
          processedSuggestions.get(i).getSuggestedTermUrl()
              .equals(processedSuggestions.get(0).getSuggestedTermUrl());
    }
    if (consensus) {
      return Optional.of(processedSuggestions.get(0));
    } else {
      return Optional.empty();
    }
  }

  // We are only interested in suggestions that have a relative score equal or higher than
  // the defined acceptable threshold.
  private List<Suggestion> filterOnlyAcceptableSuggestions(List<Suggestion> suggestions) {
    return suggestions
        .stream()
        .filter(x -> x.getRelativeScore() >= acceptableThreshold)
        .collect(Collectors.toList());
  }

  // Suggestions need to be sorted desc order based on the relative score, so the first
  // element in the collection is the best (or one of the best) we got.
  private List<Suggestion> sortDescByRelativeScore(List<Suggestion> suggestions) {
    List<Suggestion> sortedSuggestionsDescOrder = new ArrayList<>(suggestions);
    sortedSuggestionsDescOrder.sort(Comparator.comparing(Suggestion::getRelativeScore).reversed());
    return sortedSuggestionsDescOrder;
  }

}
