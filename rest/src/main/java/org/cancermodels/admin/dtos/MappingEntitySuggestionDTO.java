package org.cancermodels.admin.dtos;

import java.util.Map;
import lombok.Data;

@Data
public class MappingEntitySuggestionDTO {
  private String entityTypeName;
  private Map<String, String> values;
}
