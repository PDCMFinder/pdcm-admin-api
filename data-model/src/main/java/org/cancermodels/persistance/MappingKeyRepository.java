package org.cancermodels.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MappingKeyRepository extends JpaRepository<MappingKey, Long> {
  MappingKey findByKeyIgnoreCase(String key);
}
