package org.cancermodels.types;

public enum ProcessReportModules {
  INPUT_DATA("Input data"),
  ONTOLOGIES("Ontologies"),
  INDEXER("Indexer");

  private final String label;

  ProcessReportModules(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public static ProcessReportModules getByName(String name) {
    for (ProcessReportModules element : ProcessReportModules.values()) {
      if (element.getLabel().equalsIgnoreCase(name)) {
        return element;
      }
    }
    throw new IllegalArgumentException("Module " + name + " does not exist");
  }

}
