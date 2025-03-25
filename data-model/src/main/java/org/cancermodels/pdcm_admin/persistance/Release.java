package org.cancermodels.pdcm_admin.persistance;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * This class represents a table that contains all the names of the releases of the etl and the date when
 * executed.
 */
@Entity
@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class Release {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_seq_gen")
  @SequenceGenerator(name = "hibernate_seq_gen", sequenceName = "hibernate_sequence", allocationSize = 1)
  @ToString.Exclude
  private Long id;

  @NonNull
  private String name;

  @NonNull
  private LocalDateTime date;

}
