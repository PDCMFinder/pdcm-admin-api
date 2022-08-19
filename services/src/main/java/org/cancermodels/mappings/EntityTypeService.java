package org.cancermodels.mappings;

import java.util.List;
import org.cancermodels.persistance.EntityType;
import org.cancermodels.persistance.EntityTypeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class EntityTypeService {

  private final EntityTypeRepository entityTypeRepository;

  public EntityTypeService(EntityTypeRepository entityTypeRepository) {
    this.entityTypeRepository = entityTypeRepository;
  }

  @Cacheable("entityType")
  public EntityType getEntityTypeByName(String name) {
    return entityTypeRepository.getByNameIgnoreCase(name);
  }

  public List<EntityType> getAll() {
    return entityTypeRepository.findAll();
  }
}
