package org.cancermodels.pdcm_admin.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * A reduced representation of a model as stored in the search_index table.
 */
@Entity
@Data
@NoArgsConstructor
public class ModelSummary {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JsonIgnore
  private Integer id;

  @ManyToOne
  @JsonIgnore
  @JoinColumn(name = "release_id", nullable = false)
  private Release release;

  private String externalModelId;

  private String modelType;

  private String dataSource;

}
