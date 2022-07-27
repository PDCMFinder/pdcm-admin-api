package org.cancermodels;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class KeySearchConfiguration {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "key_id", nullable = false)
  private MappingKey mappingKey;

  /**
   * Numeric value, from 0 to 1, that represents how important this key is when // determining the
   * similarity between 2 entities
   */
  private Double weight;

  /**
   * Indicates whether or not this key is going to be used in the process of calculating
   * suggestions based on ontologies.
   */
  private Boolean searchOnOntology;

  private Boolean multiFieldQuery;

  private Boolean mainField;
}
