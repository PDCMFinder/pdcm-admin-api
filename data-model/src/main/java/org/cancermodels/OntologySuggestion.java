package org.cancermodels;

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
public class OntologySuggestion implements Suggestion<OntologySuggestion> {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @OneToOne
  @Exclude
  @EqualsAndHashCode.Include
  @JoinColumn(name = "ontology_term_id")
  private OntologyTerm ontologyTerm;

  /**
   * Value from 0 to 100 representing how good the suggestion is.
   */
  private int score;

  @Override
  public OntologySuggestion getSuggestion() {
    return this;
  }

  @Override
  public Source getSource() {
    return Source.ONTOLOGY;
  }

  @Override
  public String getTermUrl() {
    return ontologyTerm.getUrl();
  }

  @Override
  public String getTermLabel() {
    return ontologyTerm.getLabel();
  }
}
