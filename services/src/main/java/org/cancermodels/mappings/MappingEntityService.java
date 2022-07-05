package org.cancermodels.mappings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.cancermodels.EntityType;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntityRepository;
import org.cancermodels.MappingEntityStatus;
import org.cancermodels.MappingEntitySuggestion;
import org.cancermodels.MappingEntitySuggestionRepository;
import org.cancermodels.OntologySuggestion;
import org.cancermodels.OntologySuggestionRepository;
import org.cancermodels.OntologyTerm;
import org.cancermodels.mappings.MappingSummaryByTypeAndProvider.SummaryEntry;
import org.cancermodels.mappings.search.MappingsFilter;
import org.cancermodels.mappings.search.MappingsSpecs;
import org.cancermodels.mappings.suggestions.MappingEntitiesSuggestionManager;
import org.cancermodels.mappings.suggestions.OntologySuggestionManager;
import org.cancermodels.mappings.suggestions.SuggestionManager;
import org.cancermodels.ontologies.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class MappingEntityService {

  private final MappingEntityRepository mappingEntityRepository;
  private final MappingEntitiesSuggestionManager mappingEntitiesSuggestionManager;
  private final EntityTypeService entityTypeService;
  private final MappingEntitySuggestionRepository mappingEntitySuggestionRepository;
  private final OntologyService ontologyService;
  private final OntologySuggestionManager ontologySuggestionManager;
  private final OntologySuggestionRepository ontologySuggestionRepository;

  private final SuggestionManager suggestionManager;

  private static final Logger LOG = LoggerFactory.getLogger(MappingEntityService.class);

  public MappingEntityService(MappingEntityRepository mappingEntityRepository,
      MappingEntitiesSuggestionManager mappingEntitiesSuggestionManager,
      EntityTypeService entityTypeService,
      MappingEntitySuggestionRepository mappingEntitySuggestionRepository,
      OntologyService ontologyService,
      OntologySuggestionManager ontologySuggestionManager,
      OntologySuggestionRepository ontologySuggestionRepository,
      SuggestionManager suggestionManager) {
    this.mappingEntityRepository = mappingEntityRepository;
    this.mappingEntitiesSuggestionManager = mappingEntitiesSuggestionManager;
    this.entityTypeService = entityTypeService;
    this.mappingEntitySuggestionRepository = mappingEntitySuggestionRepository;
    this.ontologyService = ontologyService;
    this.ontologySuggestionManager = ontologySuggestionManager;
    this.ontologySuggestionRepository = ontologySuggestionRepository;
    this.suggestionManager = suggestionManager;
  }

  public Page<MappingEntity> findPaginatedAndFiltered(
      Pageable pageable, MappingsFilter mappingsFilter) {

    Specification<MappingEntity> specs = buildSpecifications(mappingsFilter);
    return mappingEntityRepository.findAll(specs, pageable);
  }

  private Specification<MappingEntity> buildSpecifications(MappingsFilter mappingsFilter)
  {
    Specification<MappingEntity> specifications =
        Specification.where(
            MappingsSpecs.withStatus(mappingsFilter.getStatus())
                .and(MappingsSpecs.withMappingQuery(mappingsFilter.getMappingQuery())
                .and(MappingsSpecs.withEntityTypeNames(mappingsFilter.getEntityTypeNames()))
            ));
    return specifications;
  }

  public MappingSummaryByTypeAndProvider getSummaryByTypeAndProvider(String entityTypeName) {
    MappingSummaryByTypeAndProvider mappingSummaryByTypeAndProvider = new MappingSummaryByTypeAndProvider();
    List<SummaryEntry> summaryEntries = new ArrayList<>();
    mappingSummaryByTypeAndProvider.setEntityTypeName(entityTypeName);

    Map<String, Map<String, Integer>> data = new HashMap<>();

    String mappedKey = MappingEntityStatus.MAPPED.getDescription();
    String unmappedKey = MappingEntityStatus.UNMAPPED.getDescription();

    List<Object[]> list = mappingEntityRepository.countEntityTypeStatusByProvider(entityTypeName);
    for (Object[] row : list) {
      String dataSource = row[0].toString();
      String status  = row[1].toString();
      int count = Integer.parseInt(row[2].toString());
      if (!data.containsKey(dataSource)) {
        data.put(dataSource, new HashMap<>());
      }
      data.get(dataSource).put(status, count);
    }

    for (String dataSource : data.keySet()) {
      SummaryEntry summaryEntry = new SummaryEntry();
      summaryEntry.setDataSource(dataSource);
      Map<String, Integer> countByDataSource = data.get(dataSource);

      if (countByDataSource.containsKey(mappedKey)) {
        summaryEntry.setMapped(countByDataSource.get(mappedKey));
      }
      if (countByDataSource.containsKey(unmappedKey)) {
        summaryEntry.setUnmapped(countByDataSource.get(unmappedKey));
      }
      int totalTerms = summaryEntry.getMapped() + summaryEntry.getUnmapped();
      summaryEntry.setTotalTerms(totalTerms);
      summaryEntry.setProgress(summaryEntry.getMapped()*1.0 / totalTerms );
      summaryEntries.add(summaryEntry);
    }

    mappingSummaryByTypeAndProvider.setSummaryEntries(summaryEntries);
    return mappingSummaryByTypeAndProvider;
  }

  public List<MappingEntity> getAllByTypeName(String entityTypeName) {
    return mappingEntityRepository.findAllByEntityTypeNameIgnoreCase(entityTypeName);
  }

  public void calculateSuggestedMappings() {
    // Set suggestions for treatment rules
    List<MappingEntity> allTreatmentMappings = getAllByTypeName("treatment");
    mappingEntitiesSuggestionManager.updateSuggestedMappingsByExistingRules(allTreatmentMappings);
    mappingEntityRepository.saveAll(allTreatmentMappings);
  }

  public Optional<MappingEntity> findById(int id) {
    return mappingEntityRepository.findById(id);
  }

  private void calculateOntologySuggestions() {

  }

  /**
   * Sets the suggestions by rules and by ontologies for all the mapping entities in the system
   */
  public void setMappingSuggestions() {
//    LOG.info("Calculating suggestions");
    Map<String, List<MappingEntity>> mappingEntitiesMappedByType = getMappingEntitiesMappedByType();
//
//    setSuggestionsByMappingEntities(mappingEntitiesMappedByType);
//    setSuggestionsByOntologies(mappingEntitiesMappedByType);

    suggestionManager.setSuggestions(mappingEntitiesMappedByType);
  }

  private Map<String, List<MappingEntity>> getMappingEntitiesMappedByType() {
    Map<String, List<MappingEntity>> map = new HashMap<>();
    for (EntityType entityType : entityTypeService.getAll()) {
      String entityTypeName = entityType.getName();
      List<MappingEntity> mappingEntitiesByType = getAllByTypeName(entityTypeName);
      map.put(entityTypeName.toLowerCase(), mappingEntitiesByType);
    }
    return map;
  }

  /**
   * Sets the suggestions based on similar mapping entities.
   */
  private void setSuggestionsByMappingEntities(
      Map<String, List<MappingEntity>> mappingEntitiesMappedByType) {

    LOG.info("Init mapping entity suggestions");
    int count = 0;

    for (String type : mappingEntitiesMappedByType.keySet()) {
      List<MappingEntity> mappingsByType = mappingEntitiesMappedByType.get(type);
      Map<MappingEntity, Set<MappingEntitySuggestion>> suggestionsByEntity =
          mappingEntitiesSuggestionManager.calculateSuggestions(mappingsByType);

      for (MappingEntity mappingEntity : suggestionsByEntity.keySet()) {
        // First clean and save to make sure previous suggestions are deleted.
        mappingEntity.getMappingEntitySuggestions().clear();
        mappingEntityRepository.save(mappingEntity);

        // Children are saved explicitly as well
        Set<MappingEntitySuggestion> suggestions = suggestionsByEntity.get(mappingEntity);
        mappingEntity.getMappingEntitySuggestions().addAll(suggestions);
        mappingEntitySuggestionRepository.saveAll(suggestions);
        System.out.println("Ok " + count++);
      }
      // Need to check if this call is really necessary
      mappingEntityRepository.saveAll(mappingsByType);
    }

//    for (EntityType entityType : entityTypeService.getAll()) {
//      String entityTypeName = entityType.getName();
//      LOG.info("Calculating mapping entity suggestions for " + entityTypeName);
//      List<MappingEntity> mappingsByType = getAllByTypeName(entityTypeName);
//      Map<MappingEntity, Set<MappingEntitySuggestion>> suggestionsByEntity =
//          mappingEntitiesSuggestionManager.calculateSuggestions(mappingsByType);
//
//      for (MappingEntity mappingEntity : suggestionsByEntity.keySet()) {
//        // First clean and save to make sure previous suggestions are deleted.
//        mappingEntity.getMappingEntitySuggestions().clear();
//        mappingEntityRepository.save(mappingEntity);
//
//        // Children are saved explicitly as well
//        Set<MappingEntitySuggestion> suggestions = suggestionsByEntity.get(mappingEntity);
//        mappingEntity.getMappingEntitySuggestions().addAll(suggestions);
//        mappingEntitySuggestionRepository.saveAll(suggestions);
//
//      }
//      // Need to check if this call is really necessary
//      mappingEntityRepository.saveAll(mappingsByType);
//    }
    LOG.info("Finish mapping entity suggestions");
  }

  private void setSuggestionsByOntologies(
      Map<String, List<MappingEntity>> mappingEntitiesMappedByType) {
    LOG.info("Init ontology suggestions");

    Map<String, List<OntologyTerm>> ontologyTermsMappedByType =
        ontologyService.getOntologyTermsMappedByType();

    for (String type : mappingEntitiesMappedByType.keySet()) {

      List<MappingEntity> mappingsByType = mappingEntitiesMappedByType.get(type);
      LOG.info(String.format("Found %d %s mappings", mappingsByType.size(), type));
      List<OntologyTerm> ontologyTermsByType = ontologyTermsMappedByType.get(type);
      LOG.info(String.format("Found %d ontology %s terms", ontologyTermsByType.size(), type));

      Map<MappingEntity, Set<OntologySuggestion>> suggestionsByEntity =
          ontologySuggestionManager.calculateSuggestions(mappingsByType, ontologyTermsByType);

      for (MappingEntity mappingEntity : suggestionsByEntity.keySet()) {
        // First clean and save to make sure previous suggestions are deleted.
        mappingEntity.getOntologySuggestions().clear();
        mappingEntityRepository.save(mappingEntity);

        // Children are saved explicitly as well
        Set<OntologySuggestion> suggestions = suggestionsByEntity.get(mappingEntity);
        mappingEntity.getOntologySuggestions().addAll(suggestions);
        ontologySuggestionRepository.saveAll(suggestions);
      }
      // Need to check if this call is really necessary
      mappingEntityRepository.saveAll(mappingsByType);
    }
  }
}
