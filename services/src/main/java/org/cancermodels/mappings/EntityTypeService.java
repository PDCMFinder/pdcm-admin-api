package org.cancermodels.mappings;

import java.util.List;
import org.cancermodels.persistance.EntityType;
import org.cancermodels.persistance.EntityTypeRepository;
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

  public List<EntityType> getAll() {
    return entityTypeRepository.findAll();
  }
}
