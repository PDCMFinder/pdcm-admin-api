package org.cancermodels.pdcm_admin;

import lombok.Getter;

@Getter
public enum EntityTypeName {
  Treatment("Treatment"),
  Diagnosis("Diagnosis");

  private final String label;

  EntityTypeName(String label) {
    this.label = label;
  }

}
