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
  private final OntologySuggestionManager ontologySuggestionManager;

  private static final Logger LOG = LoggerFactory.getLogger(SuggestionManager.class);

  public SuggestionManager(
      MappingEntitiesSuggestionManager mappingEntitiesSuggestionManager,
      MappingEntitySuggestionRepository mappingEntitySuggestionRepository,
      OntologySuggestionRepository ontologySuggestionRepository,
      MappingEntityRepository mappingEntityRepository,
      OntologySuggestionManager ontologySuggestionManager) {

    this.mappingEntitiesSuggestionManager = mappingEntitiesSuggestionManager;
    this.mappingEntitySuggestionRepository = mappingEntitySuggestionRepository;
    this.ontologySuggestionRepository = ontologySuggestionRepository;
    this.mappingEntityRepository = mappingEntityRepository;
    this.ontologySuggestionManager = ontologySuggestionManager;
  }

  /**
   * Sets the suggestions by rules and by ontologies for all the mapping entities in the system
   */
  public void calculateSuggestions(List<MappingEntity> toProcess,  String type) {
    LOG.info("Init suggestion calculation process");
    resetData(toProcess);

    setSuggestionsByMappingEntities(toProcess, type);
    setSuggestionsByOntologies(toProcess, type);

    saveMappingEntities(toProcess);

  }

  private void resetData(List<MappingEntity> toProcess) {
    LOG.info("Resetting data");

    for (MappingEntity mappingEntity : toProcess) {
      mappingEntity.getMappingEntitySuggestions().clear();
      mappingEntity.getOntologySuggestions().clear();
    }
    // Save all entities to execute the deletion on db
    mappingEntityRepository.saveAll(toProcess);
  }

  private void saveMappingEntities(List<MappingEntity> toProcess) {
      mappingEntityRepository.saveAll(toProcess);
  }

  /** Sets the suggestions based on similar mapping entities. */
  private void setSuggestionsByMappingEntities(
      List<MappingEntity> toProcess, String type) {

    LOG.info("Init mapping entity suggestions");
    Set<MappingEntitySuggestion> allSuggestions = new HashSet<>();

    Map<MappingEntity, List<MappingEntitySuggestion>> suggestionsByEntity =
        mappingEntitiesSuggestionManager.calculateSuggestions(toProcess, type);

    for (MappingEntity mappingEntity : suggestionsByEntity.keySet()) {
      // Children are saved explicitly
      List<MappingEntitySuggestion> suggestions = suggestionsByEntity.get(mappingEntity);
      mappingEntity.getMappingEntitySuggestions().addAll(suggestions);
      allSuggestions.addAll(suggestions);
    }

    mappingEntitySuggestionRepository.saveAll(allSuggestions);
    LOG.info("Finish mapping entity suggestions");
  }

  private void setSuggestionsByOntologies(List<MappingEntity> toProcess, String type) {
    LOG.info("Init ontology suggestions");

    Set<OntologySuggestion> allSuggestions = new HashSet<>();

    Map<MappingEntity, List<OntologySuggestion>> suggestionsByEntity =
        ontologySuggestionManager.calculateSuggestions(toProcess, type);

    for (MappingEntity mappingEntity : suggestionsByEntity.keySet()) {

      // Children are saved explicitly as well
      List<OntologySuggestion> suggestions = suggestionsByEntity.get(mappingEntity);
      mappingEntity.getOntologySuggestions().addAll(suggestions);
      allSuggestions.addAll(suggestions);
    }

    ontologySuggestionRepository.saveAll(allSuggestions);
    LOG.info("Finish ontology suggestions");
  }
}
