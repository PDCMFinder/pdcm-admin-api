package org.cancermodels.suggestions.index;

import java.util.Map;
import java.util.Set;
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

@Data
class IndexableOntologySuggestion {
  private String ontologyTermId;
  private String ontologyTermLabel;
  private String definition;
  private Set<String> synonyms;
}

@Data
class IndexableRuleSuggestion {
  private String mappedTermUrl;
  private String mappedTermLabel;
  private Map<String, String> data;
}
