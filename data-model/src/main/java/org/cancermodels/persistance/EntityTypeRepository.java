package org.cancermodels.persistance;

import org.cancermodels.persistance.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityTypeRepository extends JpaRepository<EntityType, Long> {
  EntityType getByName(String name);

}
