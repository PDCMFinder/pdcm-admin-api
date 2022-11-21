package org.cancermodels.suggestions.search_engine.query_builder;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Representation of the input for a search. This allows to generalise
 * the building of a lucene query.
 */
@Data
public class SearchInput {

  private SearchParameters commonParameters;
  // Indicates if the fieldsToInclude section should be wrapped as a
  // DisjunctionMaxQuery, meaning that we don't want the sum of every
  // individual query but the highest score.
  private boolean isDisjunctionMaxQuery;

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
