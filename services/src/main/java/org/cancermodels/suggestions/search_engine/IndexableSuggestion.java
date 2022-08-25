package org.cancermodels.suggestions.search_engine;

import lombok.Data;

/**
 * A mapping suggestion is either a rule or an ontology term. This class describes the
 * information needed to be indexed so the process can later choose the best suggestions
 * to map an entity.
 */
@Data
public class IndexableSuggestion {

  /**
   * Identifier of the suggestion
   */
  private String id;

  /**
   * Indicates if the suggestion is Rule or Ontology.
   */
  private String sourceType;

  private IndexableOntologySuggestion ontology;

  private IndexableRuleSuggestion rule;

}

