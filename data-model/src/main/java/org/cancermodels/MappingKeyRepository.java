package org.cancermodels;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MappingKeyRepository extends JpaRepository<MappingKey, Long> {

}
