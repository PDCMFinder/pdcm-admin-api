package org.cancermodels.pdcm_admin.persistance;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * A class to log results of processes in PDCM Admin (Update of input data, Reloading Ontologies,
 * etc).
 */
@Entity
@Getter
@Setter
public class ProcessReport {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String module;

  private String attribute;

  private String value;

  private LocalDateTime date;
}
