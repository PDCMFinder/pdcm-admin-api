package org.cancermodels.pdcm_admin.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
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
  @GeneratedValue(strategy = GenerationType.AUTO)
  @ToString.Exclude
  private Long id;

  @NonNull
  private String name;

  @NonNull
  private LocalDateTime date;

}
