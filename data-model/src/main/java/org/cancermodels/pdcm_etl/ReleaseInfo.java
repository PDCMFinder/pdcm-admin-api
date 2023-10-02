package org.cancermodels.pdcm_etl;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name ="release_info", schema = "pdcm_api")
public class ReleaseInfo {
  @Id
  private String name;
  private LocalDateTime date;

}

