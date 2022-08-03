package org.cancermodels;

public enum Status {
  UNMAPPED("Unmapped"),
  MAPPED("Mapped"),
  REVISION("Revision");

  private final String label;

  Status(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
