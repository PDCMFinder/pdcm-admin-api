package org.cancermodels.admin.dtos;

import lombok.Data;

@Data
public class MappingValueDTO {
  private MappingKeyDTO mappingKey;
  private String value;

  @Data
  public static class MappingKeyDTO {
    private String key;
  }
}
