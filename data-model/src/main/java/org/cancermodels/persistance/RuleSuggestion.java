package org.cancermodels.persistance;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class RuleSuggestion {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @EqualsAndHashCode.Include
  private Integer id;

  @EqualsAndHashCode.Include
  private String key;

  private String entityTypeName;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "ruleSuggestion")
  private Set<RuleSuggestionData> mappingValues = new HashSet<>();

}
