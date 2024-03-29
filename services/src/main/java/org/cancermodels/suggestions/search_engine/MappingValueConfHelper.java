package org.cancermodels.suggestions.search_engine;

import java.util.List;
import java.util.stream.Collectors;
import org.cancermodels.pdcm_admin.persistance.KeySearchConfiguration;
import org.cancermodels.pdcm_admin.persistance.MappingValue;
import org.cancermodels.suggestions.exceptions.InvalidKeySearchConfiguration;
import org.springframework.stereotype.Component;

@Component
public class MappingValueConfHelper {

  public List<MappingValue> getValuesWeightGreaterZero(List<MappingValue> mappingValues) {
    return mappingValues.stream()
        .filter(x -> getConf(x).getWeight() > 0).collect(
        Collectors.toList());
  }

  public List<MappingValue> getSearchOnOntologyValues(List<MappingValue> mappingValues) {
    return mappingValues.stream()
        .filter(x -> getConf(x).getSearchOnOntology()).collect(
            Collectors.toList());
  }

  public MappingValue getMainValue(List<MappingValue> mappingValues) {
    return mappingValues.stream()
        .filter(x -> getConf(x).getMainField())
        .findFirst().orElse(null);
  }

  public List<MappingValue> getSecondaryValues(List<MappingValue> mappingValues) {
    List<MappingValue> secondaryValues = mappingValues.stream()
        .filter(x -> getConf(x).getMultiFieldQuery()).collect(
        Collectors.toList());
    MappingValue mappingValue = getMainValue(mappingValues);
    secondaryValues.remove(mappingValue);
    return secondaryValues;
  }

  private KeySearchConfiguration getConf(MappingValue mappingValue) {
    KeySearchConfiguration conf = mappingValue.getMappingKey().getKeySearchConfiguration();
    if (conf == null) {
      throw new InvalidKeySearchConfiguration(
          "Configuration for value [" + mappingValue + "] is null.");
    }
    return conf;
  }

  public String getTextForMultiFieldQuery(MappingValue mainValue, List<MappingValue> extraValues) {
    String queryText = null;
    if (mainValue != null && !extraValues.isEmpty()) {
      String prefix = extraValues.stream()
          .map(MappingValue::getValue).collect(Collectors.joining(" "));
      queryText = prefix + " " + mainValue.getValue();
    }
    return queryText;
  }
}
