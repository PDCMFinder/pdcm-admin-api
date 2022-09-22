package org.cancermodels.mapping_rules;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * A representation as object of the JSON object that forms the JSON file with mappings.
 */
@Data
public class MappingRule {
  private String mappingKey;
  private String entityType;
  private Map<String, String> mappingValues;
  private String mappedTermUrl;
  private String mappedTermLabel;
  private String status;
  private String mappingType;
  private String source;
  @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
  private LocalDateTime dateCreated;
  @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
  private LocalDateTime dateUpdated;
}
