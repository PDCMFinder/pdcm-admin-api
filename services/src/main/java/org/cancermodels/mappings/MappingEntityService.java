package org.cancermodels.mappings;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.cancermodels.types.MappingType;
import org.cancermodels.persistance.EntityType;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingEntityRepository;
import org.cancermodels.mappings.suggestions.SuggestionManager;
import org.springframework.stereotype.Service;

@Service
public class MappingEntityService {

  private final MappingEntityRepository mappingEntityRepository;
  private final EntityTypeService entityTypeService;
  private final SuggestionManager suggestionManager;
  private final Updater updater;

  public MappingEntityService(MappingEntityRepository mappingEntityRepository,
      EntityTypeService entityTypeService,
      SuggestionManager suggestionManager,
      Updater updater) {
    this.mappingEntityRepository = mappingEntityRepository;
    this.entityTypeService = entityTypeService;
    this.suggestionManager = suggestionManager;
    this.updater = updater;
  }

  /**
   * Find a {@link MappingEntity} using its id.
   * @param id Id of the mapping entity.
   * @return Optional with the Mapping entity if found.
   */
  public Optional<MappingEntity> findById(int id) {
    return mappingEntityRepository.findById(id);
  }

  /**
   * Find a {@link MappingEntity} using its key.
   * @param key key of the mapping entity.
   * @return Optional with the Mapping entity if found.
   */
  public MappingEntity findByKey(String key) {
    return mappingEntityRepository.findByMappingKey(key);
  }

  /**
   * Updates some values in a mapping entity, if changed: Status, Mapping Term Label, Mapping Term Url
   * @param mappingEntity Entity with the new information
   * @return Mapping after it was updated
   */
  public Optional<MappingEntity> update(int id, MappingEntity mappingEntity, MappingType mappingType) {
    var res = mappingEntityRepository.findById(id);
    if (res.isPresent()) {
      MappingEntity original = res.get();
      return Optional.of(updater.update(original, mappingEntity, mappingType)) ;
    } else {
      return Optional.empty();
    }
  }

  /**
   * Sets the suggestions by rules and by ontologies for all the mapping entities in the system
   */
  public void setMappingSuggestions() throws IOException {
    Map<String, List<MappingEntity>> mappingEntitiesMappedByType = getMappingEntitiesMappedByType();
    for (String type : mappingEntitiesMappedByType.keySet()) {
      // Process all mappings
      List<MappingEntity> toProcess = mappingEntitiesMappedByType.get(type);
      suggestionManager.calculateSuggestions(toProcess);
    }

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

  public List<MappingEntity> getAllByTypeName(String entityTypeName) {
    return mappingEntityRepository.findAllByEntityTypeNameIgnoreCase(entityTypeName);
  }

  public List<MappingEntity> getAllByTypeNameAndStatus(String entityTypeName, String status) {
    return mappingEntityRepository.findAllByEntityTypeNameIgnoreCaseAndStatusOrderByMappingKeyAsc(
        entityTypeName, status);
  }

  public void deleteAll() {
    mappingEntityRepository.deleteAll();
  }

  public void savAll(List<MappingEntity> mappingEntities) {
    mappingEntityRepository.saveAll(mappingEntities);
  }

}
