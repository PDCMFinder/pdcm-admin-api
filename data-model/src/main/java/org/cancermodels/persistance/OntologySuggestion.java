package org.cancermodels.persistance;

import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity
public class OntologySuggestion {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;
  private String definition;
  @ElementCollection
  private Set<String> synonyms;
}
