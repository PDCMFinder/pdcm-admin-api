package org.cancermodels.suggestions.search_engine.query_builder;

import lombok.Data;

@Data
public class SearchInputEntry {
  private String fieldName;
  private String text;
  private double weight;

  public String toString() {
    return "fieldName=" + fieldName +", text="+ text + ", weight=" + weight;
  }
}
