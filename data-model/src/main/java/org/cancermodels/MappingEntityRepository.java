package org.cancermodels;

import java.awt.print.Pageable;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MappingEntityRepository extends
    JpaRepository<MappingEntity, Long>, JpaSpecificationExecutor<MappingEntity> {

  @Query(
      value =
          "select  lower(mv.value), status, count(1)\n"
              + "from \n"
              + "ENTITY_TYPE t, MAPPING_ENTITY me, MAPPING_VALUE mv, MAPPING_KEY mk\n"
              + "where lower(t.NAME) = lower(:entityTypeName)\n"
              + "and me.ENTITY_TYPE_ID = t.id\n"
              + "and mv.MAPPING_ENTITY_ID = me.ID\n"
              + "and mv.KEY_ID = mk.ID  \n"
              + "and lower(mk.KEY) = lower('DataSource')\n"
              + "group by lower(mv.value), status order by  lower(mv.value)",
      nativeQuery = true)
  List<Object[]> countEntityTypeStatusByProvider(@Param("entityTypeName") String entityTypeName);

  List<MappingEntity> findAllByEntityTypeNameIgnoreCase(String entityTypeName);
}
