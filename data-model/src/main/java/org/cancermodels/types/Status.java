package org.cancermodels.types;

public enum Status {
  UNMAPPED("Unmapped"),
  MAPPED("Mapped"),
  REVISE("Revise"),
  REQUEST("Request");

  private final String label;

  Status(String label) {
    this.label = label;
  }

  public static Status getStatusByName(String name) {
    for (Status status : Status.values()) {
      if (status.getLabel().equalsIgnoreCase(name)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Status " + name + " does not exist");
  }

  public String getLabel() {
    return label;
  }
}
