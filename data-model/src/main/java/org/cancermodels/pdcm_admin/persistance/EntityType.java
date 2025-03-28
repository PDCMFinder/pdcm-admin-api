package org.cancermodels.pdcm_admin.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class EntityType {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_seq_gen")
  @SequenceGenerator(name = "hibernate_seq_gen", sequenceName = "hibernate_sequence", allocationSize = 1)
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
      map.put(mappingKey.getKey(), mappingKey.getKeySearchConfiguration().getWeight());
    }
    return map;
  }
}
