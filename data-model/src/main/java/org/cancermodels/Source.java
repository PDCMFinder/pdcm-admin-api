package org.cancermodels;

public enum Source {
  RULE("Rule"),
  ONTOLOGY("Ontology"),
  OTHER("Other");

  private final String label;

  Source(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
