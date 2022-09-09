package org.cancermodels.admin.mappers;

import java.util.stream.Collectors;
import org.cancermodels.admin.dtos.MappingValueDTO;
import org.cancermodels.admin.dtos.MappingValueDTO.MappingKeyDTO;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.admin.dtos.MappingEntityDTO;
import org.cancermodels.persistance.MappingValue;
import org.springframework.stereotype.Component;

@Component
public class MappingEntityMapper {

  public MappingEntityDTO convertToDto(MappingEntity mappingEntity) {
    MappingEntityDTO mappingEntityDTO = new MappingEntityDTO();
    mappingEntityDTO.setId(mappingEntity.getId());
    mappingEntityDTO.setEntityTypeName(mappingEntity.getEntityType().getName());
    mappingEntityDTO.setMappingValues(mappingEntity.getMappingValues().stream().map(
        this::convertToDto).collect(
        Collectors.toList()));
    mappingEntityDTO.setMappedTermUrl(mappingEntity.getMappedTermUrl());
    mappingEntityDTO.setMappedTermLabel(mappingEntity.getMappedTermLabel());
    mappingEntityDTO.setStatus(mappingEntity.getStatus());
    mappingEntityDTO.setMappingType(mappingEntity.getMappingType());
    mappingEntityDTO.setSource(mappingEntity.getSource());
    mappingEntityDTO.setDateCreated(mappingEntity.getDateCreated());
    mappingEntityDTO.setDateUpdated(mappingEntity.getDateUpdated());

    return mappingEntityDTO;
  }

  private MappingValueDTO convertToDto(MappingValue mappingValue) {
    MappingValueDTO mappingValueDTO = new MappingValueDTO();
    MappingValueDTO.MappingKeyDTO mappingKeyDTO = new MappingKeyDTO();
    mappingKeyDTO.setKey(mappingValue.getMappingKey().getKey());
    mappingValueDTO.setMappingKey(mappingKeyDTO);
    mappingValueDTO.setValue(mappingValue.getValue());
    return mappingValueDTO;
  }

}
