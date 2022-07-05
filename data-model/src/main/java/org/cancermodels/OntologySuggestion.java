package org.cancermodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
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
public class OntologySuggestion {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @OneToOne
  @Exclude
  @EqualsAndHashCode.Include
  @JoinColumn(name = "ontology_term_id")
  private OntologyTerm ontologyTerm;

  @Column(columnDefinition="NUMBER(5,2)")
  private double score;

}
