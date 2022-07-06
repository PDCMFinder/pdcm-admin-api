package org.cancermodels.admin.mappers;

import org.cancermodels.OntologySuggestion;
import org.cancermodels.admin.dtos.OntologySuggestionDTO;
import org.cancermodels.admin.dtos.OntologyTermDTO;
import org.springframework.stereotype.Component;

@Component
public class OntologySuggestionMapper {
  private final OntologyTermMapper ontologyTermMapper;

  public OntologySuggestionMapper(
      OntologyTermMapper ontologyTermMapper) {
    this.ontologyTermMapper = ontologyTermMapper;
  }

  public OntologySuggestionDTO convertToDto(OntologySuggestion ontologySuggestion) {
    OntologySuggestionDTO ontologySuggestionDTO = new OntologySuggestionDTO();
    OntologyTermDTO ontologyTermDTO = ontologyTermMapper.convertToDto(
        ontologySuggestion.getOntologyTerm());
    ontologySuggestionDTO.setOntologyTermDTO(ontologyTermDTO);
    ontologySuggestionDTO.setScore(ontologySuggestion.getScore());
    return ontologySuggestionDTO;
  }
}
