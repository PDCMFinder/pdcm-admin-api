package org.cancermodels.admin.dtos;

import java.util.Set;
import lombok.Data;

@Data
public class OntologySuggestionDTO {

  private String ncit;
  private String url;
  private String ontologyTermLabel;
  private String description;
  private Set<String> synonyms;
}
