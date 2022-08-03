package org.cancermodels.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
   * Status of the mapping entity: [Mapped, Unmapped, Revision].
   */
  private String status;

  /**
   * Indicates how the mapping was done: [Manual, Automatic].
   */
  private String mappingType;

  /**
   * Indicates what was used to do the mapping: [Rule, NCIt]. Can be also "Legacy"
   * for migrated data for which we don't have the information.
   */
  private String source;

  private LocalDateTime dateCreated;

  private LocalDateTime dateUpdated;

  /**
   * Values associated to this entity (for each key). It corresponds to the values in // the
   * providers data
   */
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "mappingEntity")
  private List<MappingValue> mappingValues;

  /**
   * Suggested mappings by rules or ontologies.
   */
  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.ALL},
      orphanRemoval = true)
  @JoinColumn(name = "mapping_entity_id", nullable = false)
  private List<Suggestion> suggestions = new ArrayList<>();

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
    String entityTypeName = entityType == null ? "" : entityType.getName();
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
