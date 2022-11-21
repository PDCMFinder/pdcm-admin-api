package org.cancermodels.mappings.search;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingsFilterBuilder {

  private Map<FilterTypes, List<String>> filters;

  // Prevents instantiation.
  private MappingsFilterBuilder()
  {
    filters = new HashMap<>();
  }

  public static MappingsFilterBuilder getInstance()
  {
    return new MappingsFilterBuilder();
  }

  public MappingsFilter build()
  {
    MappingsFilter mappingsFilter = new MappingsFilter();
    mappingsFilter.setFilters(filters);
    return mappingsFilter;
  }

  public void setFilters(Map<FilterTypes, List<String>> filters)
  {
    this.filters = filters;
  }

  private MappingsFilterBuilder withFilter(FilterTypes filterType, List<String> values)
  {
    if (isListValid(values))
    {
      filters.put(filterType, values);
    }
    return this;
  }

  public MappingsFilterBuilder withEntityTypeNames(List<String> entityTypeNames)
  {
    return withFilter(FilterTypes.ENTITY_TYPE, entityTypeNames);
  }

  public MappingsFilterBuilder withMappingQuery(List<String> mappingQueries)
  {
    return withFilter(FilterTypes.MAPPING_QUERY, mappingQueries);
  }

  public MappingsFilterBuilder withStatus(List<String> status)
  {
    return withFilter(FilterTypes.STATUS, status);
  }

  public MappingsFilterBuilder withMappingType(List<String> mappingTypes)
  {
    return withFilter(FilterTypes.MAPPING_TYPE, mappingTypes);
  }


  private boolean isListValid(List<String> values)
  {
    return values != null && !values.isEmpty();
  }

}
