package org.cancermodels.suggestions.index;

import java.util.Map;
import lombok.Data;

@Data
public
class IndexableRuleSuggestion {

  private String mappedTermUrl;
  private String mappedTermLabel;
  private String entityTypeName;
  private Map<String, String> data;
}
