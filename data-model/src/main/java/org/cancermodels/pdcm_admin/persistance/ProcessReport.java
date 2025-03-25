package org.cancermodels.pdcm_admin.persistance;

import java.time.LocalDateTime;

import jakarta.persistence.*;
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
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_seq_gen")
  @SequenceGenerator(name = "hibernate_seq_gen", sequenceName = "hibernate_sequence", allocationSize = 1)
  private Long id;

  private String module;

  private String attribute;

  private String value;

  private LocalDateTime date;
}
