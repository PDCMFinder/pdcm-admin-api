package org.cancermodels.admin.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "suggestionsByOntologies")
@Data
public class OntologySuggestionDTO {
  @JsonProperty("ontologyTerm")
  private OntologyTermDTO ontologyTermDTO;
  private double score;
}
