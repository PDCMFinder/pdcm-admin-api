package org.cancermodels.admin.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Data;

@Data
public class OntologyTermDTO {
  private Long id;
  private String url;
  private String label;
  private String type;
  private List<String> synonyms;
  @JsonIgnore
  private String description;
}
