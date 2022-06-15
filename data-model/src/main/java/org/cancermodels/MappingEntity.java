package org.cancermodels;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class MappingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "entity_type_id", nullable = false)
  private EntityType entityType;

  // Label of the ontology term that is associated to this entity.
  private String mappedTermLabel;

  // Url of the ontology term that is associated to this entity.
  private String mappedTermUrl;

  // Status. It can be for example: Created, Unmapped, automatic
  private String status;

  private LocalDateTime dateCreated;

  private LocalDateTime dateUpdated;

  // Values associated to this entity (for each key). It corresponds to the values in
  // the providers data
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "mappingEntity")
  private List<MappingValue> mappingValues;

  public Map<String, String> getValuesAsMap() {
    Map<String, String> map = new HashMap<>();
    for (MappingValue mappingValue : mappingValues) {
      map.put(mappingValue.getMappingKey().getKey(), mappingValue.getValue());
    }
    return map;
  }

  @Override
  public String toString() {
    StringBuilder valuesSb = new StringBuilder();
    for (MappingValue mappingValue : mappingValues) {
      valuesSb.append(mappingValue.getMappingKey().getKey()).append(": ")
          .append(mappingValue.getValue()).append(" ");
    }
    String values = "{" + valuesSb.toString() + "}";
    String entityTypeName = entityType.getName();
    StringBuilder sb = new StringBuilder();
    sb.append("id:").append(id);
    sb.append(" type:").append(entityTypeName);
    sb.append(" values:").append(values);
    sb.append(" mappedTermLabel:").append(mappedTermLabel);
    sb.append(" mappedTermUrl:").append(mappedTermUrl);
    sb.append(" status:").append(status);
    return sb.toString();
  }

}
