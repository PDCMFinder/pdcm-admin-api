package org.cancermodels.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString.Exclude;
import org.cancermodels.persistance.MappingEntity;

/**
 * Represents a mapping suggestion. It can come from a rule or an ontology.
 */
@Entity
@Data
public class Suggestion {
  private String sourceType;
  private String suggestedTermLabel;
  private String suggestedTermUrl;
  private double score;
  private double relativeScore;
  @OneToOne(cascade = {CascadeType.ALL},
      orphanRemoval = true)
  private RuleSuggestion ruleSuggestion;
  @OneToOne(cascade = {CascadeType.ALL},
      orphanRemoval = true)
  private OntologySuggestion ontologySuggestion;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @OneToOne
  @Exclude
  @JsonIgnore
  @EqualsAndHashCode.Include
  @JoinColumn(name = "suggested_mapping_entity_id")
  private MappingEntity suggestedMappingEntity;

  @Data
  @Entity
  public static class RuleSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private int mappingEntityId;
    @ElementCollection
    private Map<String, String> data;
  }

  @Data
  @Entity
  public static class OntologySuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String definition;
    @ElementCollection
    private Set<String> synonyms;
  }
}




