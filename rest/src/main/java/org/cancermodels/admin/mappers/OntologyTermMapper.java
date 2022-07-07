package org.cancermodels.admin.mappers;

import org.cancermodels.OntologyTerm;
import org.cancermodels.admin.dtos.OntologyTermDTO;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermMapper {

  public OntologyTermDTO convertToDto(OntologyTerm ontologyTerm) {
    OntologyTermDTO ontologyTermDTO = new OntologyTermDTO();
    ontologyTermDTO.setId(ontologyTerm.getId());
    ontologyTermDTO.setLabel(ontologyTerm.getLabel());
    ontologyTermDTO.setUrl(ontologyTerm.getUrl());
    ontologyTermDTO.setType(ontologyTerm.getType());
    ontologyTermDTO.setSynonyms(ontologyTerm.getSynonyms());
    return ontologyTermDTO;
  }
}
