package org.cancermodels.pdcm_admin.types;

import lombok.Getter;

@Getter
public enum ProcessReportModules {
  INPUT_DATA("Input data"),
  ONTOLOGIES("Ontologies"),
  INDEXER("Indexer");

  private final String label;

  ProcessReportModules(String label) {
    this.label = label;
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
