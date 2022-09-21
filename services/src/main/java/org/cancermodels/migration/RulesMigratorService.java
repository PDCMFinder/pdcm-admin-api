package org.cancermodels.migration;

import java.util.Set;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingEntityRepository;
import org.springframework.stereotype.Service;

// Migrate old rules to new format
@Service
public class RulesMigratorService {

  private final OldRulesReader oldRulesReader;
  private final MappingEntityRepository mappingEntityRepository;

  public RulesMigratorService(OldRulesReader oldRulesReader,
      MappingEntityRepository mappingEntityRepository) {
    this.oldRulesReader = oldRulesReader;
    this.mappingEntityRepository = mappingEntityRepository;
  }

  public void loadOldRulesInDb() {
    mappingEntityRepository.deleteAll();
    Set<MappingEntity> diagnosisRules = oldRulesReader.readRules("diagnosis_mappings.json");
    Set<MappingEntity> treatmentRules = oldRulesReader.readRules("treatment_mappings.json");
    mappingEntityRepository.saveAll(diagnosisRules);
    mappingEntityRepository.saveAll(treatmentRules);
  }
}
