package org.cancermodels.mappings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.cancermodels.pdcm_admin.types.MappingType;
import org.cancermodels.pdcm_admin.persistance.EntityType;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.pdcm_admin.persistance.MappingEntityRepository;
import org.springframework.stereotype.Service;

@Service
public class MappingEntityService {

  private final MappingEntityRepository mappingEntityRepository;
  private final EntityTypeService entityTypeService;
  private final MappingEntityUpdater mappingEntityUpdater;

  public MappingEntityService(MappingEntityRepository mappingEntityRepository,
      EntityTypeService entityTypeService,
      MappingEntityUpdater mappingEntityUpdater) {
    this.mappingEntityRepository = mappingEntityRepository;
    this.entityTypeService = entityTypeService;
    this.mappingEntityUpdater = mappingEntityUpdater;
  }

  /**
   * Find a {@link MappingEntity} using its id.
   * @param id ID of the mapping entity.
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
  public Optional<MappingEntity> findByKey(String key) {
    return mappingEntityRepository.findByMappingKey(key);
  }

  /**
   * Updates some values in a mapping entity, if changed: Status, Mapping Term Label, Mapping Term Url
   * @param id Identifier of the mapping entity.
   * @param mappingEntity The {@link MappingEntity} with the changes.
   * @param mappingType Indicates the way the mapping is being done (in case the mapping entity
   *                    is being mapped (or the mapped term/url are being updated).
   * @return {@link MappingEntity} after updated in the db.
   */
  public Optional<MappingEntity> update(int id, MappingEntity mappingEntity, MappingType mappingType) {
    var res = mappingEntityRepository.findById(id);
    if (res.isPresent()) {
      MappingEntity original = res.get();
      return Optional.of(mappingEntityUpdater.update(original, mappingEntity, mappingType)) ;
    } else {
      return Optional.empty();
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

  /**
   * Get all the {@link MappingEntity} objects by status.
   * @param status Status to use as a filter.
   * @return List of {@link MappingEntity}.
   */
  public List<MappingEntity> getAllByStatus(String status) {
    return mappingEntityRepository.findAllByStatusIgnoreCase(status);
  }

  public void deleteAll() {
    mappingEntityRepository.deleteAll();
  }

  public void savAll(List<MappingEntity> mappingEntities) {
    mappingEntityRepository.saveAll(mappingEntities);
  }

}
