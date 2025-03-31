package org.cancermodels.pdcm_admin.types;

import lombok.Getter;

@Getter
public enum Source {
  RULE("Rule"),
  ONTOLOGY("Ontology"),
  LEGACY("Legacy");

  private final String label;

  Source(String label) {
    this.label = label;
  }

}
