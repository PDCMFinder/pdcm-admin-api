package org.cancermodels.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString.Exclude;

/**
 * Represents a mapping suggestion. It can come from a rule or an ontology.
 */
@Entity
@Data
public class Suggestion {

  @JsonIgnore
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  private String sourceType;
  private String suggestedTermLabel;
  private String suggestedTermUrl;
  private double score;
  private double relativeScore;

  @OneToOne
  @Exclude
  @EqualsAndHashCode.Include
  @JoinColumn(name = "suggested_mapping_entity_id")
  private MappingEntity mappingEntity;

  @OneToOne
  @Exclude
  @EqualsAndHashCode.Include
  @JoinColumn(name = "suggested_ontology_term_id")
  private OntologyTerm ontologyTerm;

}
