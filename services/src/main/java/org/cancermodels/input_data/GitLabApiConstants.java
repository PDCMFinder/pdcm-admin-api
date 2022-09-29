package org.cancermodels.input_data;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class GitLabApiConstants {
  // URL of the gitLab server managed by EBI (where pdxfinder-data repository is).
  static final String GITLAB_URL = "https://gitlab.ebi.ac.uk";

  // Id given by gitLab to the pdxfinder-data repository. This is needed to reference it when
  // using the API
  static final long PROJECT_ID = 1629;

}
