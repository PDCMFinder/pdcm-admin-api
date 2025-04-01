package org.cancermodels.mappings.automatic_mappings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.cancermodels.mappings.suggestions.SimilarityConfigurationReader;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.pdcm_admin.persistance.Suggestion;
import org.springframework.stereotype.Component;

/**
 * A class to find automatic mappings when possible
 */
@Component
public class AutomaticMappingsFinder {

  private final SimilarityConfigurationReader similarityConfigurationReader;

  public AutomaticMappingsFinder(SimilarityConfigurationReader similarityConfigurationReader) {
    this.similarityConfigurationReader = similarityConfigurationReader;
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
//    List<Suggestion> suggestions = suggestionsSearcher.searchTopSuggestions(mappingEntity);
    // TODO: Here comes the logic to call e2o to get the suggestions
    List<Suggestion> suggestions = new ArrayList<>();
    List<Suggestion> processedSuggestions = filterOnlyAcceptableSuggestions(suggestions);
    processedSuggestions = sortDescByRelativeScore(processedSuggestions);

    // Check if there is a suggestion with a good enough score that it can be assigned as automatic
    answer = searchPerfectSuggestion(processedSuggestions);

    // If the score is not that high but several suggestions agree on the ontology term, then one of them is returned
    if (answer.isEmpty()) {
      answer = searchForConsensusAmongAcceptableSuggestions(processedSuggestions);
    }

    return answer;
  }

  private Optional<Suggestion> searchPerfectSuggestion(List<Suggestion> suggestions) {
    for (Suggestion suggestion : suggestions) {
      if (suggestion.getRelativeScore() >= similarityConfigurationReader.getAutomaticWithRevisionThreshold()) {
        return Optional.of(suggestion);
      }
    }
    return Optional.empty();
  }

  // Evaluate if the first <<requiredConsensusNumber>> acceptable suggestions agree on their
  // suggestedTermUrl value. If true, return any of those acceptable suggestions.
  private Optional<Suggestion> searchForConsensusAmongAcceptableSuggestions(
      List<Suggestion> processedSuggestions) {

    if (processedSuggestions.isEmpty()) {
      return Optional.empty();
    }
    if (processedSuggestions.size() < similarityConfigurationReader.getRequiredConsensusNumber()) {
      return Optional.empty();
    }
    boolean consensus = true;
    for (int i = 1; i < similarityConfigurationReader.getRequiredConsensusNumber(); i++) {
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
        .filter(x -> x.getRelativeScore() >= similarityConfigurationReader.getCandidateThreshold())
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
