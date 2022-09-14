package org.cancermodels.admin.mappers;

import org.cancermodels.admin.dtos.MappingEntitySuggestionDTO;
import org.cancermodels.admin.dtos.OntologySuggestionDTO;
import org.cancermodels.admin.dtos.SuggestionDTO;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.OntologyTerm;
import org.cancermodels.persistance.Suggestion;
import org.springframework.stereotype.Component;

@Component
public class SuggestionMapper {

  public SuggestionDTO convertToDto(Suggestion suggestion) {
    SuggestionDTO suggestionDTO = new SuggestionDTO();
    suggestionDTO.setSourceType(suggestion.getSourceType());
    suggestionDTO.setScore(suggestion.getScore());
    suggestionDTO.setRelativeScore(suggestion.getRelativeScore());
    suggestionDTO.setRule(convertMappingEntityToSuggestion(suggestion.getMappingEntity()));
    suggestionDTO.setOntology(convertOntologyTermToSuggestion(suggestion.getOntologyTerm()));
    suggestionDTO.setSuggestedTermUrl(suggestion.getSuggestedTermUrl());
    suggestionDTO.setSuggestedTermLabel(suggestion.getSuggestedTermLabel());
    return suggestionDTO;
  }

  public MappingEntitySuggestionDTO convertMappingEntityToSuggestion(MappingEntity mappingEntity) {
    MappingEntitySuggestionDTO mappingEntitySuggestionDTO = null;
    if (mappingEntity != null) {
      mappingEntitySuggestionDTO = new MappingEntitySuggestionDTO();
      mappingEntitySuggestionDTO.setEntityTypeName(mappingEntity.getEntityType().getName());
      mappingEntitySuggestionDTO.setValues(mappingEntity.getValuesAsMap());
    }

    return mappingEntitySuggestionDTO;
  }

  public OntologySuggestionDTO convertOntologyTermToSuggestion(OntologyTerm ontologyTerm) {
    OntologySuggestionDTO ontologySuggestionDTO = null;

    if (ontologyTerm != null) {
      ontologySuggestionDTO = new OntologySuggestionDTO();
      ontologySuggestionDTO.setNcit(ontologyTerm.getKey());
      ontologySuggestionDTO.setUrl(ontologyTerm.getUrl());
      ontologySuggestionDTO.setOntologyTermLabel(ontologyTerm.getLabel());
      ontologySuggestionDTO.setSynonyms(ontologyTerm.getSynonyms());
      ontologySuggestionDTO.setDescription(ontologyTerm.getDescription());
    }
    return ontologySuggestionDTO;
  }
}
