package org.cancermodels.mapping_rules;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.stream.Collectors;
import org.cancermodels.EntityTypeName;
import org.cancermodels.mappings.MappingEntityService;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.types.Status;
import org.cancermodels.util.JSONHelper;
import org.springframework.stereotype.Service;

/**
 * Provides logic to handle the JSON mapping rules
 */
@Service
public class MappingRulesService {

  private final MappingEntityService mappingEntityService;

  public MappingRulesService(MappingEntityService mappingEntityService) {
    this.mappingEntityService = mappingEntityService;
  }

  public String buildMappingRulesJson(String entityTypeName)
      throws JsonProcessingException {
    validateEntityType(entityTypeName);

    // We are only interested in mapped terms
    List<MappingEntity> mappingEntities =
        mappingEntityService.getAllByTypeNameAndStatus(
            entityTypeName, Status.MAPPED.getLabel());

    // Convert the mapping entities to the expected format
    List<MappingRule> mappingRules = mappingEntities.stream()
        .map(this::mappingEntityToMappingRule)
        .collect(Collectors.toList());

    return JSONHelper.toJson(mappingRules);
  }

  private void validateEntityType(String entityType) {
    if (!EntityTypeName.Treatment.getLabel().equalsIgnoreCase(entityType)
        && !EntityTypeName.Diagnosis.getLabel().equalsIgnoreCase(entityType)) {
      throw new IllegalArgumentException("Entity type " + entityType + " does not exist.");
    }
  }

  private MappingRule mappingEntityToMappingRule(MappingEntity mappingEntity) {
    MappingRule mappingRule = new MappingRule();
    mappingRule.setMappingKey(mappingEntity.buildMappingKey());
    mappingRule.setEntityType(mappingEntity.getEntityType().getName());
    mappingRule.setMappingValues(mappingEntity.getValuesAsMap());
    mappingRule.setMappedTermLabel(mappingEntity.getMappedTermLabel());
    mappingRule.setMappedTermUrl(mappingEntity.getMappedTermUrl());
    mappingRule.setStatus(mappingEntity.getStatus());
    mappingRule.setMappingType(mappingEntity.getMappingType());
    mappingRule.setSource(mappingEntity.getSource());
    mappingRule.setDateCreated(mappingEntity.getDateCreated());
    mappingRule.setDateUpdated(mappingEntity.getDateUpdated());
    return mappingRule;
  }
}
