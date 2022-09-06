package org.cancermodels.general;

import java.util.Arrays;
import org.cancermodels.persistance.EntityType;
import org.cancermodels.persistance.KeySearchConfiguration;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingKey;
import org.cancermodels.persistance.MappingValue;

public class MappingEntityCreator {

  public static double WEIGHT_1 = 0.0;
  public static double WEIGHT_2 = 1.0;
  public static double WEIGHT_3 = 0.5;
  public static double WEIGHT_4 = 0.1;

  public static MappingEntity createMappingEntityTestInstance() {

    MappingEntity mappingEntity = new MappingEntity();
    mappingEntity.setId(1);
    mappingEntity.setMappedTermUrl("http://purl.obolibrary.org/obo/NCIT_C4627");
    mappingEntity.setMappedTermLabel("Central Nervous System Cancer");

    EntityType entityType = new EntityType();
    entityType.setId(1);
    entityType.setName("Diagnosis");

    MappingKey mappingKey1 = new MappingKey();
    mappingKey1.setEntityType(entityType);
    mappingKey1.setKey("DataSource");
    KeySearchConfiguration conf1 = new KeySearchConfiguration();
    conf1.setId(1);
    conf1.setWeight(WEIGHT_1);
    conf1.setSearchOnOntology(false);
    conf1.setMainField(false);
    conf1.setMultiFieldQuery(false);
    mappingKey1.setKeySearchConfiguration(conf1);

    MappingKey mappingKey2 = new MappingKey();
    mappingKey2.setEntityType(entityType);
    mappingKey2.setKey("SampleDiagnosis");
    KeySearchConfiguration conf2 = new KeySearchConfiguration();
    conf2.setId(2);
    conf2.setWeight(WEIGHT_2);
    conf2.setSearchOnOntology(true);
    conf2.setMainField(true);
    conf2.setMultiFieldQuery(true);
    mappingKey2.setKeySearchConfiguration(conf2);

    MappingKey mappingKey3 = new MappingKey();
    mappingKey3.setEntityType(entityType);
    mappingKey3.setKey("OriginTissue");
    KeySearchConfiguration conf3 = new KeySearchConfiguration();
    conf3.setId(3);
    conf3.setWeight(WEIGHT_3);
    conf3.setSearchOnOntology(true);
    conf3.setMainField(false);
    conf3.setMultiFieldQuery(true);
    mappingKey3.setKeySearchConfiguration(conf3);

    MappingKey mappingKey4 = new MappingKey();
    mappingKey4.setEntityType(entityType);
    mappingKey4.setKey("TumorType");
    KeySearchConfiguration conf4 = new KeySearchConfiguration();
    conf4.setId(4);
    conf4.setWeight(WEIGHT_4);
    conf4.setSearchOnOntology(false);
    conf4.setMainField(false);
    conf4.setMultiFieldQuery(false);
    mappingKey4.setKeySearchConfiguration(conf4);

    entityType.setMappingKeys(Arrays.asList(mappingKey1, mappingKey2, mappingKey3, mappingKey4));

    MappingValue mappingValue1 = new MappingValue();
    mappingValue1.setId(1);
    mappingValue1.setMappingKey(mappingKey1);
    mappingValue1.setMappingEntity(mappingEntity);
    mappingValue1.setValue("crl");

    MappingValue mappingValue2 = new MappingValue();
    mappingValue2.setId(2);
    mappingValue2.setMappingKey(mappingKey2);
    mappingValue2.setMappingEntity(mappingEntity);
    mappingValue2.setValue("central nervous system cancer");

    MappingValue mappingValue3 = new MappingValue();
    mappingValue3.setId(3);
    mappingValue3.setMappingKey(mappingKey3);
    mappingValue3.setMappingEntity(mappingEntity);
    mappingValue3.setValue("central nervous system");

    MappingValue mappingValue4 = new MappingValue();
    mappingValue4.setId(1);
    mappingValue4.setMappingKey(mappingKey4);
    mappingValue4.setMappingEntity(mappingEntity);
    mappingValue4.setValue("primary");


    mappingEntity.setMappingValues(
        Arrays.asList(mappingValue1, mappingValue2, mappingValue3, mappingValue4));

    return mappingEntity;
  }
}
