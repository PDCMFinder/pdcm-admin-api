package org.cancermodels;

import org.cancermodels.migration.MappingsLoader;
import org.springframework.stereotype.Component;


@Component
public class DummyService {
  private final EntityTypeRepository entityTypeByProjectRepository;
  private final MappingKeyRepository mappingKeyRepository;
  private final MappingEntityRepository mappingEntityRepository;
  private final MappingValueRepository mappingValueRepository;

  private final MappingsLoader mappingsLoader;

  public DummyService(
      EntityTypeRepository entityTypeByProjectRepository,
      MappingKeyRepository mappingKeyRepository,
      MappingEntityRepository mappingEntityRepository,
      MappingValueRepository mappingValueRepository,
      MappingsLoader mappingsLoader) {
    this.entityTypeByProjectRepository = entityTypeByProjectRepository;
    this.mappingKeyRepository = mappingKeyRepository;
    this.mappingEntityRepository = mappingEntityRepository;
    this.mappingValueRepository = mappingValueRepository;
    this.mappingsLoader = mappingsLoader;
  }


  public void setup() {

    EntityType treatmentType = new EntityType();
    treatmentType.setName("treatment");
    entityTypeByProjectRepository.save(treatmentType);

    EntityType diagnosisType = new EntityType();
    diagnosisType.setName("diagnosis");
    entityTypeByProjectRepository.save(diagnosisType);

    MappingKey mappingKey1 = new MappingKey();
    mappingKey1.setEntityType(treatmentType);
    mappingKey1.setKey("DataSource");

    MappingKey mappingKey2 = new MappingKey();
    mappingKey2.setEntityType(treatmentType);
    mappingKey2.setKey("TreatmentName");

    //
    MappingKey mappingKey3 = new MappingKey();
    mappingKey3.setEntityType(diagnosisType);
    mappingKey3.setKey("DataSource");

    MappingKey mappingKey4 = new MappingKey();
    mappingKey4.setEntityType(diagnosisType);
    mappingKey4.setKey("SampleDiagnosis");

    MappingKey mappingKey5 = new MappingKey();
    mappingKey5.setEntityType(diagnosisType);
    mappingKey5.setKey("OriginTissue");

    MappingKey mappingKey6 = new MappingKey();
    mappingKey6.setEntityType(diagnosisType);
    mappingKey6.setKey("TumorType");

    mappingKeyRepository.save(mappingKey1);
    mappingKeyRepository.save(mappingKey2);
    mappingKeyRepository.save(mappingKey3);
    mappingKeyRepository.save(mappingKey4);
    mappingKeyRepository.save(mappingKey5);
    mappingKeyRepository.save(mappingKey6);
  }

  public void testLoad() {
    System.out.println("call mappingsLoader.test()");
    mappingsLoader.testDiagnosis();
  }

}
