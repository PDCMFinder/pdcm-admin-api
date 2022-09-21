package org.cancermodels.mapping_rules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.cancermodels.EntityTypeName;
import org.cancermodels.mappings.EntityTypeService;
import org.cancermodels.mappings.MappingEntityService;
import org.cancermodels.persistance.EntityType;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingKey;
import org.cancermodels.persistance.MappingValue;
import org.cancermodels.types.Status;
import org.cancermodels.util.FileManager;
import org.cancermodels.util.JSONHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Provides logic to handle the JSON mapping rules
 */
@Service
public class MappingRulesService {

  @Value("${data-dir}")
  private String rootDir;

  private final MappingEntityService mappingEntityService;
  private final EntityTypeService entityTypeService;

  public MappingRulesService(MappingEntityService mappingEntityService,
      EntityTypeService entityTypeService) {
    this.mappingEntityService = mappingEntityService;
    this.entityTypeService = entityTypeService;
  }

  /**
   * Convert the mapped entities in the db into a JSON file
   * @param entityTypeName Treatment or Diagnosis
   * @return A string with a json array where each object is the json representation of a
   * mapping entity.
   * @throws JsonProcessingException
   */
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

  private MappingEntity mappingRuleToMappingEntity(MappingRule mappingRule) {
    MappingEntity mappingEntity = new MappingEntity();
    mappingEntity.setMappingKey(mappingRule.getMappingKey());
    EntityType entityType = getEntityType(mappingRule.getEntityType());
    mappingEntity.setEntityType(entityType);
    mappingEntity.setMappingValues(
        getMappingValuesFromMap(mappingRule.getMappingValues(), mappingEntity, entityType));
    mappingEntity.setMappedTermLabel(mappingRule.getMappedTermLabel());
    mappingEntity.setMappedTermUrl(mappingRule.getMappedTermUrl());
    mappingEntity.setStatus(mappingRule.getStatus());
    mappingEntity.setMappingType(mappingRule.getMappingType());
    mappingEntity.setSource(mappingRule.getSource());
    mappingEntity.setDateCreated(mappingRule.getDateCreated());
    mappingEntity.setDateUpdated(mappingRule.getDateUpdated());
    return mappingEntity;
  }

  /**
   * Deletes all the mapping entities and reload the data from the json files with the mapping rules.
   * Because the json files contain only Mapped data, any mappings in other status
   * (Revise, Unmapped, Request) will be lost.
   */
  public void restoreMappedMappingEntitiesFromJsons() throws IOException {
    mappingEntityService.deleteAll();
    for (EntityType entityType : entityTypeService.getAll()) {
      List<MappingRule> mappingRules = readRulesFromJsonByType(entityType);
      List<MappingEntity> mappingEntities = mappingRules.stream().map(
          this::mappingRuleToMappingEntity).collect(
          Collectors.toList());
      mappingEntityService.savAll(mappingEntities);
    }

  }

  private List<MappingValue> getMappingValuesFromMap(
      Map<String, String> map, MappingEntity mappingEntity, EntityType entityType) {
    List<MappingValue> mappingValues = new ArrayList<>();
    for (String key : map.keySet()) {
      MappingValue mappingValue = new MappingValue();
      mappingValue.setMappingEntity(mappingEntity);
      mappingValue.setMappingKey(getMappingKeyByName(key, entityType));
      mappingValue.setValue(map.get(key));
      mappingValues.add(mappingValue);
    }
    return mappingValues;
  }

  private MappingKey getMappingKeyByName(String name, EntityType entityType) {
    List<MappingKey> mappingKeys = entityType.getMappingKeys();
    for (MappingKey mappingKey : mappingKeys) {
      if (mappingKey.getKey().equalsIgnoreCase(name)) {
        return mappingKey;
      }
    }
    return null;
  }

  private EntityType getEntityType(String name) {
    return entityTypeService.getEntityTypeByName(name);
  }

  private List<MappingRule> readRulesFromJsonByType(EntityType entityType) throws IOException {
    String mappingJsonFilePath = rootDir + "/mapping/" + entityType.getMappingRulesFileName();
    String json = FileManager.getStringFromFile(mappingJsonFilePath);
    return JSONHelper.fromJson(json, new TypeReference<>() {
    });
  }

}
