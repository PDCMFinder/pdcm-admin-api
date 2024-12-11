package org.cancermodels.pdcm_etl;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name ="release_info", schema = "pdcm_api")
public class ReleaseInfo {
  @Id
  private String name;
  private LocalDateTime date;

}

