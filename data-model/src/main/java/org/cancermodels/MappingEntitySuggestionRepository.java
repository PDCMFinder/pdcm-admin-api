package org.cancermodels;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MappingEntitySuggestionRepository extends
    JpaRepository<MappingEntitySuggestion, Long>, JpaSpecificationExecutor<MappingEntity> {

}
