package org.cancermodels;

import java.awt.print.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MappingEntityRepository extends
    JpaRepository<MappingEntity, Long>, JpaSpecificationExecutor<MappingEntity> {
}
