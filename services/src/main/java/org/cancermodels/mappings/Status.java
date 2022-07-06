package org.cancermodels.mappings;

public enum Status {
  UNMAPPED("unmapped"),
  MAPPED("mapped"),
  REVISION("revision");

  private final String label;

  Status(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
