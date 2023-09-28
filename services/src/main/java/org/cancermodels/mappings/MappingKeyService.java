package org.cancermodels.mappings;

import java.util.List;
import org.cancermodels.pdcm_admin.persistance.MappingKey;
import org.cancermodels.pdcm_admin.persistance.MappingKeyRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class MappingKeyService {

  private final MappingKeyRepository mappingKeyRepository;

  public MappingKeyService(MappingKeyRepository mappingKeyRepository) {
    this.mappingKeyRepository = mappingKeyRepository;
  }

  @Cacheable
  public MappingKey getByName(String name) {
    return mappingKeyRepository.findByKeyIgnoreCase(name);
  }

  public List<MappingKey> getAll() {
    return mappingKeyRepository.findAll();
  }
}
