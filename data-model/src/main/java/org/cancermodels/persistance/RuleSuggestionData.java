package org.cancermodels.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.ToString;

/**
 * It was needed a separated class (instead of a ElementCollection) to be able to
 * make the value a clob.
 */
@Data
@Entity
public class RuleSuggestionData {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  private String key;
  @Lob
  private String value;

  @JsonIgnore
  @ManyToOne
  @ToString.Exclude
  @JoinColumn(name = "rule_suggestion_id", nullable = false)
  private RuleSuggestion ruleSuggestion;
}
