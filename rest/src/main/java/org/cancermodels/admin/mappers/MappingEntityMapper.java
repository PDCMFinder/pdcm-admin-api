package org.cancermodels.admin.mappers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntitySuggestion;
import org.cancermodels.OntologySuggestion;
import org.cancermodels.admin.dtos.MappingEntityDTO;
import org.cancermodels.admin.dtos.MappingEntitySuggestionDTO;
import org.cancermodels.admin.dtos.OntologySuggestionDTO;
import org.springframework.stereotype.Component;

@Component
public class MappingEntityMapper {

  private final MappingEntitySuggestionMapper mappingEntitySuggestionMapper;
  private final OntologySuggestionMapper ontologySuggestionMapper;

  public MappingEntityMapper(
      MappingEntitySuggestionMapper mappingEntitySuggestionMapper,
      OntologySuggestionMapper ontologySuggestionMapper) {
    this.mappingEntitySuggestionMapper = mappingEntitySuggestionMapper;
    this.ontologySuggestionMapper = ontologySuggestionMapper;
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
    mappingEntityDTO.setMappingEntitySuggestionDTOS(mappingEntitySuggestionDTOS);

    List<OntologySuggestionDTO> ontologySuggestionDTOS =
        convertToOntologySuggestionDTO(mappingEntity.getOntologySuggestions());
    mappingEntityDTO.setOntologySuggestionDTOS(ontologySuggestionDTOS);

    return mappingEntityDTO;
  }

  private List<MappingEntitySuggestionDTO> convertToMappingEntitySuggestionDto(
      List<MappingEntitySuggestion> mappingEntitySuggestions) {

    List<MappingEntitySuggestionDTO>  mappingEntitySuggestionDTOS = new ArrayList<>();
    for (MappingEntitySuggestion suggestion : mappingEntitySuggestions)
    {
      mappingEntitySuggestionDTOS.add(mappingEntitySuggestionMapper.convertToDto(suggestion));
    }
    mappingEntitySuggestionDTOS.sort(
        Comparator.comparing(MappingEntitySuggestionDTO::getScore).reversed());

    return mappingEntitySuggestionDTOS;
  }

  private List<OntologySuggestionDTO> convertToOntologySuggestionDTO(
      List<OntologySuggestion> ontologySuggestions) {

    List<OntologySuggestionDTO>  ontologySuggestionDTOS = new ArrayList<>();
    for (OntologySuggestion suggestion : ontologySuggestions)
    {
      ontologySuggestionDTOS.add(ontologySuggestionMapper.convertToDto(suggestion));
    }
    ontologySuggestionDTOS.sort(
        Comparator.comparing(OntologySuggestionDTO::getScore).reversed());

    return ontologySuggestionDTOS;
  }

}
