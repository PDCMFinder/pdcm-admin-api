package org.cancermodels.admin.dtos;

import java.util.Map;
import lombok.Data;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "suggestedMappings")
@Data
public class MappingEntitySuggestionDTO {
  private Map<String, String> mappingValues;
  private String mappedTermLabel;
  private String mappedTermUrl;
  private double score;
}
