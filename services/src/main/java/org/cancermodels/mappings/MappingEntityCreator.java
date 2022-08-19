package org.cancermodels.mappings;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cancermodels.EntityTypeName;
import org.cancermodels.persistance.EntityType;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingKey;
import org.cancermodels.persistance.MappingValue;
import org.cancermodels.types.MappingKeyName;
import org.cancermodels.types.Status;
import org.springframework.stereotype.Component;

@Component
public class MappingEntityCreator {
  private final MappingKeyService mappingKeyService;
  private final EntityTypeService entityTypeService;
  private Map<String, MappingKey> mapKeys;
  private EntityType treatmentType;
  private EntityType diagnosisType;
  private MappingKey treatmentNameMappingKey;
  private MappingKey dataSourceTMappingKey;
  private MappingKey sampleDiagnosisMappingKey;
  private MappingKey originTissueMappingKey;
  private MappingKey tumorTypeMappingKey;
  private MappingKey dataSourceDMappingKey;

  public MappingEntityCreator(MappingKeyService mappingKeyService,
      EntityTypeService entityTypeService) {
    this.mappingKeyService = mappingKeyService;
    this.entityTypeService = entityTypeService;
    initMapKeys();
    initTypes();
  }

  private void initTypes() {
    treatmentType =
        entityTypeService.getEntityTypeByName(EntityTypeName.Treatment.getLabel());
    diagnosisType =
        entityTypeService.getEntityTypeByName(EntityTypeName.Diagnosis.getLabel());

    treatmentNameMappingKey =
        getMappingKey(treatmentType, MappingKeyName.TREATMENT_NAME.getLabel());
    dataSourceTMappingKey =
        getMappingKey(treatmentType, MappingKeyName.DATASOURCE.getLabel());

    sampleDiagnosisMappingKey =
        getMappingKey(diagnosisType, MappingKeyName.SAMPLE_DIAGNOSIS.getLabel());
    originTissueMappingKey =
        getMappingKey(diagnosisType, MappingKeyName.ORIGIN_TISSUE.getLabel());

    tumorTypeMappingKey =
        getMappingKey(diagnosisType, MappingKeyName.TUMOR_TYPE.getLabel());

    dataSourceDMappingKey =
        getMappingKey(diagnosisType, MappingKeyName.DATASOURCE.getLabel());

  }

  private MappingEntity create(EntityType entityType, Map<MappingKey, String> data) {
    MappingEntity mappingEntity = new MappingEntity();
    mappingEntity.setEntityType(entityType);
    for (MappingKey mappingKey : data.keySet())
    {
      MappingValue mappingValue = new MappingValue();
      mappingValue.setMappingEntity(mappingEntity);
      mappingValue.setMappingKey(mappingKey);
      mappingValue.setValue(data.get(mappingKey));
      mappingEntity.getMappingValues().add(mappingValue);
    }
    mappingEntity.setMappingKey(mappingEntity.buildMappingKey());
    mappingEntity.setStatus(Status.UNMAPPED.getLabel());
    mappingEntity.setDateCreated(LocalDateTime.now());
    return mappingEntity;
  }

  public MappingEntity createTreatmentMappingEntity(String treatmentName, String dataSource) {
    Map<MappingKey, String> data = new HashMap<>();
    data.put(treatmentNameMappingKey, treatmentName);
    data.put(dataSourceTMappingKey, dataSource);

    return create(treatmentType, data);
  }

  public MappingEntity createDiagnosisMappingEntity(
      String sampleDiagnosis,
      String originTissue,
      String tumorType,
      String dataSource) {

    Map<MappingKey, String> data = new HashMap<>();
    data.put(sampleDiagnosisMappingKey, sampleDiagnosis);
    data.put(originTissueMappingKey, originTissue);
    data.put(tumorTypeMappingKey, tumorType);
    data.put(dataSourceDMappingKey, dataSource);

    return create(diagnosisType, data);
  }

  private String getUniqueKeyByEntityTypeAndKey(EntityType entityType, String key) {
    return entityType.getName() + "." + key;
  }

  private MappingKey getMappingKey(EntityType entityType, String key) {
    String uniqueKey = getUniqueKeyByEntityTypeAndKey(entityType, key);
    return mapKeys.get(uniqueKey);
  }

  private void initMapKeys() {
    mapKeys = new HashMap<>();
    List<MappingKey> all = mappingKeyService.getAll();
    all.forEach(x -> {
      String key = getUniqueKeyByEntityTypeAndKey(x.getEntityType(), x.getKey());
      mapKeys.put(key, x);
    });
  }
}
