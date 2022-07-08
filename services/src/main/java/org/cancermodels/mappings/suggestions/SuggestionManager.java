package org.cancermodels.mappings.suggestions;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntityRepository;
import org.cancermodels.MappingEntitySuggestion;
import org.cancermodels.MappingEntitySuggestionRepository;
import org.cancermodels.OntologySuggestion;
import org.cancermodels.OntologySuggestionRepository;
import org.cancermodels.OntologyTerm;
import org.cancermodels.ontologies.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Class in charge of setting the suggestions for mapping entities base on:
 * 1) Existing mapping entities.
 * 2) Ontology terms.
 */
@Component
public class SuggestionManager {

  private final MappingEntitiesSuggestionManager mappingEntitiesSuggestionManager;
  private final MappingEntitySuggestionRepository mappingEntitySuggestionRepository;
  private final OntologySuggestionRepository ontologySuggestionRepository;
  private final MappingEntityRepository mappingEntityRepository;
  private final OntologyService ontologyService;
  private final OntologySuggestionManager ontologySuggestionManager;

  private static final Logger LOG = LoggerFactory.getLogger(SuggestionManager.class);

  public SuggestionManager(
      MappingEntitiesSuggestionManager mappingEntitiesSuggestionManager,
      MappingEntitySuggestionRepository mappingEntitySuggestionRepository,
      OntologySuggestionRepository ontologySuggestionRepository,
      MappingEntityRepository mappingEntityRepository,
      OntologyService ontologyService,
      OntologySuggestionManager ontologySuggestionManager) {

    this.mappingEntitiesSuggestionManager = mappingEntitiesSuggestionManager;
    this.mappingEntitySuggestionRepository = mappingEntitySuggestionRepository;
    this.ontologySuggestionRepository = ontologySuggestionRepository;
    this.mappingEntityRepository = mappingEntityRepository;
    this.ontologyService = ontologyService;
    this.ontologySuggestionManager = ontologySuggestionManager;
  }

  /**
   * Sets the suggestions by rules and by ontologies for all the mapping entities in the system
   */
  public void calculateSuggestions(Map<String, List<MappingEntity>> mappingEntitiesMappedByType) {
    LOG.info("Init suggestion calculation process");
    resetData(mappingEntitiesMappedByType);

    setSuggestionsByMappingEntities(mappingEntitiesMappedByType);
    setSuggestionsByOntologies(mappingEntitiesMappedByType);

    saveMappingEntities(mappingEntitiesMappedByType);

  }

  /**
   * Sets the suggestions by rules and by ontologies for a the mapping entity.
   */
  public void setSuggestionsForOneEntity(MappingEntity mappingEntity) {
    // Reset data only for this entity
    List<MappingEntitySuggestion> mappingEntitySuggestions =
        mappingEntity.getMappingEntitySuggestions();
    mappingEntitySuggestionRepository.deleteAll(mappingEntitySuggestions);
    List<OntologySuggestion> ontologySuggestions =
        mappingEntity.getOntologySuggestions();
    ontologySuggestionRepository.deleteAll(ontologySuggestions);
    mappingEntity.getMappingEntitySuggestions().clear();
    mappingEntity.getOntologySuggestions().clear();
    mappingEntityRepository.save(mappingEntity);



  }

  private void resetData(Map<String, List<MappingEntity>> mappingEntitiesMappedByType) {

    LOG.info("Resetting data");
//    mappingEntitySuggestionRepository.deleteAll();
//    ontologySuggestionRepository.deleteAll();

    for (String type : mappingEntitiesMappedByType.keySet()) {
      List<MappingEntity> entitiesByType = mappingEntitiesMappedByType.get(type);
      for (MappingEntity mappingEntity : entitiesByType) {
        mappingEntity.getMappingEntitySuggestions().clear();
        mappingEntity.getOntologySuggestions().clear();
      }
      // Save all entities to execute the deletion on db
      mappingEntityRepository.saveAll(entitiesByType);
    }
  }

  private void saveMappingEntities(Map<String, List<MappingEntity>> mappingEntitiesMappedByType) {
    for (String type : mappingEntitiesMappedByType.keySet()) {
      List<MappingEntity> entitiesByType = mappingEntitiesMappedByType.get(type);
      mappingEntityRepository.saveAll(entitiesByType);
    }
  }

  /**
   * Sets the suggestions based on similar mapping entities.
   */
  private void setSuggestionsByMappingEntities(
      Map<String, List<MappingEntity>> mappingEntitiesMappedByType) {

    LOG.info("Init mapping entity suggestions");
    Set<MappingEntitySuggestion> allSuggestions = new HashSet<>();

    for (String type : mappingEntitiesMappedByType.keySet()) {

      List<MappingEntity> mappingsByType = mappingEntitiesMappedByType.get(type);
      Map<MappingEntity, List<MappingEntitySuggestion>> suggestionsByEntity =
          mappingEntitiesSuggestionManager.calculateSuggestions(mappingsByType, type);

      for (MappingEntity mappingEntity : suggestionsByEntity.keySet()) {

        // Children are saved explicitly
        List<MappingEntitySuggestion> suggestions = suggestionsByEntity.get(mappingEntity);
        mappingEntity.getMappingEntitySuggestions().addAll(suggestions);
        allSuggestions.addAll(suggestions);
      }
    }
    mappingEntitySuggestionRepository.saveAll(allSuggestions);
    LOG.info("Finish mapping entity suggestions");
  }

  private void setSuggestionsByOntologies(
      Map<String, List<MappingEntity>> mappingEntitiesMappedByType) {
    LOG.info("Init ontology suggestions");

    Map<String, List<OntologyTerm>> ontologyTermsMappedByType =
        ontologyService.getOntologyTermsMappedByType();

    Set<OntologySuggestion> allSuggestions = new HashSet<>();

    for (String type : mappingEntitiesMappedByType.keySet()) {

      List<MappingEntity> mappingsByType = mappingEntitiesMappedByType.get(type);
      LOG.info(String.format("Found %d %s mappings", mappingsByType.size(), type));
      List<OntologyTerm> ontologyTermsByType = ontologyTermsMappedByType.get(type);
      LOG.info(String.format("Found %d ontology %s terms", ontologyTermsByType.size(), type));

      Map<MappingEntity, List<OntologySuggestion>> suggestionsByEntity =
          ontologySuggestionManager.calculateSuggestions(mappingsByType, ontologyTermsByType, type);

      for (MappingEntity mappingEntity : suggestionsByEntity.keySet()) {

        // Children are saved explicitly as well
        List<OntologySuggestion> suggestions = suggestionsByEntity.get(mappingEntity);
        mappingEntity.getOntologySuggestions().addAll(suggestions);
        allSuggestions.addAll(suggestions);
      }
    }
    ontologySuggestionRepository.saveAll(allSuggestions);
    LOG.info("Finish ontology suggestions");
  }


}
