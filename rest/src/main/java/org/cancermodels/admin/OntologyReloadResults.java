package org.cancermodels.admin;

import lombok.Getter;
import lombok.Setter;

/**
 * A simple POJO class to show the results of loading the ontologies in the database
 */
@Getter
@Setter
public class OntologyReloadResults {
  private int numReloadedDiagnosis;
  private int numReloadedTreatments;
  private int numFailedUrlsDiagnosis;
  private int numFailedUrlsTreatments;

  private int totalDiagnosis;
  private int totalTreatments;
  private int totalRegimens;
}
