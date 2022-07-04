package org.cancermodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.ToString.Exclude;

@Entity
@Data
public class MappingEntitySuggestion {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @ManyToOne
  @Exclude
  @JsonIgnore
  @JoinColumn(name = "mapping_entity_id")
  private MappingEntity mappingEntity;

  @ManyToOne
  @Exclude
  @JsonIgnore
  @JoinColumn(name = "suggested_mapping_entity_id")
  private MappingEntity suggestedMappingEntity;

  private double score;

}
