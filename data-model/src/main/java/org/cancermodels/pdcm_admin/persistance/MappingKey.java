package org.cancermodels.pdcm_admin.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

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

  @JsonIgnore
  @OneToOne(mappedBy = "mappingKey", cascade = CascadeType.ALL,
      fetch = FetchType.LAZY, optional = false)
  private KeySearchConfiguration keySearchConfiguration;
}
