package org.cancermodels;

public enum EntityTypeName {
  Treatment("Treatment"),
  Diagnosis("Diagnosis");

  private final String label;

  EntityTypeName(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
