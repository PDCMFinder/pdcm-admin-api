package org.cancermodels.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long>,
    JpaSpecificationExecutor<MappingEntity> {

  void deleteAllBySourceType(String sourceType);
}
