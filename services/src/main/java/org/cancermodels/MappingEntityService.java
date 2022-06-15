package org.cancermodels;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MappingEntityService {

  private final MappingEntityRepository mappingEntityRepository;

  public MappingEntityService(MappingEntityRepository mappingEntityRepository) {
    this.mappingEntityRepository = mappingEntityRepository;
  }

  public List<MappingEntity> findAll() {
    return mappingEntityRepository.findAll();
  }

  public Page<MappingEntity> findAll(Pageable pageable) {
    return mappingEntityRepository.findAll(pageable);
  }
}
