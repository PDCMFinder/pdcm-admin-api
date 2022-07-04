package org.cancermodels.admin.mappers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntitySuggestion;
import org.cancermodels.admin.dtos.MappingEntityDTO;
import org.cancermodels.admin.dtos.MappingEntitySuggestionDTO;
import org.springframework.stereotype.Component;

@Component
public class MappingEntityMapper {

  private MappingEntitySuggestionMapper mappingEntitySuggestionMapper;

  public MappingEntityMapper(
      MappingEntitySuggestionMapper mappingEntitySuggestionMapper) {
    this.mappingEntitySuggestionMapper = mappingEntitySuggestionMapper;
  }

  public MappingEntityDTO convertToDto(MappingEntity mappingEntity) {
    MappingEntityDTO mappingEntityDTO = new MappingEntityDTO();
    mappingEntityDTO.setId(mappingEntity.getId());
    mappingEntityDTO.setEntityTypeName(mappingEntity.getEntityType().getName());
    mappingEntityDTO.setMappingValues(mappingEntity.getValuesAsMap());
    mappingEntityDTO.setMappedTermUrl(mappingEntity.getMappedTermUrl());
    mappingEntityDTO.setMappedTermLabel(mappingEntity.getMappedTermLabel());
    mappingEntityDTO.setStatus(mappingEntity.getStatus());
    mappingEntityDTO.setDateCreated(mappingEntity.getDateCreated());
    mappingEntityDTO.setDateUpdated(mappingEntity.getDateUpdated());

    List<MappingEntitySuggestionDTO> mappingEntitySuggestionDTOS =
        convertToMappingEntitySuggestionDto(mappingEntity.getMappingEntitySuggestions());
    mappingEntityDTO.setSuggestedMappings(mappingEntitySuggestionDTOS);
    return mappingEntityDTO;
  }

  private List<MappingEntitySuggestionDTO> convertToMappingEntitySuggestionDto(
      Set<MappingEntitySuggestion> mappingEntitySuggestions) {

    List<MappingEntitySuggestionDTO>  mappingEntitySuggestionDTOS = new ArrayList<>();
    for (MappingEntitySuggestion suggestion : mappingEntitySuggestions)
    {
      mappingEntitySuggestionDTOS.add(mappingEntitySuggestionMapper.convertToDto(suggestion));
    }
    mappingEntitySuggestionDTOS.sort(
        Comparator.comparing(MappingEntitySuggestionDTO::getScore).reversed());

    return mappingEntitySuggestionDTOS;
  }

}
