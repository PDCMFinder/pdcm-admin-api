package org.cancermodels;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OntologySuggestionRepository extends
    JpaRepository<OntologySuggestion, Long>, JpaSpecificationExecutor<MappingEntity> {

}
