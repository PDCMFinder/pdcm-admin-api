package org.cancermodels.mappings.search;

public enum FilterTypes {
  MAPPING_QUERY("mapping_query"),
  ENTITY_TYPE("entity_type"),
  STATUS("status"),
  MAPPING_TYPE("mappingType");

  private final String name;

  FilterTypes(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
