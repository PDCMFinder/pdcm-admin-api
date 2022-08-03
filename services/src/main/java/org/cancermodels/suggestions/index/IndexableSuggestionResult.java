package org.cancermodels.suggestions.index;

import lombok.Data;

/**
 * A mapping suggestion is either a rule or an ontology term. This class describes the
 * information needed to be indexed so the process can later choose the best suggestions
 * to map an entity.
 */
@Data
public class IndexableSuggestionResult {

  /**
   * Suggestion obtained as a result of a search.
   */
  private IndexableSuggestion indexableSuggestion;

  /**
   * Score assigned by lucene.
   */
  private double score;

}
