package org.cancermodels.pdcm_admin.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString.Exclude;

@Entity
@Table(uniqueConstraints=
@UniqueConstraint(columnNames = {"mapping_entity_id", "key_id"}))
@Data
public class MappingValue {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @ManyToOne
  @Exclude
  @JsonIgnore
  @JoinColumn(name = "mapping_entity_id", nullable = false)
  private MappingEntity mappingEntity;

  @ManyToOne
  @JoinColumn(name = "key_id", nullable = false)
  private MappingKey mappingKey;

  // Value for a particular mapping key in a mapping entity
  private String value;

  public String toString() {
    String keyName = mappingKey == null ? "null" : mappingKey.getKey();
    return "id: " + id + " key: " + keyName + " value: " + value;
  }

}
