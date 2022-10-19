package org.cancermodels.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cancermodels.EntityTypeName;
import org.cancermodels.persistance.EntityType;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingKey;
import org.cancermodels.persistance.MappingValue;

public class MappingEntityBuilder {

  private final EntityType treatmentType = createTreatmentType();
  private final EntityType diagnosisType = createDiagnosisType();

  private EntityType entityType;
  private String mappingKey;
  private List<MappingValue> mappingValues = new ArrayList<>();
  private String mappedTermUrl;

  public MappingEntity build() {
    MappingEntity mappingEntity = new MappingEntity();
    mappingEntity.setEntityType(entityType);
    mappingEntity.setMappingValues(mappingValues);
    mappingEntity.setMappedTermUrl(mappedTermUrl);
    mappingEntity.setMappingKey(mappingKey);
    return mappingEntity;
  }

  public MappingEntityBuilder setEntityType(EntityTypeName entityTypeName) {
    if (entityTypeName.equals(EntityTypeName.Treatment)) {
      entityType = treatmentType;
    } else if (entityTypeName.equals(EntityTypeName.Diagnosis)) {
      entityType = diagnosisType;
    } else {
      entityType = null;
    }
    return this;
  }

  public MappingEntityBuilder setMappingKey(String mappingKey) {
    this.mappingKey = mappingKey;
    return this;
  }

  public MappingEntityBuilder setValues(Map<String, String> values) {
    if (entityType == null) {
      throw new IllegalArgumentException("Entity type must be defined first");
    }
    for (String key : values.keySet()) {
      MappingKey mappingKey = createMappingKey(key);
      MappingValue mappingValue = new MappingValue();
      mappingValue.setMappingKey(mappingKey);
      mappingValue.setValue(values.get(key));
      mappingValues.add(mappingValue);
    }
    return this;
  }

  public MappingEntityBuilder setMappedTermUrl(String mappedTermUrl) {
    this.mappedTermUrl = mappedTermUrl;
    return this;
  }

  private MappingKey createMappingKey(String keyName) {
    MappingKey mappingKey = new MappingKey();
    mappingKey.setKey(keyName);
    mappingKey.setEntityType(entityType);
    return mappingKey;
  }

  private EntityType createTreatmentType() {
    EntityType treatmentType = new EntityType();
    treatmentType.setId(1);
    treatmentType.setName(EntityTypeName.Treatment.getLabel());
    return treatmentType;
  }

  private EntityType createDiagnosisType() {
    EntityType diagnosisType = new EntityType();
    diagnosisType.setId(2);
    diagnosisType.setName(EntityTypeName.Diagnosis.getLabel());
    return diagnosisType;
  }

  public static Map<String, String> createTreatmentValues(String dataSource, String treatmentName) {
    Map<String, String> values = new HashMap<>();
    values.put(Constants.DATA_SOURCE_KEY_NAME, dataSource);
    values.put(Constants.TREATMENT_NAME_KEY_NAME, treatmentName);
    return values;
  }

  public static void main(String[] args) {
    //
    MappingEntityBuilder mappingEntityBuilder = new MappingEntityBuilder();
    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(createTreatmentValues("TRACE", "Aspirin"))
        .build();

    System.out.println(Util.prettyPrint(mappingEntity));
    System.out.println(mappingEntity);
  }
}
