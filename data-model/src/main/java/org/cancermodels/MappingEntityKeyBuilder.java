package org.cancermodels;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.cancermodels.persistance.MappingEntity;

/**
 * A class that creates the key for a mapping entity.
 */
public class MappingEntityKeyBuilder {

  private static final String SEPARATOR = "|";

  public static String buildKeyDiagnosisMapping(
      String sampleDiagnosis, String tumorType, String originTissue, String dataSource) {
    List<String> elementsAsList =
        Arrays.asList(
            EntityTypeName.Diagnosis.getLabel(), sampleDiagnosis, tumorType, originTissue, dataSource);

    return generateHashForValues(elementsAsList);
  }

  public static String buildKeyTreatmentMapping(String treatmentName, String dataSource) {
    List<String> elementsAsList =
        Arrays.asList(
            EntityTypeName.Treatment.getLabel(), treatmentName, dataSource);
    return generateHashForValues(elementsAsList);
  }

  public static String buildKey(MappingEntity mappingEntity) {
    String key = "";
    if (mappingEntity.getEntityType() != null) {
      String entityTypeName = mappingEntity.getEntityType().getName();
      Map<String, String> valuesAsMap = mappingEntity.getValuesAsMap();

      if (entityTypeName.equalsIgnoreCase(EntityTypeName.Diagnosis.getLabel())) {
        key = buildKeyDiagnosisMapping(
            valuesAsMap.get("SampleDiagnosis"),
            valuesAsMap.get("TumourType"),
            valuesAsMap.get("OriginTissue"),
            valuesAsMap.get("DataSource"));
      }
      else if (entityTypeName.equalsIgnoreCase(EntityTypeName.Treatment.getLabel())) {
        key = buildKeyTreatmentMapping(
            valuesAsMap.get("TreatmentName"),
            valuesAsMap.get("DataSource"));
      }
    }
    return key;
  }

  public static String generateHashForValues(List<String> values) {
    return convertToHash(concatenateValues(values));
  }

  private static String concatenateValues(List<String> values) {
    return values.stream()
        .map(x -> x == null ? "" : x)
        .map(String::toLowerCase)
        .collect(Collectors.joining(SEPARATOR));
  }

  private static String convertToHash(String text) {
    return DigestUtils.sha256Hex(text);
  }

}
