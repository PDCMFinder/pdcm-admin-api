package org.cancermodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class EntityType {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JsonIgnore
  private Integer id;

  // Name of the entity type. Example: "Diagnosis" or "Treatment".
  private String name;

  // Name of the file that contains the mapping rules for this type".
  private String mappingRulesFileName;

  // Keys associated to this type. Example: ["DataSource", "TreatmentName"]
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "entityType")
  private List<MappingKey> mappingKeys;

  // Maps with the weights associated to each key in this type. Weights determine
  // how important an attribute or key is when looking for similarities between
  // mapping entities.
  public Map<String, Double> getWeightsAsMap() {
    Map<String, Double> map = new HashMap<>();
    for (MappingKey mappingKey : mappingKeys) {
      map.put(mappingKey.getKey(), mappingKey.getWeight());
    }
    return map;
  }
}
