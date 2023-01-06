package org.cancermodels.suggestions.search_engine.query_builder;

import java.util.ArrayList;
import java.util.List;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingValue;
import org.cancermodels.suggestions.FieldsNames;
import org.cancermodels.suggestions.search_engine.MappingValueConfHelper;
import org.springframework.stereotype.Component;

/**
 * Builds a {@link SearchInput} object suitable to search ontologies
 */
@Component
public class MappingEntitySearchInputBuilder {

  private final MappingValueConfHelper mappingValueConfHelper;

  public MappingEntitySearchInputBuilder(
      MappingValueConfHelper mappingValueConfHelper) {
    this.mappingValueConfHelper = mappingValueConfHelper;
  }

  public SearchInput build(MappingEntity mappingEntity, SearchParameters searchParameters) {
    SearchInput searchInput = new SearchInput();
    List<MappingValue> mappingValues = mappingEntity.getMappingValues();
    List<MappingValue> toProcess = mappingValueConfHelper.getValuesWeightGreaterZero(mappingValues);
    MappingValue mainValue = mappingValueConfHelper.getMainValue(mappingValues);
    List<MappingValue> secondaryValues = mappingValueConfHelper.getSecondaryValues(mappingValues);

    searchInput.setCommonParameters(searchParameters);
    List<SearchInputEntry> simpleInputEntries = buildEntriesForMappingValues(toProcess, searchParameters);

    List<SearchInputEntry> include = new ArrayList<>(simpleInputEntries);
    List<SearchInputEntry> exclude = new ArrayList<>();
    List<SearchInputEntry> filters = new ArrayList<>();

    // Add also a query with the combine text of the main value and secondary values
    // (to simulate cases like a search with origin tissue + sample diagnosis)
    String composedText = mappingValueConfHelper.getTextForMultiFieldQuery(mainValue, secondaryValues);

    if (composedText != null) {
      SearchInputEntry entryForComposedText =
          buildEntriesForMappingValueWithCustomText(mainValue, composedText, searchParameters);
      include.add(entryForComposedText);
    }

    searchInput.setFieldsToInclude(include);

    exclude.add(buildExcludeEntry(mappingEntity));
    searchInput.setFieldsToExclude(exclude);
    filters.add(buildFilterEntry(mappingEntity));
    searchInput.setFilterFields(filters);

    return searchInput;
  }

  private SearchInputEntry buildExcludeEntry(MappingEntity mappingEntity) {
    SearchInputEntry searchInputEntry = new SearchInputEntry();
    searchInputEntry.setFieldName(FieldsNames.ID.getName());
    searchInputEntry.setText(mappingEntity.getMappingKey());
    return searchInputEntry;
  }

  private SearchInputEntry buildFilterEntry(MappingEntity mappingEntity) {
    SearchInputEntry searchInputEntry = new SearchInputEntry();
    searchInputEntry.setFieldName(FieldsNames.RULE_ENTITY_TYPE_NAME.getName());
    searchInputEntry.setText(mappingEntity.getEntityType().getName());
    return searchInputEntry;
  }

  private List<SearchInputEntry> buildEntriesForMappingValues(
      List<MappingValue> toProcess, SearchParameters commonParameters) {
    List<SearchInputEntry> searchInputEntries = new ArrayList<>();
    for (MappingValue mappingValue : toProcess) {
      SearchInputEntry searchInputEntry = buildEntriesForMappingValue(mappingValue, commonParameters);
      searchInputEntries.add(searchInputEntry);
    }
    return searchInputEntries;
  }


  private SearchInputEntry buildEntriesForMappingValue(
      MappingValue mappingValue, SearchParameters commonParameters) {
    var conf = mappingValue.getMappingKey().getKeySearchConfiguration();
    double weight = conf.getWeight() * commonParameters.getRuleMultiplier();
    return buildEntry(getFieldNameByValue(mappingValue), mappingValue.getValue(), weight);
  }

  private SearchInputEntry buildEntriesForMappingValueWithCustomText(
      MappingValue mappingValue, String customText, SearchParameters commonParameters) {
    var conf = mappingValue.getMappingKey().getKeySearchConfiguration();
    double weight = conf.getWeight() * commonParameters.getRuleMultiplier();
    return buildEntry(getFieldNameByValue(mappingValue), customText, weight);
  }

  private SearchInputEntry buildEntry(String fieldName, String value, double weight) {
    SearchInputEntry searchInputEntry = new SearchInputEntry();
    searchInputEntry.setFieldName(fieldName);
    searchInputEntry.setText(value);
    searchInputEntry.setWeight(weight);
    return searchInputEntry;
  }


  private String getFieldNameByValue(MappingValue mappingValue) {
    String key = mappingValue.getMappingKey().getKey();
    // Make first letter lowercase to be consistent with the names of other fields
    key = Character.toLowerCase(key.charAt(0)) + key.substring(1);
    return FieldsNames.RULE_VALUE.getName() + key;
  }

}
