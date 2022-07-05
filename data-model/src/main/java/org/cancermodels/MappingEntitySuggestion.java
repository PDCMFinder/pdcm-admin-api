package org.cancermodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString.Exclude;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MappingEntitySuggestion {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @OneToOne
  @Exclude
  @JsonIgnore
  @EqualsAndHashCode.Include
  @JoinColumn(name = "suggested_mapping_entity_id")
  private MappingEntity suggestedMappingEntity;

  private double score;

}
