package org.cancermodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.digest.DigestUtils;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MappingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  /**
   * The unique String that identifies a Mapping
   */
  @JsonIgnore
  @Column(unique = true, nullable = false)
  @EqualsAndHashCode.Include
  private String mappingKey;

  @OneToOne
  @JoinColumn(name = "entity_type_id", nullable = false)
  private EntityType entityType;

  /**
   * Label of the ontology term that is associated to this entity.
   */
  private String mappedTermLabel;

  /**
   * Url of the ontology term that is associated to this entity.
   */
  private String mappedTermUrl;

  /**
   * Status. It can be for example: Created, Unmapped, automatic.
   */
  private String status;

  private LocalDateTime dateCreated;

  private LocalDateTime dateUpdated;

  /**
   * Values associated to this entity (for each key). It corresponds to the values in // the
   * providers data
   */
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "mappingEntity")
  private List<MappingValue> mappingValues;

  /**
   * Suggested mappings (other mapping entities that are similar).
   */
  @OneToMany(
      fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
  @JoinColumn(name = "mapping_entity_id", nullable = false)
  private Set<MappingEntitySuggestion> mappingEntitySuggestions= new HashSet<>();

  public Map<String, String> getValuesAsMap() {
    Map<String, String> map = new HashMap<>();
    for (MappingValue mappingValue : mappingValues) {
      map.put(mappingValue.getMappingKey().getKey(), mappingValue.getValue());
    }
    return map;
  }

  private String getValuesAsMapString() {
    StringBuilder valuesSb = new StringBuilder();
    for (MappingValue mappingValue : mappingValues) {
      valuesSb.append(mappingValue.getMappingKey().getKey()).append(": ")
          .append(mappingValue.getValue()).append(" ");
    }
    return "{" + valuesSb.toString() + "}";
  }

  @Override
  public String toString() {
    String values = getValuesAsMapString();
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

  public String buildMappingKey(){
    String key = entityType.getName();
    key += getValuesAsMapString();
    key = key.replaceAll("[^a-zA-Z0-9 _-]","");
    key = DigestUtils.sha256Hex(key);
    return key.toLowerCase();
  }

}
