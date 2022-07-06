package org.cancermodels.admin.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "mappings")
@Data
public class MappingEntityDTO {
  private int id;
  private String entityTypeName;
  private Map<String, String> mappingValues;
  private String mappedTermUrl;
  private String mappedTermLabel;
  private String status;

  @JsonProperty("suggestionsByMappingEntities")
  private List<MappingEntitySuggestionDTO> mappingEntitySuggestionDTOS;

  @JsonProperty("suggestionsByOntologies")
  private List<OntologySuggestionDTO> ontologySuggestionDTOS;

  private LocalDateTime dateCreated;
  private LocalDateTime dateUpdated;
}
