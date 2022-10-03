package org.cancermodels.types;

public enum ProcessReportModules {
  INPUT_DATA("Input data");

  private final String label;

  ProcessReportModules(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
