package org.cancermodels.pdcm_admin.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_seq_gen")
  @SequenceGenerator(name = "hibernate_seq_gen", sequenceName = "hibernate_sequence", allocationSize = 1)
  private Integer id;

  private String sourceType;
  private String suggestedTermLabel;
  private String suggestedTermUrl;
  private double score;
  private double relativeScore;

  @ManyToOne(fetch= FetchType.LAZY)
  @Exclude
  @EqualsAndHashCode.Include
  @JoinColumn(name = "suggested_mapping_entity_id")
  private MappingEntity mappingEntity;

  @ManyToOne(fetch= FetchType.LAZY)
  @Exclude
  @EqualsAndHashCode.Include
  @JoinColumn(name = "suggested_ontology_term_id")
  private OntologyTerm ontologyTerm;

}
