package org.cancermodels;

import org.springframework.stereotype.Component;

@Component
public class EntityTypeService {

  private final EntityTypeRepository entityTypeRepository;

  public EntityTypeService(EntityTypeRepository entityTypeRepository) {
    this.entityTypeRepository = entityTypeRepository;
  }

  public EntityType getEntityTypeByName(String name) {
    return entityTypeRepository.getByName(name);
  }
}
