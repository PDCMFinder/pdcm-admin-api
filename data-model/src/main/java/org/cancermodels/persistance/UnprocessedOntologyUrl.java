package org.cancermodels.persistance;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UnprocessedOntologyUrl {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @NonNull
  @EqualsAndHashCode.Include
  private String url;

  private String note;

  private String rawTermUrl;

  @NonNull
  private String type;

  private int attempts;

  private String errorMessage;
}
