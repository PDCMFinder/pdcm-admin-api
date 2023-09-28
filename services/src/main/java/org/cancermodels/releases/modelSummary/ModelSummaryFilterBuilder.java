package org.cancermodels.releases.modelSummary;

import org.cancermodels.mappings.search.FilterTypes;
import org.cancermodels.mappings.search.MappingsFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ModelSummaryFilterBuilder {

  private Map<ModelSummaryFilterTypes, List<String>> filters;

  // Prevents instantiation.
  private ModelSummaryFilterBuilder()
  {
    filters = new HashMap<>();
  }

  public static ModelSummaryFilterBuilder getInstance()
  {
    return new ModelSummaryFilterBuilder();
  }

  public ModelSummaryFilter build()
  {
    ModelSummaryFilter modelSummaryFilter = new ModelSummaryFilter();
    modelSummaryFilter.setFilters(filters);
    return modelSummaryFilter;
  }

  public void setFilters(Map<ModelSummaryFilterTypes, List<String>> filters)
  {
    this.filters = filters;
  }

  private ModelSummaryFilterBuilder withFilter(ModelSummaryFilterTypes filterType, List<String> values)
  {
    if (isListValid(values))
    {
      // Use only not null values
      List<String> notNullValues = values.stream().filter(Objects::nonNull).collect(Collectors.toList());
      if (!notNullValues.isEmpty()) {
        filters.put(filterType, values);
      }
    }
    return this;
  }

  public ModelSummaryFilterBuilder withModelType(List<String> modelTypes)
  {
    return withFilter(ModelSummaryFilterTypes.MODEL_TYPE, modelTypes);
  }

  public ModelSummaryFilterBuilder withReleaseId(List<String> releaseId)
  {
    return withFilter(ModelSummaryFilterTypes.RELEASE_ID, releaseId);
  }
  private boolean isListValid(List<String> values)
  {
    return values != null && !values.isEmpty();
  }

}
