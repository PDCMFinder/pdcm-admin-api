package org.cancermodels.admin.dtos;

import lombok.Data;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "suggestionsByOntologies")
@Data
public class OntologySuggestionDTO {
  private OntologyTermDTO ontologyTermDTO;
  private double score;
}
