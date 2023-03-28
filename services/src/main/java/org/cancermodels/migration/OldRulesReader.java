package org.cancermodels.migration;

import java.util.HashSet;
import java.util.Set;
import org.cancermodels.mappings.EntityTypeService;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.util.ResourceReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

// Reads rules in the old format
@Service
public class OldRulesReader {

  private final EntityTypeService entityTypeService;

  public OldRulesReader(EntityTypeService entityTypeService) {
    this.entityTypeService = entityTypeService;
  }

  public Set<MappingEntity> readRules(String fileName) {

    Set<MappingEntity> mappingEntities = new HashSet<>();

    String json = ResourceReader.readFileToString("old_rules/" + fileName);
    // TODO: Check spelling
    json = json.replaceAll("TumourType", "TumorType");
    JsonRuleToEntityMapper mappingEntityMapper = new JsonRuleToEntityMapper(entityTypeService);

    try {
      JSONObject job = new JSONObject(json);
      if (job.has("mappings")) {
        JSONArray rows = job.getJSONArray("mappings");

        for (int i = 0; i < rows.length(); i++) {
          JSONObject row = rows.getJSONObject(i);
          MappingEntity mappingEntity = mappingEntityMapper.jsonObjectToMappingEntity(row);
          if (mappingEntity != null) {
            mappingEntities.add(mappingEntity);
          }
        }
      }

    } catch (JSONException e) {
      e.printStackTrace();
    }
    return mappingEntities;
  }

}
