package org.cancermodels.admin.mappers;

import org.cancermodels.MappingEntitySuggestion;
import org.cancermodels.admin.dtos.MappingEntitySuggestionDTO;
import org.springframework.stereotype.Component;

@Component
public class MappingEntitySuggestionMapper {

  public MappingEntitySuggestionDTO convertToDto(MappingEntitySuggestion mappingEntitySuggestion) {
    MappingEntitySuggestionDTO mappingEntityDTO = new MappingEntitySuggestionDTO();
    mappingEntityDTO.setMappingValues(mappingEntitySuggestion.getSuggestedMappingEntity().getValuesAsMap());
    mappingEntityDTO.setMappedTermLabel(mappingEntitySuggestion.getSuggestedMappingEntity().getMappedTermLabel());
    mappingEntityDTO.setMappedTermUrl(mappingEntitySuggestion.getSuggestedMappingEntity().getMappedTermUrl());
    mappingEntityDTO.setScore(mappingEntitySuggestion.getScore());
    return mappingEntityDTO;
  }
}
