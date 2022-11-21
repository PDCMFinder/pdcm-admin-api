package org.cancermodels.suggestions.search_engine.query_builder;

import java.util.ArrayList;
import java.util.List;
import org.cancermodels.suggestions.FieldsNames;
import org.springframework.stereotype.Component;

/**
 * Builds a {@link SearchInput} object suitable to search ontologies
 */
@Component
public class OntologySearchInputBuilder {

  public SearchInput build(
      String simpleInput, String composedInput, SearchParameters commonParameters) {
    SearchInput searchInput = new SearchInput();

    searchInput.setCommonParameters(commonParameters);
    List<SearchInputEntry> simpleInputEntries = buildEntries(simpleInput, commonParameters);

    List<SearchInputEntry> include = new ArrayList<>(simpleInputEntries);

    if (composedInput != null) {
      List<SearchInputEntry> composedInputEntries = buildEntries(composedInput, commonParameters);
      include.addAll(composedInputEntries);
    }

    searchInput.setFieldsToInclude(include);
    // In ontology query, we want to know what scores more: label, synonym
    // or definition. The sum here is not useful because it rests flexibility
    // when trying to assign more relevance to a specific field.
    searchInput.setDisjunctionMaxQuery(true);

    return searchInput;
  }

  private List<SearchInputEntry> buildEntries(String text, SearchParameters commonParameters) {
    List<SearchInputEntry> searchInputEntries = new ArrayList<>();
    searchInputEntries.add(buildLabelEntry(text, commonParameters));
    searchInputEntries.add(buildDefinitionEntry(text, commonParameters));
    searchInputEntries.add(buildSynonymEntry(text, commonParameters));
    return searchInputEntries;
  }

  private SearchInputEntry buildLabelEntry(String text, SearchParameters commonParameters) {
    SearchInputEntry searchInputEntry = new SearchInputEntry();
    searchInputEntry.setFieldName(FieldsNames.ONTOLOGY_LABEL.getName());
    searchInputEntry.setText(text);
    searchInputEntry.setWeight(commonParameters.getOntologyLabelWeight());
    return searchInputEntry;
  }

  private SearchInputEntry buildDefinitionEntry(String text, SearchParameters commonParameters) {
    SearchInputEntry searchInputEntry = new SearchInputEntry();
    searchInputEntry.setFieldName(FieldsNames.ONTOLOGY_DEFINITION.getName());
    searchInputEntry.setText(text);
    searchInputEntry.setWeight(commonParameters.getOntologyDefinitionWeight());
    return searchInputEntry;
  }

  private SearchInputEntry buildSynonymEntry(String text, SearchParameters commonParameters) {
    SearchInputEntry searchInputEntry = new SearchInputEntry();
    searchInputEntry.setFieldName(FieldsNames.ONTOLOGY_SYNONYM.getName());
    searchInputEntry.setText(text);
    searchInputEntry.setWeight(commonParameters.getOntologySynonymWeight());
    return searchInputEntry;
  }

}
