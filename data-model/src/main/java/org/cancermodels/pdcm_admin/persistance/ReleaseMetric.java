package org.cancermodels.pdcm_admin.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

/**
 * Key-Value to represent counts per release
 */
@Entity
@Data
@NoArgsConstructor
public class ReleaseMetric {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_seq_gen")
  @SequenceGenerator(name = "hibernate_seq_gen", sequenceName = "hibernate_sequence", allocationSize = 1)
  @JsonIgnore
  private Integer id;

  @ManyToOne
  @JsonIgnore
  @JoinColumn(name = "release_id", nullable = false)
  private Release release;

  private String key;

  private Long value;
}
