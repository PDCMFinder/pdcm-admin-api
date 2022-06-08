package org.cancermodels.ontologies;

public enum OntologyTermType {
  DIAGNOSIS("Diagnosis"),
  TREATMENT("Treatment"),
  REGIMEN("Regimen");

  private final String description;

  OntologyTermType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public static OntologyTermType getTypeByString(String value) {
    return OntologyTermType.valueOf(value.toUpperCase());
  }

}
