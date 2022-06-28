package org.cancermodels.migration;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cancermodels.EntityType;
import org.cancermodels.mappings.EntityTypeService;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingKey;
import org.cancermodels.MappingValue;
import org.cancermodels.util.JSONHelper;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class JsonRuleToEntityMapper {

  private final EntityTypeService entityTypeService;

  public JsonRuleToEntityMapper(EntityTypeService entityTypeService) {
    this.entityTypeService = entityTypeService;
  }

  public MappingEntity jsonObjectToMappingEntity(JSONObject jsonObject) {

    MappingEntity mappingEntity = new MappingEntity();
    EntityType entityType = getEntityType(jsonObject.getString("entityType"));
    mappingEntity.setEntityType(entityType);

    JSONObject mappingValuesJSONObject = jsonObject.getJSONObject("mappingValues");
    Map<String, String> mappingValuesMap = JSONHelper.JSONObjectToStringMap(mappingValuesJSONObject);

    // Adding customised fix wrong provider name
    if (mappingValuesMap.containsKey("DataSource") && mappingValuesMap.get("DataSource").equalsIgnoreCase("dfci cpdm") ) {
      mappingValuesMap.put("DataSource", "dfci-cpdm");
    }

     List<MappingValue> mappingValues =
         getMappingValuesFromMap(mappingValuesMap, mappingEntity, entityType);
    mappingEntity.setMappingValues(mappingValues);

    if (!jsonObject.isNull("mappedTermLabel")) {
      mappingEntity.setMappedTermLabel(jsonObject.getString("mappedTermLabel"));
    }

    String originalStatus = jsonObject.getString("status");

    // Making status simpler: mapped or unmapped (from the existing data, might introduce others later)
    String newStatus = originalStatus;
    if ("validated".equalsIgnoreCase(originalStatus)) {
      newStatus = "mapped";
    }
    else if("created".equalsIgnoreCase(originalStatus)) {
      newStatus = "mapped";
    }

    mappingEntity.setStatus(newStatus);

    if (!jsonObject.isNull("dateCreated")) {
      Timestamp dataCreatedTimeStamp = new Timestamp(jsonObject.getLong("dateCreated"));
      mappingEntity.setDateCreated(dataCreatedTimeStamp.toLocalDateTime());
    }

    if (!jsonObject.isNull("dateUpdated")) {
      Timestamp dataUpdatedTimeStamp = new Timestamp(jsonObject.getLong("dateUpdated"));
      mappingEntity.setDateUpdated(dataUpdatedTimeStamp.toLocalDateTime());
    }

    return mappingEntity;
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

  private EntityType getEntityType(String name) {
    return entityTypeService.getEntityTypeByName(name);
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
}
