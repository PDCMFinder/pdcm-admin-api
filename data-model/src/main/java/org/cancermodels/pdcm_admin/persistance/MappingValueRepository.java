package org.cancermodels.pdcm_admin.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MappingValueRepository extends JpaRepository<MappingValue, Long> {

}
