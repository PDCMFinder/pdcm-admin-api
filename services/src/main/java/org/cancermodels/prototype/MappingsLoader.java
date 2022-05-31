package org.cancermodels.prototype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.cancermodels.EntityType;
import org.cancermodels.EntityTypeService;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingKey;
import org.cancermodels.MappingValue;
import org.cancermodels.mappers.JsonRuleToEntityMapper;
import org.cancermodels.util.FileManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class MappingsLoader {

  private final EntityTypeService entityTypeService;

  private SimilarityComparator similarityComparator = new JaroWinklerDistanceSimilarityComparator();
  private TermsWeightedSimilarityCalculator termsWeightedSimilarityCalculator =
      new TermsWeightedSimilarityCalculator(similarityComparator);

  public MappingsLoader(EntityTypeService entityTypeService) {
    this.entityTypeService = entityTypeService;
  }


  public void testTreatment() {

    EntityType entityTypeByProject = new EntityType();
    entityTypeByProject.setName("Treatment");

    MappingKey mappingKey1 = new MappingKey();
    mappingKey1.setEntityType(entityTypeByProject);
    mappingKey1.setKey("DataSource");
    mappingKey1.setWeight(0.0);

    MappingKey mappingKey2 = new MappingKey();
    mappingKey2.setEntityType(entityTypeByProject);
    mappingKey2.setKey("TreatmentName");
    mappingKey2.setWeight(1.0);

    List<MappingKey> mappingKeys = new ArrayList<>();
    mappingKeys.add(mappingKey1);
    mappingKeys.add(mappingKey2);

    entityTypeByProject.setMappingKeys(mappingKeys);

    List<MappingEntity> mappingEntities =
        loadTreatmentMappings("/Users/mmartinez/repos/pdx/pdxfinder-data/mapping/treatment_mappings.json");
    System.out.println("Loaded "+ mappingEntities.size() + " records");

    MappingEntity pivote = createPivoteTreatment();

    calculateScore(pivote, mappingEntities);
  }

  public void testDiagnosis() {
//    Project project = new Project();
//    project.setName("pdcm");
//
//    EntityType entityTypeByProject = new EntityType();
//    entityTypeByProject.setProject(project);
//    entityTypeByProject.setName("Treatment");
//
//    MappingKey mappingKey1 = new MappingKey();
//    mappingKey1.setEntityType(entityTypeByProject);
//    mappingKey1.setKey("DataSource");
//    mappingKey1.setWeight(0.0);
//
//    MappingKey mappingKey2 = new MappingKey();
//    mappingKey2.setEntityType(entityTypeByProject);
//    mappingKey2.setKey("TreatmentName");
//    mappingKey2.setWeight(1.0);
//
//    List<MappingKey> mappingKeys = new ArrayList<>();
//    mappingKeys.add(mappingKey1);
//    mappingKeys.add(mappingKey2);
//
//    entityTypeByProject.setMappingKeys(mappingKeys);

    List<MappingEntity> mappingEntities =
        loadTreatmentMappings("/Users/mmartinez/repos/pdx/pdxfinder-data/mapping/diagnosis_mappings.json");
    System.out.println("Loaded "+ mappingEntities.size() + " records");

    MappingEntity pivote = createPivoteDiagnosis();

    calculateScore(pivote, mappingEntities);
  }

  private void calculateScore(MappingEntity pivote, List<MappingEntity> mappingEntities) {
    System.out.println("Pivote "+ pivote);
    Map<String, String> leftValues = pivote.getValuesAsMap();
    Map<String, Double> weights = pivote.getEntityType().getWeightsAsMap();

    SortedMap<String, Double> scoreMap = new TreeMap<>();

    for (MappingEntity mappingEntity : mappingEntities) {
      Map<String, String> rightValues = mappingEntity.getValuesAsMap();
      double score = termsWeightedSimilarityCalculator.calculateTermsWeightedSimilarity(
          leftValues, rightValues, weights);
      System.out.println("------");
      System.out.println("entity");
      System.out.println(mappingEntity.getValuesAsMap().toString());
      System.out.println("score:" + score);
      String mapValue = mappingEntity.getValuesAsMap() + " label: " + mappingEntity.getMappedTermLabel();
      scoreMap.put(mapValue, score);
      System.out.println("------");
    }

    Stream<Entry<String,Double>> sorted =
        scoreMap.entrySet().stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

    sorted.forEach(x -> {
      System.out.println(x);
    });

//    System.out.println("scoreMap");
//    for (String key : sorted.) {
//      System.out.println(key + ":" + scoreMap.get(key));
//    }

  }

  private MappingEntity createPivoteTreatment() {
    EntityType entityType = new EntityType();

    MappingEntity mappingEntity = new MappingEntity();

    MappingKey dataSourceKey = new MappingKey();
    dataSourceKey.setKey("DataSource");
    dataSourceKey.setWeight(0.00);
    MappingKey treatmentNameKey = new MappingKey();
    treatmentNameKey.setKey("TreatmentName");
    treatmentNameKey.setWeight(1.0);

    MappingValue mappingValue1 = new MappingValue();
    mappingValue1.setValue("pptc");
    mappingValue1.setMappingKey(dataSourceKey);
    MappingValue mappingValue2 = new MappingValue();
    mappingValue2.setValue("doxorubicin hcl liposome injection");
    mappingValue2.setMappingKey(treatmentNameKey);

    List<MappingKey> mappingKeys = new ArrayList<>();
    mappingKeys.add(dataSourceKey);
    mappingKeys.add(treatmentNameKey);

    entityType.setMappingKeys(mappingKeys);
    mappingEntity.setEntityType(entityType);

    mappingEntity.setMappingValues(Arrays.asList(mappingValue1, mappingValue2));
    return mappingEntity;
  }

  private MappingEntity createPivoteDiagnosis() {
    EntityType entityType = new EntityType();
    entityType.setName("diagnosis");

    MappingEntity mappingEntity = new MappingEntity();

    MappingKey dataSourceKey = new MappingKey();
    dataSourceKey.setKey("DataSource");
    dataSourceKey.setWeight(0.00);
    MappingKey sampleDiagnosisKey = new MappingKey();
    sampleDiagnosisKey.setKey("SampleDiagnosis");
    sampleDiagnosisKey.setWeight(0.8);
    MappingKey originTissueKey = new MappingKey();
    originTissueKey.setKey("OriginTissue");
    originTissueKey.setWeight(0.15);
    MappingKey tumorTypeKey = new MappingKey();
    tumorTypeKey.setKey("TumorType");
    tumorTypeKey.setWeight(0.05);


    MappingValue mappingValue1 = new MappingValue();
    mappingValue1.setValue("pptc");
    mappingValue1.setMappingKey(dataSourceKey);

    MappingValue mappingValue2 = new MappingValue();
    mappingValue2.setValue("rearranged b acute lymphoblastic leukemia with t(v;11q23.3); kmt2a ");
    mappingValue2.setMappingKey(sampleDiagnosisKey);

    MappingValue mappingValue3 = new MappingValue();
    mappingValue3.setValue("peripheral blood");
    mappingValue3.setMappingKey(originTissueKey);

    MappingValue mappingValue4 = new MappingValue();
    mappingValue4.setValue("not specified");
    mappingValue4.setMappingKey(tumorTypeKey);

    List<MappingKey> mappingKeys = Arrays.asList(
        dataSourceKey, sampleDiagnosisKey, originTissueKey, tumorTypeKey);

    entityType.setMappingKeys(mappingKeys);
    mappingEntity.setEntityType(entityType);

    mappingEntity.setMappingValues(Arrays.asList(mappingValue1, mappingValue2, mappingValue3, mappingValue4));
    return mappingEntity;
  }


  private List<MappingEntity> loadTreatmentMappings(String file) {
    String json = FileManager.getStringFromFile(file);

    JsonRuleToEntityMapper mappingEntityMapper = new JsonRuleToEntityMapper(entityTypeService);

    List<MappingEntity> mappingEntities = new ArrayList<>();


    try {
      JSONObject job = new JSONObject(json);
      if (job.has("mappings")) {
        JSONArray rows = job.getJSONArray("mappings");
        System.out.println("ok rows");

        for (int i = 0; i < rows.length(); i++) {
          JSONObject row = rows.getJSONObject(i);
          System.out.println("***ini**");
          MappingEntity mappingEntity = mappingEntityMapper.jsonObjectToMappingEntity(row);
          System.out.println(mappingEntity);
          System.out.println("**end***\n");
          mappingEntities.add(mappingEntity);

//          String dataSource = mappingVal.getString("DataSource");
//          String treatmentName = mappingVal.getString("TreatmentName").toLowerCase();
//          String ontologyTerm = row.getString("mappedTermLabel");
//          String mapType = row.optString("mapType").toLowerCase();
//          String justification = row.optString("justification").toLowerCase();
//          String mappedTermUrl = row.getString("mappedTermUrl");
//          Long entityId = row.getLong("entityId");
//          String status = row.optString("status").toLowerCase();
//
//          System.out.println(dataSource + "|" + treatmentName
//          + "|" + ontologyTerm + "|" + mappedTermUrl);
//
//          if (ontologyTerm.equals("") || ontologyTerm == null) {
//            continue;
//          }
//
//          //DO not ask, I know it looks horrible...
//          if (justification == null || justification.equals("null")) {
//            justification = "";
//          }
//
//          //make everything lowercase
//          if (dataSource != null) {
//            dataSource = dataSource.toLowerCase();
//          }
//
//          Map<String, String> mappingValues = new HashMap<>();
//          mappingValues.put("DataSource", dataSource);
//          mappingValues.put("TreatmentName", treatmentName);
//
//          TreatmentRecord treatmentRecord = new TreatmentRecord();
//          treatmentRecord.setDataSource(dataSource);
//          treatmentRecord.setTreatmentName(treatmentName);
//          treatmentRecord.setMappedTermLabel(ontologyTerm);
//          treatmentRecords.add(treatmentRecord);
        }


      }

    } catch (JSONException e) {
      e.printStackTrace();
    }

    return mappingEntities;

  }

//  public static void main(String[] args) {
//    MappingsLoader mappingsLoader = new MappingsLoader(projectService, entityTypeService);
//    mappingsLoader.test();
//    System.out.println("ju");
//  }
}
