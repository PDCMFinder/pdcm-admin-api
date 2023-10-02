package org.cancermodels.releases.modelSummary;

import lombok.Data;
import org.cancermodels.mappings.search.FilterTypes;
import org.cancermodels.mappings.search.MappingsFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ModelSummaryFilter {
    private Map<ModelSummaryFilterTypes, List<String>> filters;
    private final static String LABEL_VALUE_SEPARATOR = ":";

    ModelSummaryFilter() {
        filters = new HashMap<>();
    }

    public static ModelSummaryFilter getInstance()
    {
        return new ModelSummaryFilter();
    }

    public List<String> getModelTypes()
    {
        return filters.getOrDefault(ModelSummaryFilterTypes.MODEL_TYPE, null);
    }

    public List<String> getReleaseIds()
    {
        return filters.getOrDefault(ModelSummaryFilterTypes.RELEASE_ID, null);
    }
}
