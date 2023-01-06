package org.cancermodels.suggestions;

public enum FieldsNames {
  ID("id"),
  SOURCE_TYPE("sourceType"),
  ONTOLOGY_NCIT_TERM("ontology.ncit_term"),
  ONTOLOGY_LABEL("ontology.label"),
  ONTOLOGY_KEY("ontology.key"),
  ONTOLOGY_DEFINITION("ontology.definition"),
  ONTOLOGY_SYNONYM("ontology.synonym"),
  ONTOLOGY_TYPE("ontology.type"),
  RULE_MAPPED_TERM_URL("rule.mappedTermUrl"),
  RULE_MAPPED_TERM_LABEL("rule.mappedTermLabel"),
  RULE_KEY("rule.key"),
  RULE_VALUE("rule.value."),
  RULE_ENTITY_TYPE_NAME("rule.entityTypeName");

  private final String name;

  FieldsNames(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
