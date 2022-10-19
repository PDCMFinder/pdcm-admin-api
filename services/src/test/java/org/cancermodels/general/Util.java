package org.cancermodels.general;

import java.util.Map;
import org.cancermodels.persistance.MappingEntity;

public class Util {
  public static String prettyPrint(MappingEntity mappingEntity) {
    Map<String, String> values = mappingEntity.getValuesAsMap();
    String entityTypeName =
        mappingEntity.getEntityType() == null ? "" : mappingEntity.getEntityType() .getName();
    String result = "id: " + mappingEntity.getId() + "\n";
    result += "key: " + mappingEntity.getMappingKey() + "\n";
    result += "Type: " + entityTypeName + "\n";
    result += "values: " + "\n";
    result += "{" + "\n";
    for (String key : values.keySet()) {
      result += "\t" + key + ": " + values.get(key) +"\n";
    }
    result += "}" + "\n";
    return result;
  }
}
