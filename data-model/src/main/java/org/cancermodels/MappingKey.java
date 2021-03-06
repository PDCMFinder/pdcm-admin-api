package org.cancermodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class MappingKey {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JsonIgnore
  private Integer id;

  @ManyToOne
  @JsonIgnore
  @JoinColumn(name = "entity_type_id", nullable = false)
  private EntityType entityType;

  /**
   * Name of the key. A key is an attribute that is relevant in the process of mapping // data
   * provided by the user to an ontology term.
   */
  private String key;

  /**
   * Numeric value, from 0 to 1, that represents how important this key is when // determining the
   * similarity between 2 entities
   */
  @JsonIgnore private Double weight;

  /**
   * Indicates whether or not this key is going to be used in the process of calculating
   * suggestions based on ontologies.
   */
  private Boolean toUseInOntologySuggestionCalculation;
}
