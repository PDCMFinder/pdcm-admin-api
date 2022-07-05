package org.cancermodels.mappings;

import java.util.List;
import org.cancermodels.EntityType;
import org.cancermodels.EntityTypeRepository;
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
