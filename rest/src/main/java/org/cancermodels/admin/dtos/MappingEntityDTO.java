package org.cancermodels.admin.dtos;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import org.cancermodels.persistance.MappingValue;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "mappings")
@Data
public class MappingEntityDTO {
  private int id;
  private String entityTypeName;
  private List<MappingValueDTO> mappingValues;
  private String mappedTermUrl;
  private String mappedTermLabel;
  private String status;
  private String mappingType;
  private String source;
  private LocalDateTime dateCreated;
  private LocalDateTime dateUpdated;
}

