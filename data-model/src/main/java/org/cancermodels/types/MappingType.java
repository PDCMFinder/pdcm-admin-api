package org.cancermodels.types;

public enum MappingType {
  MANUAL("Manual"),
  AUTOMATIC("Automatic");

  private final String label;

  MappingType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
