package org.cancermodels.suggestions.search_engine.query_builder;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SearchInput {

  private SearchParameters commonParameters;

  private List<SearchInputEntry> fieldsToInclude = new ArrayList<>();
  private List<SearchInputEntry> fieldsToExclude = new ArrayList<>();

  @Override
  public String toString() {
    String result = commonParameters.toString() + "\n";
    result += "fieldsToInclude: [\n";
    for (SearchInputEntry searchInputEntry : fieldsToInclude) {
      result += searchInputEntry.toString() + "\n";
    }
    result += "]\n";
    result += "fieldsToExclude: [\n";
    for (SearchInputEntry searchInputEntry : fieldsToExclude) {
      result += searchInputEntry.toString() + "\n";
    }
    result += "]\n";
    return result;
    }
}
