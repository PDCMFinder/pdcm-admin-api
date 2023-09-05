package org.cancermodels.reader;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.cancermodels.EntityTypeName;
import org.cancermodels.MappingEntityKeyBuilder;
import org.cancermodels.mappings.MappingEntityCreator;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingEntityRepository;
import org.cancermodels.types.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

/**
 * When new data from the providers is obtained, there are potentially several new terms
 * (diagnosis or treatments) that don't have a mapping term. This class identifies and
 * stores them in the database.
 */
@Slf4j
@Component
public class MissingMappingsService {

  @Value("${data-dir}")
  private String rootDir;

  private final DataReader dataReader;

  private final MappingEntityRepository mappingEntityRepository;
  private final MappingEntityCreator mappingEntityCreator;

  // This is to keep the keys of the entities in the system (that are NOT unmapped).
  // The value in the map is not likely to be used, so we will just store the id of
  // the entity to save some space.
  private final Map<String, Integer> existingMappingKeys = new HashMap<>();

  // New mappings that need to be saved at the end of the process.
  private Set<MappingEntity> newMappingEntities;

  public MissingMappingsService(DataReader dataReader,
      MappingEntityRepository mappingEntityRepository,
      MappingEntityCreator mappingEntityCreator) {
    this.dataReader = dataReader;
    this.mappingEntityRepository = mappingEntityRepository;
    this.mappingEntityCreator = mappingEntityCreator;
  }

  private void loadExistingMappingKeys() {
    mappingEntityRepository.findAll()
        .forEach(x -> existingMappingKeys.put(x.getMappingKey(), x.getId()));
  }

  /**
   * Reads treatment and diagnosis data and detects terms that are unmapped, creating the
   * corresponding mapping entities (unmapped) so the curator can map them later.
   * @return a map with the counts of the new detected terms.
   */
  @Transactional
  public Map<String, Integer> detectNewUnmappedTerms() {
    // We need to delete Unmapped terms first so we don't end up with orphan values.
    mappingEntityRepository.deleteAllByStatus(Status.UNMAPPED.getLabel());

    newMappingEntities = new HashSet<>();

    // Load the keys of the mapping entities to use it later as a way to check if a read
    // record from tsv already exists.
    loadExistingMappingKeys();

    generateMissingMappings();

    log.info("Read {} new mappings", newMappingEntities.size());

    mappingEntityRepository.saveAll(newMappingEntities);

    return getCountsByType(newMappingEntities);

  }

  private Map<String, Integer> getCountsByType(Set<MappingEntity> mappingEntities) {
    Map<String, Integer> countsByType = new HashMap<>();
    countsByType.put(EntityTypeName.Diagnosis.getLabel().toLowerCase(), 0);
    countsByType.put(EntityTypeName.Treatment.getLabel().toLowerCase(), 0);
    mappingEntities.forEach(
        x -> {
          String key = x.getEntityType().getName().toLowerCase();
          countsByType.put(key, countsByType.get(key) + 1);
        });
    return countsByType;
  }

  private void generateMissingMappings() {

    List<Path> folders = getProviderDirs();
    for (Path path : folders) {
      generateDiagnosisEntities(path);
      generateTreatmentEntities(path);
    }
  }

  private void generateDiagnosisEntities(Path path) {
    log.info("\nSearching diagnosis for " + path.toString());
    String dataSource = path.getFileName().toString();
    log.info("DataSource: " + dataSource);
    PathMatcher metadataFile = FileSystems.getDefault().getPathMatcher("glob:**{metadata-patient_sample}.tsv");
    Map<String, Table> metaDataTemplate = dataReader.getTableByFile(path, metadataFile);
    readDiagnosisAttributesFromTemplate(metaDataTemplate, dataSource);
  }

  private void generateTreatmentEntities(Path path) {
    log.info("\nSearching treatments for " + path.toString());
    String dataSource = path.getFileName().toString();
    log.info("DataSource: " + dataSource);
    PathMatcher drugDataFile = FileSystems.getDefault().getPathMatcher("glob:**{drug,treatment}*.tsv");
    Map<String, Table> drugDataTemplate = dataReader.getTableByFile(path, drugDataFile);
    readTreatmentAttributesFromTemplate(drugDataTemplate, dataSource);
  }

  private void readDiagnosisAttributesFromTemplate(Map<String, Table> tables, String dataSource) {
    try {
      Table sampleTable = tables.get("metadata-patient_sample.tsv");

      for (Row row : sampleTable) {

        // Attributes are expected to be lowercase
        String primarySiteName = row.getString("primary_site").toLowerCase();
        String diagnosis = row.getString("diagnosis").toLowerCase();
        String tumorTypeName = row.getString("tumour_type").toLowerCase();

        // Convert `Not Collected`, 'Not Provided' to `Unknown` as for the mapping process both terms mean
        // there is no data.
        if (primarySiteName.equals("not collected") || primarySiteName.equals("not provided")) {
          primarySiteName = "unknown";
        }
        if (tumorTypeName.equals("not collected") || tumorTypeName.equals("not provided")) {
          tumorTypeName = "unknown";
        }

        String key = MappingEntityKeyBuilder.buildKeyDiagnosisMapping(
            diagnosis, tumorTypeName, primarySiteName, dataSource);

        // Only create the mapping if it doesn't already exist
        if (!existingMappingKeys.containsKey(key)) {
          MappingEntity mappingEntity = mappingEntityCreator.createDiagnosisMappingEntity(
              diagnosis, primarySiteName, tumorTypeName, dataSource);
          newMappingEntities.add(mappingEntity);
        }
      }
    }
    catch (Exception e) {
      var error_message = String.format("Exception while getting diagnosis data from provider: %s", dataSource);
      log.error(error_message);
      log.error("details: " + e.getMessage());
    }
  }

  private void readTreatmentAttributesFromTemplate(Map<String, Table> tables, String abbrev){

    Table drugTable = tables.get("drugdosing-Sheet1.tsv");
    Table treatmentTable = tables.get("patienttreatment-Sheet1.tsv");
    getTreatmentAttributesFromTemplate(drugTable, abbrev);
    getTreatmentAttributesFromTemplate(treatmentTable, abbrev);
  }

  private void getTreatmentAttributesFromTemplate(Table table, String dataSource){

    try {
      if (table == null) {
        return;
      }
      for (Row row : table) {

        String treatmentName = row.getString("treatment_name");
        String[] drugArray = treatmentName.split("\\+");

        for(String drug : drugArray) {

          String drugValue = drug.toLowerCase();
          if (drugValue.equals("not collected") || drugValue.equals("not provided")) {
            drugValue = "unknown";
          }

          String key = MappingEntityKeyBuilder.buildKeyTreatmentMapping(drugValue, dataSource);

          // Only create the mapping if it doesn't already exist
          if (!existingMappingKeys.containsKey(key)) {
            MappingEntity mappingEntity =
                mappingEntityCreator.createTreatmentMappingEntity(drugValue, dataSource);
            newMappingEntities.add(mappingEntity);
          }
        }
      }
    }
    catch (Exception e){
      log.error("Exception while getting treatment data from provider");
      log.error("details: " + e.getMessage());
    }
  }

  private List<Path> getProviderDirs() {

    Path updogDirectory = Paths.get(rootDir, "/data/UPDOG");

    List<Path> subfolders;
    try (var files = Files.walk(updogDirectory, 1)) {
      subfolders = files
          .filter(p -> Files.isDirectory(p) && !p.equals(updogDirectory))
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Error opening updog dir");
      return new ArrayList<>();
    }
    return subfolders;
  }

}
