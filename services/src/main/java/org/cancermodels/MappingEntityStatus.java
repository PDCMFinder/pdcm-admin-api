package org.cancermodels;

public enum MappingEntityStatus {
  MAPPED("mapped"),
  UNMAPPED("unmapped");

  private final String description;

  MappingEntityStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
