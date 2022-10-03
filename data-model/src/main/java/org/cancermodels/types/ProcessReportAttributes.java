package org.cancermodels.types;

public enum ProcessReportAttributes {
  UPDATED("Updated"),
  LAST_VERSION("last version");

  private final String label;

  ProcessReportAttributes(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
