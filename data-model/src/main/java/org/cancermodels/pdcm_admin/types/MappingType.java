package org.cancermodels.pdcm_admin.types;

import lombok.Getter;

@Getter
public enum MappingType {
  MANUAL("Manual"),
  AUTOMATIC_REVIEW("Automatic-Review"),
  AUTOMATIC_MAPPED("Automatic-Mapped");

  private final String label;

  MappingType(String label) {
    this.label = label;
  }

}
