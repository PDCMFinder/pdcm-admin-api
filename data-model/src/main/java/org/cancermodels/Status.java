package org.cancermodels;

public enum Status {
  UNMAPPED("Unmapped"),
  MAPPED("Mapped"),
  REVISE("Revise"),
  REQUEST("Request");

  private final String label;

  Status(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
