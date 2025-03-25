package org.cancermodels.pdcm_admin.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class KeySearchConfiguration {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_seq_gen")
  @SequenceGenerator(name = "hibernate_seq_gen", sequenceName = "hibernate_sequence", allocationSize = 1)
  private Integer id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "key_id", nullable = false)
  @JsonIgnore
  private MappingKey mappingKey;

  /**
   * Numeric value, from 0 to 1, that represents how important this key is when // determining the
   * similarity between 2 entities
   */
  private Double weight;

  /**
   * Indicates whether this key is going to be used in the process of calculating
   * suggestions based on ontologies.
   */
  private Boolean searchOnOntology;

  private Boolean multiFieldQuery;

  private Boolean mainField;
}
