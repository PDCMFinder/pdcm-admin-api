package org.cancermodels.suggestions.index;

import java.util.Set;
import lombok.Data;

@Data
public
class IndexableOntologySuggestion {

  private String ontologyTermId;
  private String ontologyTermLabel;
  private String definition;
  private Set<String> synonyms;
}
