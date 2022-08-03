package org.cancermodels.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
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
  @Lob
  private String value;

  public String toString() {
    String keyName = mappingKey == null ? "null" : mappingKey.getKey();
    return "id: " + id + " key: " + keyName + " value: " + value;
  }

}
