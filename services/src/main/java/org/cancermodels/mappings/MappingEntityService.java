package org.cancermodels.mappings;

import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntityRepository;
import org.cancermodels.mappings.search.MappingsFilter;
import org.cancermodels.mappings.search.MappingsSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class MappingEntityService {

  private final MappingEntityRepository mappingEntityRepository;

  public MappingEntityService(MappingEntityRepository mappingEntityRepository) {
    this.mappingEntityRepository = mappingEntityRepository;
  }

  public Page<MappingEntity> findPaginatedAndFiltered(
      Pageable pageable, MappingsFilter mappingsFilter) {

    Specification<MappingEntity> specs = buildSpecifications(mappingsFilter);
    return mappingEntityRepository.findAll(specs, pageable);
  }

  private Specification<MappingEntity> buildSpecifications(MappingsFilter mappingsFilter)
  {
    Specification<MappingEntity> specifications =
        Specification.where(
            MappingsSpecs.withStatus(mappingsFilter.getStatus())
                .and(MappingsSpecs.withMappingQuery(mappingsFilter.getMappingQuery())
                .and(MappingsSpecs.withEntityTypeNames(mappingsFilter.getEntityTypeNames()))
            ));
    return specifications;
  }
}
