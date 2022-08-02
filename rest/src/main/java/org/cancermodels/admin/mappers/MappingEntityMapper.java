package org.cancermodels.admin.mappers;

import org.cancermodels.MappingEntity;
import org.cancermodels.admin.dtos.MappingEntityDTO;
import org.springframework.stereotype.Component;

@Component
public class MappingEntityMapper {

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

    mappingEntityDTO.setSuggestions(mappingEntity.getSuggestions());

    return mappingEntityDTO;
  }

}
