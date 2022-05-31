package org.cancermodels;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MappingValueRepository extends JpaRepository<MappingValue, Long> {

}
