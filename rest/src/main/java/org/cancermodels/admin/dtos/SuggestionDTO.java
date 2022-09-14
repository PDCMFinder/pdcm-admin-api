package org.cancermodels.admin.dtos;

import lombok.Data;

@Data
public class SuggestionDTO {

  private String sourceType;
  private String suggestedTermLabel;
  private String suggestedTermUrl;
  private double score;
  private double relativeScore;
  private MappingEntitySuggestionDTO rule;
  private OntologySuggestionDTO ontology;
}
