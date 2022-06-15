package org.cancermodels.mappings.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class MappingsFilter {

  private Map<FilterTypes, List<String>> filters;
  private final static String LABEL_VALUE_SEPARATOR = ":";

  MappingsFilter()
  {
    filters = new HashMap<>();
  }

  public static MappingsFilter getInstance()
  {
    return new MappingsFilter();
  }

  public Map<String, List<String>> getNotNullFilterNames()
  {
    return filters.entrySet().stream()
        .filter(x -> x.getValue() != null)
        .collect(Collectors.toMap(x -> x.getKey().getName(), Entry::getValue));
  }

  public List<String> getEntityType()
  {
    return filters.getOrDefault(FilterTypes.ENTITY_TYPE, null);
  }

  public List<String> getStatus()
  {
    return filters.getOrDefault(FilterTypes.STATUS, null);
  }

  public Map<String, List<String>> getMappingQuery() {
    Map<String, List<String>> mappingQuery = new HashMap<>();

    List<String> plainMappingQuery = filters.getOrDefault(FilterTypes.MAPPING_QUERY, null);

    if (plainMappingQuery != null) {
      plainMappingQuery.forEach(x -> {
        String[] label_value = x.split(LABEL_VALUE_SEPARATOR);
        String label = label_value[0];
        String value = label_value[1];
        if (!mappingQuery.containsKey(label)) {
          mappingQuery.put(label, new ArrayList<>());
        }
        List<String> values = mappingQuery.get(label);
        values.add(value);
      });
    }
    return mappingQuery;
  }

}
