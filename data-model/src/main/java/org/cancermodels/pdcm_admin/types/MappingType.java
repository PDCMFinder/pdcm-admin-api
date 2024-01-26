package org.cancermodels.pdcm_admin.types;

public enum MappingType {
  MANUAL("Manual"),
  AUTOMATIC_REVIEW("Automatic-Review"),
  AUTOMATIC_MAPPED("Automatic-Mapped");

  private final String label;

  MappingType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
