package org.cancermodels.persistance;

import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long>,
    JpaSpecificationExecutor<MappingEntity> {

}
